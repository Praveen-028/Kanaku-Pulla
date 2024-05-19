package com.mad.kanakupulla

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mad.expensesmanager.model.Data
interface IncomeItemClickListener {
    fun onItemClick(data: Data)
}

class IncomeAdapter(private var dataList: MutableList<Data>,private val itemClickListener: IncomeItemClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_HEADER = 0
    private val TYPE_ITEM = 1
    private var headerView: View? = null

    fun addHeaderView(headerView: View) {
        this.headerView = headerView
        notifyItemInserted(0)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> HeaderViewHolder(headerView!!)
            TYPE_ITEM -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.income_recycler_data, parent, false)
                MyViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                // Bind header view data here if needed
            }
            is MyViewHolder -> {
                val dataPosition = if (headerView != null) position - 1 else position
                val data = dataList[dataPosition]
                holder.bind(data)
            }
        }
    }

    override fun getItemCount(): Int {
        return (dataList.size) + if (headerView != null) 1 else 0
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0 && headerView != null) {
            TYPE_HEADER
        } else {
            TYPE_ITEM
        }
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),View.OnClickListener {
        private val mType: TextView = itemView.findViewById(R.id.type_txt_income)
        private val mNote: TextView = itemView.findViewById(R.id.note_txt_income)
        private val mDate: TextView = itemView.findViewById(R.id.date_txt_income)
        private val mAmount: TextView = itemView.findViewById(R.id.ammount_txt_income)
        private var dataId: String = ""

        fun bind(data: Data) {
            dataId = data.id
            mType.text = data.type
            mNote.text = data.note
            mDate.text = data.date
            mAmount.text = data.amount.toString()
        }
        init {
            itemView.setOnClickListener(this)
        }
        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val data = dataList[position-1]
                itemClickListener.onItemClick(data) // Invoke the callback method
            }
        }

    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mType: TextView = itemView.findViewById(R.id.type_txt_income)
        private val mNote: TextView = itemView.findViewById(R.id.note_txt_income)
        private val mDate: TextView = itemView.findViewById(R.id.date_txt_income)
        private val mAmount: TextView = itemView.findViewById(R.id.ammount_txt_income)
    }

    fun setData(newData: List<Data>) {
        dataList.clear()
        dataList.addAll(newData)
        // Add new data
        notifyDataSetChanged()
    }

}



