package com.mad.kanakupulla

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.telephony.SmsMessage
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SmsReceiver : BroadcastReceiver() {

    private lateinit var database: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var uid: String

    override fun onReceive(context: Context, intent: Intent) {
        mAuth = FirebaseAuth.getInstance()
        val currentUser: FirebaseUser? = mAuth.currentUser
        uid = currentUser?.uid ?: ""

        if (uid.isEmpty()) {
            Log.e("SmsReceiver", "User not authenticated")
            return
        }

        database = FirebaseDatabase.getInstance().getReference()

        val bundle = intent.extras
        if (bundle != null) {
            try {
                val pdus = bundle.get("pdus") as Array<*>
                val msgs = Array(pdus.size) { i -> SmsMessage.createFromPdu(pdus[i] as ByteArray) }
                for (msg in msgs) {
                    val msgBody = msg.messageBody.toString()
                    val originatingAddress = msg.originatingAddress

                    // Log all messages for debugging purposes
                    Log.d("SmsReceiver", "SMS from: $originatingAddress : $msgBody")

                    val accref = database.child("Users").child(uid).child("AccNo")
                    accref.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val accountNumber = dataSnapshot.getValue(String::class.java)
                            if (accountNumber != null) {
                                processSms(msgBody, accountNumber)
                            } else {
                                Log.e("SmsReceiver", "Account number not found for user: $uid")
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            Log.e("SmsReceiver", "Failed to read account number", databaseError.toException())
                        }
                    })
                }
            } catch (e: Exception) {
                Log.e("SmsReceiver", "Exception caught", e)
            }
        }
    }

    private fun processSms(msgBody: String, userAccountNumber: String) {
        if (matchesAccountNumber(msgBody, userAccountNumber)) {
            val amountString = extractAmount(msgBody)
            val amount: Int = amountString.toIntOrNull()?:0
            val currentDate = getCurrentDate()
            val name = extractName(msgBody)

            val transactionId = database.push().key.toString()
            val transactionData = TransactionData(amountString, currentDate, name, transactionId)

            if (isCreditTransaction(msgBody)) {
                Log.d("SmsReceiver", "Income: $amount, Date: $currentDate, From: $name")
                saveTransaction("IncomeData", transactionId, transactionData)
            } else if (isDebitTransaction(msgBody)) {
                Log.d("SmsReceiver", "Expense: $amount, Date: $currentDate, To: $name")
                saveTransaction("ExpenseData", transactionId, transactionData)
            }
            Log.d("SmsReceiver", "Bank Transaction SMS: $msgBody")
        }
    }

    private fun isCreditTransaction(message: String): Boolean {
        val creditKeywords = listOf("credited", "deposit", "received")
        val isCredit = creditKeywords.any { message.contains(it, ignoreCase = true) }
        Log.d("SmsReceiver", "isCreditTransaction: message=\"$message\", isCredit=$isCredit")
        return isCredit
    }

    private fun isDebitTransaction(message: String): Boolean {
        val debitKeywords = listOf("debited", "withdrawn", "spent")
        val isDebit = debitKeywords.any { message.contains(it, ignoreCase = true) }
        Log.d("SmsReceiver", "isDebitTransaction: message=\"$message\", isDebit=$isDebit")
        return isDebit
    }

    private fun matchesAccountNumber(message: String, userAccountNumber: String): Boolean {
        val regex = Regex("""a/c\s+XXXXXXXXXXXX(\d{4})""")
        val matchResult = regex.find(message)
        val accountNumber = matchResult?.groupValues?.get(1)
        val matches = accountNumber != null && userAccountNumber.endsWith(accountNumber)
        Log.d("SmsReceiver", "matchesAccountNumber: accountNumber=$accountNumber, matches=$matches")
        return matches
    }

    private fun extractAmount(messageBody: String): String {
        val regex = Regex("""(?:Rs\.?|rs\.?|(?i)RS)\s*(\d+(?:\.\d+)?)""")
        val matchResult = regex.find(messageBody)
        val amountString = matchResult?.groupValues?.get(1) ?: "0" // Default value if no match found
        val amount = amountString.split(".")[0] // Extract only the integer part
        Log.d("SmsReceiver", "extractAmount: messageBody=\"$messageBody\", amountString=$amount")
        return amount.trim()
    }


    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        Log.d("SmsReceiver", "getCurrentDate: currentDate=$currentDate")
        return currentDate
    }

    private fun extractName(messageBody: String): String {
        val creditRegex = Regex("""credited\s+Rs\..+from\s+([a-zA-Z\s]+)\s+on""")
        val debitRegex = Regex("""debited\s+Rs\..+to\s+([a-zA-Z\s]+)\s+info""")
        val creditMatch = creditRegex.find(messageBody)
        val debitMatch = debitRegex.find(messageBody)
        val name = creditMatch?.groupValues?.get(1)?.trim()
            ?: debitMatch?.groupValues?.get(1)?.trim()
            ?: "Unknown"
        Log.d("SmsReceiver", "extractName: messageBody=\"$messageBody\", name=$name")
        return name
    }

    private fun saveTransaction(type: String, transactionId: String, data: TransactionData) {
        val month = SimpleDateFormat("MMM", Locale.getDefault()).format(Date()).toString()
        val year = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date()).toString()


        database.child(type).child(year).child(month).child(uid).child(transactionId).setValue(data)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("Data", "Added success")
                } else {
                    Log.e("data", "added ", task.exception)
                }
            }
        Log.d("SmsReceiver", "saveTransaction: type=$type, transactionId=$transactionId, data=$data")
    }

    companion object {
        fun enableSmsReading(context: Context) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            sharedPreferences.edit().putBoolean("sms_reading_enabled", true).apply()
            Log.d("SmsReceiver", "SMS reading enabled")
        }

        fun disableSmsReading(context: Context) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            sharedPreferences.edit().putBoolean("sms_reading_enabled", false).apply()
            Log.d("SmsReceiver", "SMS reading disabled")
        }

        fun isSmsReadingEnabled(context: Context): Boolean {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            return sharedPreferences.getBoolean("sms_reading_enabled", false)
        }
    }
}

data class TransactionData(
    val amount: String,
    val date: String,
    val note: String,
    val id: String,
    val mode: String = "Online",
    val type: String = "Others"
)
