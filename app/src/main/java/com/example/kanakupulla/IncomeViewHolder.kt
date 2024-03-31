package com.example.kanakupulla

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.expensesmanager.model.Data

class IncomeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val mType: TextView = itemView.findViewById(R.id.type_txt_income)
    private val mNote: TextView = itemView.findViewById(R.id.note_txt_income)
    private val mDate: TextView = itemView.findViewById(R.id.date_txt_income)
    private val mAmount: TextView = itemView.findViewById(R.id.ammount_txt_income)

    fun bind(data: Data) {
        mType.text = data.type
        mNote.text = data.note
        mDate.text = data.date
        mAmount.text = data.amount.toString()
    }
}
