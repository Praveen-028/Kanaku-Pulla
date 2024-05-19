package com.mad.expensesmanager.model
import java.text.SimpleDateFormat
import java.util.Locale

data class Data(
    val amount:String,
    val type: String,
    val note: String,
    val date: String,
    val id: String,
    val mode:String="Offline"
) {
    // No-argument constructor required by Firebase
    constructor() : this("", "", "", "", "")

    fun getParsedAmount(): Float {
        return amount.toFloatOrNull() ?: 0f
    }

    fun getParsedDate(): Long {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return try {
            dateFormat.parse(date)?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    override fun toString(): String {
        return "Data(amount=$amount, type='$type', note='$note', date='$date')"
    }
}
