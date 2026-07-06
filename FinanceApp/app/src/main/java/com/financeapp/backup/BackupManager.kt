package com.financeapp.backup

import android.content.Context
import android.os.Environment
import com.financeapp.data.BackupData
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupFileInfo(
    val file: File,
    val name: String,
    val timestamp: Long,
    val sizeBytes: Long,
    val formattedDate: String,
    val formattedSize: String
)

object BackupManager {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    private fun getBackupDir(context: Context): File {
        val dir = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "FinanceApp/Backups"
        )
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun createBackupFileName(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return "${BackupData.BACKUP_FILE_PREFIX}${timestamp}${BackupData.BACKUP_FILE_EXTENSION}"
    }

    fun saveBackup(context: Context, backupData: BackupData): Result<File> {
        return try {
            val dir = getBackupDir(context)
            val fileName = createBackupFileName()
            val file = File(dir, fileName)

            val json = gson.toJson(backupData)
            FileWriter(file).use { writer ->
                writer.write(json)
                writer.flush()
            }

            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun readBackup(file: File): Result<BackupData> {
        return try {
            val reader = FileReader(file)
            val json = reader.readText()
            reader.close()

            val validationResult = validateBackupJson(json)
            if (validationResult.isFailure) {
                return Result.failure(validationResult.exceptionOrNull()!!)
            }

            val backupData = gson.fromJson(json, BackupData::class.java)
            Result.success(backupData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun validateBackupJson(json: String): Result<Unit> {
        return try {
            val parser = JsonParser.parseString(json)
            if (!parser.isJsonObject) {
                return Result.failure(Exception("Invalid backup format: not a JSON object"))
            }

            val obj = parser.asJsonObject

            if (!obj.has("version")) {
                return Result.failure(Exception("Invalid backup: missing version field"))
            }

            val version = obj.get("version").asInt
            if (version > BackupData.BACKUP_VERSION) {
                return Result.failure(Exception("Backup version $version is newer than supported version ${BackupData.BACKUP_VERSION}"))
            }

            if (!obj.has("expenses") && !obj.has("salaryConfig") &&
                !obj.has("savingsGoals") && !obj.has("portfolioHoldings") &&
                !obj.has("budgetAllocations")
            ) {
                return Result.failure(Exception("Invalid backup: no data fields found"))
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Invalid backup file: ${e.message}"))
        }
    }

    fun getAvailableBackups(context: Context): List<BackupFileInfo> {
        val dir = getBackupDir(context)
        val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US)

        return dir.listFiles { file ->
            file.isFile && file.name.endsWith(BackupData.BACKUP_FILE_EXTENSION)
        }?.map { file ->
            val name = file.nameWithoutExtension
                .removePrefix(BackupData.BACKUP_FILE_PREFIX)
                .replace("_", " ")
                .replaceFirstChar { it.uppercase() }

            BackupFileInfo(
                file = file,
                name = file.name,
                timestamp = file.lastModified(),
                sizeBytes = file.length(),
                formattedDate = dateFormat.format(Date(file.lastModified())),
                formattedSize = formatFileSize(file.length())
            )
        }?.sortedByDescending { it.timestamp } ?: emptyList()
    }

    fun deleteBackup(file: File): Result<Unit> {
        return try {
            if (file.exists()) {
                file.delete()
                Result.success(Unit)
            } else {
                Result.failure(Exception("File not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun cleanOldBackups(context: Context, keepCount: Int = 10): Int {
        val backups = getAvailableBackups(context)
        var deletedCount = 0

        if (backups.size > keepCount) {
            backups.drop(keepCount).forEach { backup ->
                deleteBackup(backup.file)
                deletedCount++
            }
        }

        return deletedCount
    }

    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> "${String.format("%.1f", bytes / (1024.0 * 1024.0))} MB"
        }
    }
}
