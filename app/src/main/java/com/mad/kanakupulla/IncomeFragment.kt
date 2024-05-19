package com.mad.kanakupulla

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import java.util.Calendar
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mad.expensesmanager.model.Data
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Locale

class IncomeFragment : Fragment() ,IncomeItemClickListener{
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mIncomeDatabase: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: IncomeAdapter
    private lateinit var mType: TextView
    private lateinit var mNote: TextView
    private lateinit var mDate: TextView
    private lateinit var mAmount: TextView
    private lateinit var mTotalAmountTextView: TextView
    private lateinit var editType:Spinner
    private var selectedMonth: String? = null



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_income, container, false)
        mTotalAmountTextView = view.findViewById(R.id.income_txt_result)

        mAuth = FirebaseAuth.getInstance()
        val currentUser: FirebaseUser? = mAuth.currentUser
        val uid: String = currentUser?.uid ?: ""

        mIncomeDatabase = FirebaseDatabase.getInstance().reference.child("IncomeData")

        recyclerView = view.findViewById(R.id.recycler_id_income)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        val emptyMutableList = mutableListOf<Data>()
        adapter = IncomeAdapter(emptyMutableList,this)

        val headerView = inflater.inflate(R.layout.income_recycler_data, recyclerView, false)
        mType = headerView.findViewById(R.id.type_txt_income)
        mNote = headerView.findViewById(R.id.note_txt_income)
        mDate = headerView.findViewById(R.id.date_txt_income)
        mAmount = headerView.findViewById(R.id.ammount_txt_income)
        adapter.addHeaderView(headerView)
        recyclerView.adapter = adapter






        val incometype = view?.findViewById<Spinner>(R.id.incomeType)
        val incomemonth = view?.findViewById<Spinner>(R.id.incomemonth)

        val typeAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.Spinner_items, // Array resource containing income type names
            android.R.layout.simple_spinner_item
        )
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        incometype?.adapter = typeAdapter


// For Month Spinner
        val monthAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.Months, // Array resource containing month names
            android.R.layout.simple_spinner_item
        )
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        incomemonth?.adapter = monthAdapter

// For Year Spinner


        var type = incometype?.selectedItem?.toString()?.trim() ?: ""
        var month = incomemonth?.selectedItem?.toString()?.trim()?: ""
        var year=    SimpleDateFormat("yyyy", Locale.getDefault()).format(Calendar.getInstance().time)


// If the selected month is empty, use the current month
        month = if (month == "Select") {
            SimpleDateFormat("MMM", Locale.getDefault()).format(Calendar.getInstance().time)
        } else {
            month
        }

        fetchDataFromFirebase(type,month,year)

        val refresh=view?.findViewById<ImageView>(R.id.refreshh)
        refresh?.setOnClickListener{


            type = incometype?.selectedItem?.toString()?.trim() ?: ""

            month = incomemonth?.selectedItem?.toString()?.trim()?: ""
            month = if (month == "Select") {
                SimpleDateFormat("MMM", Locale.getDefault()).format(Calendar.getInstance().time)
            } else {
                month
            }
            fetchDataFromFirebase(type,month,year)
        }

        return view
    }
    override fun onItemClick(data: Data) {
        updateDataItem(data)
    }
    private fun updateTotalAmount(dataList: List<Data>) {
        var totalAmount = 0
        for (data in dataList) {
            val amountInt = data.amount.toIntOrNull() ?: 0
            totalAmount += amountInt
        }
        mTotalAmountTextView.text = "Total Amount: $totalAmount"
    }

    private fun updateDataItem(data: Data){
        val myDialog=AlertDialog.Builder(activity)
        val infalter=LayoutInflater.from(activity)
        val myView=infalter.inflate(R.layout.update_data_item,null)
        myDialog.setView(myView)

        val dialog=myDialog.create()
        val editAmount = myView.findViewById<EditText>(R.id.amount_edt)

        editType = myView.findViewById(R.id.type_edt)
        val typeAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.Spinner_items,
            android.R.layout.simple_spinner_item
        )
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        editType.adapter = typeAdapter
        val selectedtype=data.type
        val spinnerItemsArray = resources.getStringArray(R.array.Spinner_items)
        val selectedIndex = spinnerItemsArray.indexOf(selectedtype)
        if (selectedIndex != -1) {
            editType.setSelection(selectedIndex)
        } else {
            Log.e("ExpenseFragment", "Selected type not found in the list")
        }
        val editNote = myView.findViewById<EditText>(R.id.note_edt)

        editAmount.setText(data.amount.toString())
        editNote.setText(data.note)

        val btndel=myView.findViewById<TextView>(R.id.btnupd_delete)
        val btnUpdate=myView.findViewById<TextView>(R.id.btn_upd_update)
        val dataId=data.id
        val dateId=data.date
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val date = dateFormat.parse(dateId)

        // Format the parsed date to extract month and year separately
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
        val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())

        val yearMonth = monthFormat.format(date) // Extracted month (e.g., "Mar")
        val year = yearFormat.format(date)

        btnUpdate.setOnClickListener{
            val newText = editType.selectedItem.toString()
            val newAmount = editAmount.text.toString().toInt()
            val newNote=editNote.text.toString()
            val currentUser: FirebaseUser? = mAuth.currentUser
            val uid: String = currentUser?.uid ?: ""
 // Format month as "Mar"
            val databaseReference = FirebaseDatabase.getInstance().reference.child("IncomeData").child(year).child(yearMonth).child(uid).child(dataId)
            val newDataMap = HashMap<String, Any>()
            newDataMap["type"] = newText
            newDataMap["amount"] = newAmount
            newDataMap["note"]=newNote

            databaseReference.updateChildren(newDataMap)
                .addOnSuccessListener {
                    Toast.makeText(activity,"Data Updated",Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                .addOnFailureListener { e ->
                    // Handle failure
                    Log.e(TAG, "Error updating data: $e")
                }
        }
        btndel.setOnClickListener{
            val currentUser: FirebaseUser? = mAuth.currentUser
            val uid: String = currentUser?.uid ?: ""
            val databaseReference = FirebaseDatabase.getInstance().reference.child("IncomeData").child(year).child(yearMonth).child(uid).child(dataId)
            databaseReference.removeValue()
                .addOnSuccessListener {
                    dialog.dismiss()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error deleting data: $e")
                }
            dialog.dismiss()
        }
        dialog.show()
    }
    private fun fetchDataFromFirebase(itype:String,yearMonth: String,year:String) {
        val mUser = mAuth.currentUser
        val uid: String = mUser!!.uid

        val dheincomedata = mIncomeDatabase.child(year).child(yearMonth).child(uid)
        Log.d("data passesd","year:$year month:$yearMonth type:$itype")

        dheincomedata.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val dataList = mutableListOf<Data>()
                for (snapshot in dataSnapshot.children) {
                    val id = snapshot.key ?: ""
                    val type = snapshot.child("type").getValue(String::class.java) ?: ""
                    val note = snapshot.child("note").getValue(String::class.java) ?: ""
                    val date = snapshot.child("date").getValue(String::class.java) ?: ""
                    val amount = snapshot.child("amount").getValue(String::class.java) ?: "0" // Default to "0" if amount is null
                    val data = Data(amount, type, note, date,id)
                    if (itype=="Select")
                    {
                        dataList.add(data)
                    }
                    else if (itype==type)
                    {
                        dataList.add(data)
                    }
                    Log.d("IncomeFragment", "Data: $data")
                }
                adapter.setData(dataList)
                updateTotalAmount(dataList)

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
                Log.e("ExpenseFragment", "Error fetching data: $databaseError")
            }
        })
    }


}
