package com.example.kanakupulla

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expensesmanager.model.Data
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class IncomeFragment : Fragment() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mIncomeDatabase: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: IncomeAdapter
    private lateinit var mType: TextView
    private lateinit var mNote: TextView
    private lateinit var mDate: TextView
    private lateinit var mAmount: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_income, container, false)

        mAuth = FirebaseAuth.getInstance()
        val currentUser: FirebaseUser? = mAuth.currentUser
        val uid: String = currentUser?.uid ?: ""

        mIncomeDatabase = FirebaseDatabase.getInstance().reference.child("IncomeData").child(uid)
        recyclerView = view.findViewById(R.id.recycler_id_income)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        adapter = IncomeAdapter(emptyList())

        val headerView = inflater.inflate(R.layout.income_recycler_data, recyclerView, false)
        // Initialize TextViews
        mType = headerView.findViewById(R.id.type_txt_income)
        mNote = headerView.findViewById(R.id.note_txt_income)
        mDate = headerView.findViewById(R.id.date_txt_income)
        mAmount = headerView.findViewById(R.id.ammount_txt_income)

        // Inflate the layout containing the header view


        // Set data to header view if needed

        // Add the header view to the adapter
        adapter.addHeaderView(headerView)
        recyclerView.adapter = adapter

        fetchDataFromFirebase()

        return view
    }


    private fun fetchDataFromFirebase() {
        mIncomeDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val dataList = mutableListOf<Data>()
                for (snapshot in dataSnapshot.children) {
                    val type = snapshot.child("type").getValue(String::class.java) ?: ""
                    val note = snapshot.child("note").getValue(String::class.java) ?: ""
                    val date = snapshot.child("date").getValue(String::class.java) ?: ""
                    val amount = snapshot.child("amount").getValue(Int::class.java) ?: 0
                    val data = Data(amount, type, note, date)
                    dataList.add(data)
                }
                adapter.setData(dataList)

                // Update TextViews with latest data
                if (dataList.isNotEmpty()) {
                    val latestData = dataList.last()
                    Log.d("IncomeFragment", "Latest data: $latestData")
                    mType.text = latestData.type
                    mNote.text = latestData.note
                    mDate.text = latestData.date
                    mAmount.text = latestData.amount.toString()
                } else {
                    Log.d("IncomeFragment", "Data list is empty")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("IncomeFragment", "Error fetching data: $databaseError")
                // Handle errors
            }
        })
    }

}
