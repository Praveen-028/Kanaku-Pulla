package com.example.kanakupulla

import android.app.AlertDialog
import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expensesmanager.model.Data
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ExpenseFragment : Fragment(),ExpenseItemClickListener {
    // Declare necessary variables and views
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mExpenseDatabase: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ExpenseAdapter
    private lateinit var mType: TextView
    private lateinit var mNote: TextView
    private lateinit var mDate: TextView
    private lateinit var mAmount: TextView
    private lateinit var mTotalAmountTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_expense, container, false)
        mTotalAmountTextView = view.findViewById(R.id.expense_txt_result)

        mAuth = FirebaseAuth.getInstance()
        val currentUser: FirebaseUser? = mAuth.currentUser
        val uid: String = currentUser?.uid ?: ""

        mExpenseDatabase = FirebaseDatabase.getInstance().reference.child("ExpenseData").child(uid)
        recyclerView = view.findViewById(R.id.recycler_id_expense)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        val emptyMutableList = mutableListOf<Data>()

        adapter = ExpenseAdapter(emptyMutableList,this)

        val headerView = inflater.inflate(R.layout.expense_recycler_data, recyclerView, false)
        // Initialize TextViews
        mType = headerView.findViewById(R.id.type_txt_expense)
        mNote = headerView.findViewById(R.id.note_txt_expense)
        mDate = headerView.findViewById(R.id.date_txt_expense)
        mAmount = headerView.findViewById(R.id.ammount_txt_expense)

        // Add the header view to the adapter
        adapter.addHeaderView(headerView)
        recyclerView.adapter = adapter

        fetchDataFromFirebase()

        return view
    }
    override fun onItemClick(data: Data) {
        updateDataItem(data)
        // Implement your logic here when a RecyclerView item is clicked
        // This method will be called from the adapter
        // You can invoke the method from another Kotlin file here
    }

    // Update the method to calculate and display the total amount
    private fun updateTotalAmount(dataList: List<Data>) {
        var totalAmount = 0
        for (data in dataList) {
            totalAmount += data.amount
        }
        mTotalAmountTextView.text = "Total Amount: $totalAmount" // Display the total amount
    }
    private fun updateDataItem(data: Data){
        val myDialog= AlertDialog.Builder(activity)
        val infalter=LayoutInflater.from(activity)
        val myView=infalter.inflate(R.layout.update_data_item,null)
        myDialog.setView(myView)

        val dialog=myDialog.create()
        val editAmount = myView.findViewById<EditText>(R.id.amount_edt)
        val editType = myView.findViewById<EditText>(R.id.type_edt)
        val editNote = myView.findViewById<EditText>(R.id.note_edt)

        editAmount.setText(data.amount.toString())
        editType.setText(data.type)
        editNote.setText(data.note)

        val btndel=myView.findViewById<TextView>(R.id.btnupd_delete)
        val btnUpdate=myView.findViewById<TextView>(R.id.btn_upd_update)
        val dataId=data.id

        btnUpdate.setOnClickListener{
            val newText = editType.text.toString()
            val newAmount = editAmount.text.toString().toInt()
            val newNote=editNote.text.toString()// Assuming amount is an integer
            // Update data in Firebase Realtime Database
            val currentUser: FirebaseUser? = mAuth.currentUser
            val uid: String = currentUser?.uid ?: ""
            val databaseReference = FirebaseDatabase.getInstance().reference.child("ExpenseData").child(uid).child(dataId)
            val newDataMap = HashMap<String, Any>()
            newDataMap["type"] = newText
            newDataMap["amount"] = newAmount
            newDataMap["note"]=newNote

            databaseReference.updateChildren(newDataMap)
                .addOnSuccessListener {
                    // Data updated successfully
                    Toast.makeText(activity,"Data Updated", Toast.LENGTH_SHORT).show()
                    dialog.dismiss() // Dismiss dialog after successful update
                }
                .addOnFailureListener { e ->
                    // Handle failure
                    Log.e(ContentValues.TAG, "Error updating data: $e")
                }
        }
        btndel.setOnClickListener{
            val currentUser: FirebaseUser? = mAuth.currentUser
            val uid: String = currentUser?.uid ?: ""
            val databaseReference = FirebaseDatabase.getInstance().reference.child("ExpenseData").child(uid).child(dataId)
            databaseReference.removeValue()
                .addOnSuccessListener {
                    // Data deleted successfully
                    dialog.dismiss() // Dismiss dialog after successful deletion
                }
                .addOnFailureListener { e ->
                    // Handle failure
                    Log.e(ContentValues.TAG, "Error deleting data: $e")
                }
            dialog.dismiss()
        }
        dialog.show()
    }
    private fun fetchDataFromFirebase() {
        mExpenseDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val dataList = mutableListOf<Data>()
                for (snapshot in dataSnapshot.children) {
                    val id = snapshot.key ?: ""
                    val type = snapshot.child("type").getValue(String::class.java) ?: ""
                    val note = snapshot.child("note").getValue(String::class.java) ?: ""
                    val date = snapshot.child("date").getValue(String::class.java) ?: ""
                    val amount = snapshot.child("amount").getValue(Int::class.java) ?: 0
                    val data = Data(amount, type, note, date,id)
                    dataList.add(data)
                    Log.d("ExpenseFragment", "Data: $data")
                }
                adapter.setData(dataList)
                updateTotalAmount(dataList)

                // Update TextViews with latest data
                if (dataList.isNotEmpty()) {
                    val latestData = dataList.last()
                    Log.d("ExpenseFragment", "Latest data: $latestData")
                    mType.text = latestData.type
                    mNote.text = latestData.note
                    mDate.text = latestData.date
                    mAmount.text = latestData.amount.toString()
                } else {
                    Log.d("ExpenseFragment", "Data list is empty")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("ExpenseFragment", "Error fetching data: $databaseError")
                // Handle errors
            }
        })
    }
}
