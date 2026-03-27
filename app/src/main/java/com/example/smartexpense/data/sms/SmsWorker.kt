package com.example.smartexpense.data.sms

import android.content.Context
import android.net.Uri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.smartexpense.domain.repository.TransactionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class SmsWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: TransactionRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {

            val lastSyncedTime = 0L 
            val newSyncedTime = System.currentTimeMillis()

            val cursor = applicationContext.contentResolver.query(
                Uri.parse("content:
                arrayOf("address", "body", "date"),
                "date > ?",
                arrayOf(lastSyncedTime.toString()),
                "date DESC"
            )

            cursor?.use {
                val addressIdx = it.getColumnIndex("address")
                val bodyIdx = it.getColumnIndex("body")
                val dateIdx = it.getColumnIndex("date")

                while (it.moveToNext()) {
                    val address = it.getString(addressIdx)
                    val body = it.getString(bodyIdx)
                    val date = it.getLong(dateIdx)

                    val transaction = SmsParser.parseSms(address, body, date)
                    if (transaction != null) {

                        repository.insertTransaction(transaction)
                    }
                }
            }
            

            
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}
