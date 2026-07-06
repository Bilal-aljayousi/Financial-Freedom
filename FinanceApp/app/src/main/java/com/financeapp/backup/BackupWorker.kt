package com.financeapp.backup

import android.content.Context
import android.content.SharedPreferences
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.financeapp.data.FinanceDatabase
import com.financeapp.data.FinanceRepository

class BackupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val enabled = prefs.getBoolean(KEY_AUTO_BACKUP_ENABLED, false)

        if (!enabled) return Result.success()

        val frequency = prefs.getString(KEY_BACKUP_FREQUENCY, FREQUENCY_WEEKLY) ?: FREQUENCY_WEEKLY

        val lastBackupTime = prefs.getLong(KEY_LAST_BACKUP_TIME, 0)
        val now = System.currentTimeMillis()
        val elapsed = now - lastBackupTime

        val shouldBackup = when (frequency) {
            FREQUENCY_WEEKLY -> elapsed >= 7 * 24 * 60 * 60 * 1000L
            FREQUENCY_MONTHLY -> elapsed >= 30 * 24 * 60 * 60 * 1000L
            else -> false
        }

        if (!shouldBackup) return Result.success()

        return try {
            val db = FinanceDatabase.getDatabase(applicationContext)
            val repository = FinanceRepository(db)
            val backupData = repository.createBackupData()

            val result = BackupManager.saveBackup(applicationContext, backupData)
            if (result.isSuccess) {
                prefs.edit().putLong(KEY_LAST_BACKUP_TIME, now).apply()
                BackupManager.cleanOldBackups(applicationContext, keepCount = 10)
                Result.success()
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Backup failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        const val WORK_NAME = "auto_backup"
        const val PREFS_NAME = "backup_prefs"
        const val KEY_AUTO_BACKUP_ENABLED = "auto_backup_enabled"
        const val KEY_BACKUP_FREQUENCY = "backup_frequency"
        const val KEY_LAST_BACKUP_TIME = "last_backup_time"
        const val FREQUENCY_WEEKLY = "weekly"
        const val FREQUENCY_MONTHLY = "monthly"

        fun setAutoBackupEnabled(context: Context, enabled: Boolean) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putBoolean(KEY_AUTO_BACKUP_ENABLED, enabled).apply()
        }

        fun setBackupFrequency(context: Context, frequency: String) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putString(KEY_BACKUP_FREQUENCY, frequency).apply()
        }

        fun getAutoBackupEnabled(context: Context): Boolean {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_AUTO_BACKUP_ENABLED, false)
        }

        fun getBackupFrequency(context: Context): String {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_BACKUP_FREQUENCY, FREQUENCY_WEEKLY) ?: FREQUENCY_WEEKLY
        }

        fun getLastBackupTime(context: Context): Long {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getLong(KEY_LAST_BACKUP_TIME, 0)
        }
    }
}
