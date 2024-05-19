// UnpaidAdapter.kt
package com.mad.kanakupulla

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mad.expensesmanager.model.UnData

class UnpaidAdapter(private val listener: OnItemClickListener) : ListAdapter<UnData, UnpaidAdapter.UnpaidViewHolder>(UnpaidDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UnpaidViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.uhpaid_data_cycler, parent, false)
        return UnpaidViewHolder(view, listener)
    }

    override fun onBindViewHolder(holder: UnpaidViewHolder, position: Int) {
        val currentUnpaid = getItem(position)
        holder.bind(currentUnpaid)
    }

    inner class UnpaidViewHolder(itemView: View, private val listener: OnItemClickListener) : RecyclerView.ViewHolder(itemView) {
        private val amountTextView: TextView = itemView.findViewById(R.id.ammount_txt_unpaid)
        private val typeTextView: TextView = itemView.findViewById(R.id.type_txt_unpaid)
        private val noteTextView: TextView = itemView.findViewById(R.id.note_txt_unpaid)
        private val towhatTextView:TextView=itemView.findViewById(R.id.towhat_txt_unpaid)

        fun bind(undata: UnData) {
            amountTextView.text = undata.ourAmountInt.toString()
            typeTextView.text = undata.type
            noteTextView.text = undata.note
            towhatTextView.text=undata.to

            if (undata.to == "Income") {
                amountTextView.setTextColor(Color.GREEN)
                towhatTextView.setTextColor(Color.GREEN)
            } else {
                amountTextView.setTextColor(Color.RED)
                towhatTextView.setTextColor(Color.RED)
            }

            // Set click listener for the whole item view
            itemView.setOnClickListener {
                listener.onItemClick(undata)
            }
        }
    }

    private class UnpaidDiffCallback : DiffUtil.ItemCallback<UnData>() {
        override fun areItemsTheSame(oldItem: UnData, newItem: UnData): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: UnData, newItem: UnData): Boolean {
            return (oldItem == newItem)
        }
    }
}
