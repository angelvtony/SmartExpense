package com.example.smartexpense.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.smartexpense.data.local.entity.Transaction
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import com.example.smartexpense.data.local.entity.TransactionType

object ReportExporter {

    fun generateExcelReport(context: Context, transactions: List<Transaction>): Uri? {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Transactions")

        val headerRow = sheet.createRow(0)
        val headers = arrayOf("ID", "Date", "Title", "Amount", "Category", "Type", "Payment Method", "Note")
        headers.forEachIndexed { index, title ->
            headerRow.createCell(index).setCellValue(title)
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        transactions.forEachIndexed { index, transaction ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(transaction.id.toDouble())
            row.createCell(1).setCellValue(dateFormat.format(transaction.date))
            row.createCell(2).setCellValue(transaction.title)
            row.createCell(3).setCellValue(transaction.amount)
            row.createCell(4).setCellValue(transaction.category)
            row.createCell(5).setCellValue(transaction.type.name)
            row.createCell(6).setCellValue(transaction.paymentMethod)
            row.createCell(7).setCellValue(transaction.note)
        }

        val columnWidths = intArrayOf(8, 20, 30, 14, 20, 12, 20, 30)
        columnWidths.forEachIndexed { index, width ->
            sheet.setColumnWidth(index, width * 256)
        }

        val fileName = "SmartExpense_Report_${System.currentTimeMillis()}.xlsx"
        val file = File(context.cacheDir, fileName)

        return try {
            val fileOut = FileOutputStream(file)
            workbook.write(fileOut)
            fileOut.close()
            workbook.close()
            
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun shareFile(context: Context, uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Report"))
    }

    fun importExcelReport(context: Context, uri: Uri): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        
        inputStream?.use { 
            val workbook = XSSFWorkbook(it)
            val sheet = workbook.getSheetAt(0)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

            for (rowIndex in 1..sheet.lastRowNum) {
                val row = sheet.getRow(rowIndex) ?: continue
                try {
                    val dateStr = row.getCell(1)?.stringCellValue ?: ""
                    val date = dateFormat.parse(dateStr) ?: Date()
                    val title = row.getCell(2)?.stringCellValue ?: "Imported"
                    val amount = row.getCell(3)?.numericCellValue ?: 0.0
                    val category = row.getCell(4)?.stringCellValue ?: "Other"
                    val typeStr = row.getCell(5)?.stringCellValue ?: "EXPENSE"
                    val type = try { TransactionType.valueOf(typeStr) } catch (e: Exception) { TransactionType.EXPENSE }
                    val paymentMethod = row.getCell(6)?.stringCellValue ?: "Manual"
                    val note = row.getCell(7)?.stringCellValue ?: ""

                    transactions.add(
                        Transaction(
                            title = title,
                            amount = amount,
                            category = category,
                            date = date,
                            paymentMethod = paymentMethod,
                            note = note,
                            type = type
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            workbook.close()
        }
        return transactions
    }
}
