package com.example.expensesmanager.model
import java.util.Date

data class Data(
    val amount: Int,
    val type: String,
    val note: String,
    val date: String,
    val id: String
) {
    // No-argument constructor required by Firebase
    constructor() : this(0, "", "", "", "")
    override fun toString(): String {
        return "Data(amount=$amount, type='$type', note='$note', date='$date')"
    }
}
