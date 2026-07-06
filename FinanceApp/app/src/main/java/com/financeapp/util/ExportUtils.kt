package com.financeapp.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.financeapp.data.Expense
import com.financeapp.data.SavingsGoal
import com.financeapp.data.SpendingAlert
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportUtils {

    fun exportExpensesToCsv(
        context: Context,
        expenses: List<Expense>,
        fileName: String = "expenses_${System.currentTimeMillis()}"
    ): Uri? {
        return try {
            val file = createFile(context, "$fileName.csv")
            FileWriter(file).use { writer ->
                writer.append("Date,Category,Description,Amount\n")
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
                expenses.forEach { expense ->
                    writer.append("${dateFormat.format(Date(expense.date))},${escapeCsv(expense.category)},${escapeCsv(expense.description)},${expense.amount}\n")
                }
            }
            getFileUri(context, file)
        } catch (e: Exception) {
            null
        }
    }

    fun exportGoalsToCsv(
        context: Context,
        goals: List<SavingsGoal>,
        fileName: String = "goals_${System.currentTimeMillis()}"
    ): Uri? {
        return try {
            val file = createFile(context, "$fileName.csv")
            FileWriter(file).use { writer ->
                writer.append("Name,Target Amount,Current Amount,Progress %,Deadline\n")
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                goals.forEach { goal ->
                    val progress = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount * 100) else 0.0
                    writer.append("${escapeCsv(goal.name)},${goal.targetAmount},${goal.currentAmount},${String.format("%.1f", progress)},${dateFormat.format(Date(goal.deadline))}\n")
                }
            }
            getFileUri(context, file)
        } catch (e: Exception) {
            null
        }
    }

    fun exportAlertsToCsv(
        context: Context,
        alerts: List<SpendingAlert>,
        fileName: String = "alerts_${System.currentTimeMillis()}"
    ): Uri? {
        return try {
            val file = createFile(context, "$fileName.csv")
            FileWriter(file).use { writer ->
                writer.append("Category,Type,Limit,Spent,Message,Date\n")
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
                alerts.forEach { alert ->
                    writer.append("${escapeCsv(alert.category)},${alert.alertType},${alert.limitAmount},${alert.currentSpent},${escapeCsv(alert.message)},${dateFormat.format(Date(alert.timestamp))}\n")
                }
            }
            getFileUri(context, file)
        } catch (e: Exception) {
            null
        }
    }

    fun exportFullReport(
        context: Context,
        expenses: List<Expense>,
        goals: List<SavingsGoal>,
        alerts: List<SpendingAlert>,
        salary: Double,
        month: Int,
        year: Int
    ): Uri? {
        return try {
            val monthName = SimpleDateFormat("MMMM_yyyy", Locale.US).format(Date())
            val file = createFile(context, "finance_report_$monthName.txt")
            FileWriter(file).use { writer ->
                writer.append("=== FINANCE REPORT - $monthName ===\n\n")

                writer.append("--- SALARY ---\n")
                writer.append("Monthly Salary: $$salary\n\n")

                writer.append("--- EXPENSES SUMMARY ---\n")
                val totalExpenses = expenses.sumOf { it.amount }
                writer.append("Total Expenses: $$totalExpenses\n")
                writer.append("Net Savings: $${salary - totalExpenses}\n\n")

                writer.append("Category Breakdown:\n")
                expenses.groupBy { it.category }.forEach { (category, catExpenses) ->
                    val catTotal = catExpenses.sumOf { it.amount }
                    val percent = if (totalExpenses > 0) (catTotal / totalExpenses * 100) else 0.0
                    writer.append("  $category: $$catTotal (${String.format("%.1f", percent)}%)\n")
                }

                writer.append("\n--- SAVINGS GOALS ---\n")
                if (goals.isEmpty()) {
                    writer.append("No goals set.\n")
                } else {
                    goals.forEach { goal ->
                        val progress = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount * 100) else 0.0
                        writer.append("  ${goal.name}: $${goal.currentAmount} / $${goal.targetAmount} (${String.format("%.1f", progress)}%)\n")
                    }
                }

                writer.append("\n--- SPENDING ALERTS ---\n")
                if (alerts.isEmpty()) {
                    writer.append("No alerts.\n")
                } else {
                    alerts.forEach { alert ->
                        writer.append("  [${alert.alertType}] ${alert.category}: ${alert.message}\n")
                    }
                }

                writer.append("\n--- GENERATED ---\n")
                writer.append("Report generated on: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())}\n")
            }
            getFileUri(context, file)
        } catch (e: Exception) {
            null
        }
    }

    private fun createFile(context: Context, fileName: String): File {
        val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "FinanceApp")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, fileName)
    }

    private fun getFileUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }

    fun shareFile(context: Context, uri: Uri, mimeType: String = "text/csv") {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Report"))
    }
}
