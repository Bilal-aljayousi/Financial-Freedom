package com.financeapp.backup

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.data.BackupData
import com.financeapp.data.FinanceDatabase
import com.financeapp.data.FinanceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class BackupManagerUiState(
    val isLoading: Boolean = false,
    val backups: List<BackupFileInfo> = emptyList(),
    val message: String? = null,
    val isError: Boolean = false,
    val showRestoreDialog: Boolean = false,
    val selectedBackup: BackupFileInfo? = null,
    val showDeleteDialog: Boolean = false,
    val backupToDelete: BackupFileInfo? = null,
    val autoBackupEnabled: Boolean = false,
    val backupFrequency: String = BackupWorker.FREQUENCY_WEEKLY,
    val lastBackupTime: Long = 0L,
    val backupPreview: BackupData? = null,
    val showOverwriteDialog: Boolean = false,
    val pendingRestoreData: BackupData? = null
)

class BackupManagerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinanceRepository
    private val appContext = application

    private val _uiState = MutableStateFlow(BackupManagerUiState())
    val uiState: StateFlow<BackupManagerUiState> = _uiState.asStateFlow()

    init {
        val db = FinanceDatabase.getDatabase(application)
        repository = FinanceRepository(db)
        loadBackups()
        loadAutoBackupSettings()
    }

    private fun loadBackups() {
        viewModelScope.launch {
            val backups = withContext(Dispatchers.IO) {
                BackupManager.getAvailableBackups(appContext)
            }
            _uiState.value = _uiState.value.copy(backups = backups)
        }
    }

    private fun loadAutoBackupSettings() {
        _uiState.value = _uiState.value.copy(
            autoBackupEnabled = BackupWorker.getAutoBackupEnabled(appContext),
            backupFrequency = BackupWorker.getBackupFrequency(appContext),
            lastBackupTime = BackupWorker.getLastBackupTime(appContext)
        )
    }

    fun createBackup() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, message = null)
            try {
                val backupData = withContext(Dispatchers.IO) {
                    repository.createBackupData()
                }
                val result = withContext(Dispatchers.IO) {
                    BackupManager.saveBackup(appContext, backupData)
                }

                if (result.isSuccess) {
                    val file = result.getOrThrow()
                    val totalRecords = backupData.expenses.size +
                            backupData.savingsGoals.size +
                            backupData.portfolioHoldings.size +
                            (if (backupData.salaryConfig != null) 1 else 0) +
                            backupData.budgetAllocations.size

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "Backup created successfully! $totalRecords records saved."
                    )
                    loadBackups()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "Backup failed: ${result.exceptionOrNull()?.message}",
                        isError = true
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Backup failed: ${e.message}",
                    isError = true
                )
            }
        }
    }

    fun previewBackup(file: BackupFileInfo) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, message = null)
            try {
                val result = withContext(Dispatchers.IO) {
                    BackupManager.readBackup(file.file)
                }

                if (result.isSuccess) {
                    val backupData = result.getOrThrow()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        backupPreview = backupData,
                        showRestoreDialog = true,
                        selectedBackup = file
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "Failed to read backup: ${result.exceptionOrNull()?.message}",
                        isError = true
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Error reading backup: ${e.message}",
                    isError = true
                )
            }
        }
    }

    fun restoreBackup(overwrite: Boolean) {
        val backupData = _uiState.value.backupPreview ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                showRestoreDialog = false,
                message = null
            )
            try {
                withContext(Dispatchers.IO) {
                    repository.restoreFromBackup(backupData, overwrite)
                }

                val totalRecords = backupData.expenses.size +
                        backupData.savingsGoals.size +
                        backupData.portfolioHoldings.size +
                        (if (backupData.salaryConfig != null) 1 else 0) +
                        backupData.budgetAllocations.size

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Data restored successfully! $totalRecords records imported.",
                    backupPreview = null,
                    selectedBackup = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Restore failed: ${e.message}",
                    isError = true
                )
            }
        }
    }

    fun requestDeleteBackup(backup: BackupFileInfo) {
        _uiState.value = _uiState.value.copy(
            showDeleteDialog = true,
            backupToDelete = backup
        )
    }

    fun confirmDeleteBackup() {
        val backup = _uiState.value.backupToDelete ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, showDeleteDialog = false)
            try {
                val result = withContext(Dispatchers.IO) {
                    BackupManager.deleteBackup(backup.file)
                }

                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "Backup deleted successfully."
                    )
                    loadBackups()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "Failed to delete backup: ${result.exceptionOrNull()?.message}",
                        isError = true
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Delete failed: ${e.message}",
                    isError = true
                )
            }
        }
    }

    fun shareBackup(file: BackupFileInfo) {
        try {
            val uri = FileProvider.getUriForFile(
                appContext,
                "${appContext.packageName}.fileprovider",
                file.file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            appContext.startActivity(Intent.createChooser(intent, "Share Backup"))
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                message = "Failed to share backup: ${e.message}",
                isError = true
            )
        }
    }

    fun toggleAutoBackup(enabled: Boolean) {
        BackupWorker.setAutoBackupEnabled(appContext, enabled)
        _uiState.value = _uiState.value.copy(autoBackupEnabled = enabled)
    }

    fun setBackupFrequency(frequency: String) {
        BackupWorker.setBackupFrequency(appContext, frequency)
        _uiState.value = _uiState.value.copy(backupFrequency = frequency)
    }

    fun dismissDialog() {
        _uiState.value = _uiState.value.copy(
            showRestoreDialog = false,
            showDeleteDialog = false,
            showOverwriteDialog = false,
            backupPreview = null,
            selectedBackup = null,
            backupToDelete = null,
            pendingRestoreData = null
        )
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null, isError = false)
    }

    fun clearAllData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, message = null)
            try {
                withContext(Dispatchers.IO) {
                    repository.clearAllData()
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "All data cleared successfully."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Failed to clear data: ${e.message}",
                    isError = true
                )
            }
        }
    }
}
