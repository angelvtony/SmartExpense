package com.example.smartexpense.data.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.example.smartexpense.domain.repository.TransactionRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: TransactionRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            messages?.forEach { sms ->
                val sender = sms.originatingAddress ?: return@forEach
                val body = sms.messageBody ?: return@forEach
                val timestamp = sms.timestampMillis

                
                val transaction = SmsParser.parseSms(sender, body, timestamp)
                if (transaction != null) {
                    val pendingResult = goAsync()
                    scope.launch {
                        try {
                            repository.insertTransaction(transaction)
                        } finally {
                            pendingResult.finish()
                        }
                    }
                }
            }
        }
    }
}
