package com.example.smartexpense.data.sms

import com.example.smartexpense.data.local.entity.Transaction
import com.example.smartexpense.data.local.entity.TransactionType
import java.util.Date
import java.util.regex.Pattern

object SmsParser {

    fun parseSms(sender: String, body: String, timestamp: Long): Transaction? {

        if (sender.length < 3 || sender.all { it.isDigit() }) return null

        val lowerBody = body.lowercase()
        val isDebit = lowerBody.contains("debited") || lowerBody.contains("spent") || lowerBody.contains("paid")
        val isCredit = lowerBody.contains("credited") || lowerBody.contains("received") || lowerBody.contains("deposited")

        if (!isDebit && !isCredit) return null

        val amount = extractAmount(body) ?: return null
        

        val merchant = extractMerchant(body, isDebit)
        
        val category = if (isDebit) getCategoryForMerchant(merchant, body) else "Income"

        return Transaction(
            title = merchant,
            amount = amount,
            category = category,
            date = Date(timestamp),
            paymentMethod = "SMS Auto-Import",
            note = "Imported from SMS: $body",
            type = if (isDebit) TransactionType.EXPENSE else TransactionType.INCOME
        )
    }

    private fun extractAmount(body: String): Double? {

        val pattern = Pattern.compile("(?i)(?:rs\\.?|inr)\\s*([0-9,]+(?:\\.[0-9]{2})?)")
        val matcher = pattern.matcher(body)
        if (matcher.find()) {
            return matcher.group(1)?.replace(",", "")?.toDoubleOrNull()
        }
        return null
    }

    private fun extractMerchant(body: String, isDebit: Boolean): String {

        val pattern = if (isDebit) {
            Pattern.compile("(?i)(?:at|to|for)\\s+([A-Za-z0-9\\s]+?)(?:\\.|\\s|$)")
        } else {
            Pattern.compile("(?i)(?:from|by)\\s+([A-Za-z0-9\\s]+?)(?:\\.|\\s|$)")
        }
        val matcher = pattern.matcher(body)
        if (matcher.find()) {
            return matcher.group(1)?.trim() ?: "Unknown"
        }
        return "Unknown Transaction"
    }
    private fun getCategoryForMerchant(merchant: String, body: String): String {
        val lowerMerchant = merchant.lowercase()
        val lowerBody = body.lowercase()

        return when {
            lowerMerchant.contains("zomato") || lowerMerchant.contains("swiggy") || lowerBody.contains("restaurant") || lowerBody.contains("food") -> "Food & Dining"
            lowerMerchant.contains("uber") || lowerMerchant.contains("ola") || lowerBody.contains("petrol") || lowerBody.contains("fuel") -> "Transport"
            lowerMerchant.contains("amazon") || lowerMerchant.contains("flipkart") || lowerBody.contains("shopping") -> "Shopping"
            lowerMerchant.contains("netflix") || lowerMerchant.contains("prime") || lowerMerchant.contains("spotify") -> "Subscriptions"
            lowerBody.contains("electricity") || lowerBody.contains("water") || lowerBody.contains("gas") || lowerBody.contains("rent") -> "Bills & Utilities"
            else -> "General"
        }
    }
}
