package com.mad.kanakupulla

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mad.expensesmanager.model.Data
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private lateinit var mAuth: FirebaseAuth
private lateinit var mIncomeDatabase: DatabaseReference
private lateinit var mExpenseDatabase: DatabaseReference

class DashboardFragment  : Fragment() {
    private lateinit var fab_main_btn: FloatingActionButton
    private lateinit var fab_income_btn: FloatingActionButton
    private lateinit var fab_expense_btn: FloatingActionButton
    private lateinit var fab_income_txt: TextView
    private lateinit var fab_expense_txt: TextView
    private lateinit var budget_ft_text:TextView
    private lateinit var budget_ft_btn:FloatingActionButton
    private var isOpen = false
    private lateinit var FadOpen: Animation
    private lateinit var FadClose: Animation
    private lateinit var totalIncomeTextView: TextView
    private lateinit var totalExpenseTextView: TextView
    private lateinit var totalBudgetView:TextView
    private lateinit var IncomeRecyler:RecyclerView
    private lateinit var ExpenseRecyler:RecyclerView
    private lateinit var editType: Spinner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val myview = inflater.inflate(R.layout.fragment_dashboard, container, false)

        mAuth=FirebaseAuth.getInstance()

        val mUser = mAuth.currentUser
        val uid:String = mUser!!.uid

        mIncomeDatabase = FirebaseDatabase.getInstance().getReference().child("IncomeData")
        mExpenseDatabase = FirebaseDatabase.getInstance().getReference().child("ExpenseData")

        IncomeRecyler=myview.findViewById(R.id.incomeRecycler)
        ExpenseRecyler=myview.findViewById(R.id.expenseRecycler)

        IncomeRecyler.layoutManager=LinearLayoutManager(activity,LinearLayoutManager.HORIZONTAL, false)
        ExpenseRecyler.layoutManager=LinearLayoutManager(activity,LinearLayoutManager.HORIZONTAL, false)

        fab_main_btn = myview.findViewById(R.id.fb_main_plus_btn)
        fab_income_btn = myview.findViewById(R.id.income_ft_btn)
        fab_expense_btn = myview.findViewById(R.id.expense_ft_btn)
        budget_ft_btn=myview.findViewById(R.id.budget_ft_btn)

        fab_income_txt = myview.findViewById(R.id.expense_ft_text)
        fab_expense_txt = myview.findViewById(R.id.income_ft_text)
        budget_ft_text=myview.findViewById(R.id.budget_ft_text)

        FadOpen = AnimationUtils.loadAnimation(requireActivity(), R.anim.fade_open)
        FadClose = AnimationUtils.loadAnimation(requireActivity(), R.anim.fade_close)

        totalIncomeTextView=myview.findViewById(R.id.income_set_result)
        totalExpenseTextView=myview.findViewById(R.id.expense_set_result)
        totalBudgetView=myview.findViewById(R.id.Budget)
        fab_main_btn.setOnClickListener {
            addData()
            if (isOpen) {
                closeMenu()
            } else {
                openMenu()
            }
        }
        val currentDate = Calendar.getInstance().time
        val year:String=SimpleDateFormat("YYYY",Locale.getDefault()).format(currentDate)
        val yearMonth: String = SimpleDateFormat("MMM", Locale.getDefault()).format(currentDate) // Format month as "Mar"

        val dheincomedata = mIncomeDatabase.child(year).child(yearMonth).child(uid)
        val dheexpensedata = mExpenseDatabase.child(year).child(yearMonth).child(uid)
        var totalExpense = 0
        var totalIncome = 0

        dheincomedata.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val amount = snapshot.child("amount").getValue(String::class.java)?.toIntOrNull() ?: 0
                    totalIncome += amount
                }
                totalIncomeTextView.text = "$totalIncome"
            }

            override fun onCancelled(error: DatabaseError) {
                val errorMessage = "Database error: ${error.message}"
                Log.e("DashboardFragment", errorMessage)
                // Show a toast message
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
        })
        dheexpensedata.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (snapshot in dataSnapshot.children) {
                    val amount = snapshot.child("amount").getValue(String::class.java)?.toIntOrNull() ?: 0
                    totalExpense += amount
                }
                totalExpenseTextView.text = "$totalExpense"
            }

            override fun onCancelled(error: DatabaseError) {
                val errorMessage = "Database error: ${error.message}"
                Log.e("DashboardFragment", errorMessage)
                // Show a toast message
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
        })
        val databaseReference = FirebaseDatabase.getInstance().reference
        val currentMonth = SimpleDateFormat("MM", Locale.getDefault()).format(Date())
        val currentYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())
        val budgetReference = databaseReference.child("Budget").child(uid).child(currentYear).child(currentMonth)
        budgetReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val budgetString = dataSnapshot.getValue(String::class.java) ?: ""

                // Convert the budget string to an integer
                val budget = budgetString.toIntOrNull() ?: 0
                val rem=budget-totalExpense
                totalBudgetView.text="$rem"// Default to 0 if conversion fails

                // Now you can use the budget integer value
                // ...
            }

            override fun onCancelled(error: DatabaseError) {
                val errorMessage = "Database error: ${error.message}"
                Log.e("DashboardFragment", errorMessage)
                // Show a toast message
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
        })

        // Fetch income data and create adapter
        dheincomedata.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val incomeList = mutableListOf<Data>()
                for (snapshot in dataSnapshot.children) {
                    val data = snapshot.getValue(Data::class.java)
                    data?.let { incomeList.add(it) }
                }
                val incomeAdapter = dsIncomeAdapter(incomeList)
                IncomeRecyler.adapter = incomeAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                val errorMessage = "Database error: ${error.message}"
                Log.e("DashboardFragment", errorMessage)
                // Show a toast message
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()            }
        })

// Fetch expense data and create adapter
        dheexpensedata.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val expenseList = mutableListOf<Data>()
                for (snapshot in dataSnapshot.children) {
                    val data = snapshot.getValue(Data::class.java)
                    data?.let { expenseList.add(it) }
                }
                val expenseAdapter = dsExpenseAdapter(expenseList)
                ExpenseRecyler.adapter = expenseAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })


        return myview

    }

    private fun openMenu() {
        fab_income_btn.startAnimation(FadOpen)
        fab_expense_btn.startAnimation(FadOpen)
        budget_ft_btn.startAnimation(FadOpen)
        fab_income_btn.isClickable = true
        fab_expense_btn.isClickable = true

        fab_income_txt.startAnimation(FadOpen)
        budget_ft_text.startAnimation(FadOpen)
        fab_expense_txt.startAnimation(FadOpen)
        fab_income_txt.isClickable = true
        fab_expense_txt.isClickable = true
        budget_ft_btn.isClickable=true
        budget_ft_text.isClickable=true

        isOpen = true
    }

    private fun closeMenu() {
        fab_income_btn.startAnimation(FadClose)
        fab_expense_btn.startAnimation(FadClose)
        budget_ft_text.startAnimation(FadClose)
        budget_ft_btn.startAnimation(FadClose)
        budget_ft_btn.isClickable=false
        budget_ft_text.isClickable=false


        fab_income_btn.isClickable = false
        fab_expense_btn.isClickable = false

        fab_income_txt.startAnimation(FadClose)
        fab_expense_txt.startAnimation(FadClose)
        fab_income_txt.isClickable = false
        fab_expense_txt.isClickable = false

        isOpen = false
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DashboardFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

    }
    private fun ftAnimation() {
        if(isOpen)
        {
            closeMenu()
        }
        else {
            openMenu()
        }
    }
    private fun addData() {
        fab_income_btn.setOnClickListener {
            incomeDataInsert()
        }

        fab_expense_btn.setOnClickListener {
            expenseDataInsert()
        }
        budget_ft_btn.setOnClickListener {
            // Create an AlertDialog with an EditText
            val builder = AlertDialog.Builder(activity)
            builder.setTitle("Enter Budget")

            // Set up the input
            val input = EditText(activity)
            input.inputType = InputType.TYPE_CLASS_NUMBER
            builder.setView(input)

            // Set up the buttons
            builder.setPositiveButton("OK") { dialog, _ ->
                val budget = input.text.toString().trim()

                // Get the current month and year
                val currentMonth = SimpleDateFormat("MM", Locale.getDefault()).format(Date())
                val currentYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())

                // Store the budget in Firebase Realtime Database under the user's UID for the current month and year
                val currentUser: FirebaseUser? = mAuth.currentUser
                val uid: String = currentUser?.uid ?: ""

                val databaseReference = FirebaseDatabase.getInstance().reference
                databaseReference.child("Budget").child(uid).child(currentYear).child(currentMonth).setValue(budget)
                    .addOnSuccessListener {
                        Toast.makeText(activity, "Budget saved successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(activity, "Failed to save budget: ${e.message}", Toast.LENGTH_SHORT).show()
                    }

                dialog.dismiss()
            }
            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }

            builder.show()
        }

    }
    fun incomeDataInsert() {
        val myDialog = AlertDialog.Builder(activity)
        val inflater = LayoutInflater.from(activity)
        val myView = inflater.inflate(R.layout.custom_layout_for_insertdata, null)
        myDialog.setView(myView)

        val dialog = myDialog.create()
        val editAmount = myView.findViewById<EditText>(R.id.amount_edt)
        editType = myView.findViewById(R.id.type_edt)
        val typeAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.Spinner_items, // Array resource containing Spinner items
            android.R.layout.simple_spinner_item
        )
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        editType.adapter = typeAdapter


        val editNote = myView.findViewById<EditText>(R.id.note_edt)

        val btnSave = myView.findViewById<TextView>(R.id.btnSave)
        val btnCancel = myView.findViewById<TextView>(R.id.btnCancel)

        btnSave.setOnClickListener {
            val type = editType.selectedItem.toString().trim()
            if (type == "Select") {
                // Display an error message on the spinner
                (editType.selectedView as? TextView)?.error = "Please select a correct value"
                return@setOnClickListener
            } else {
                // Clear error message on the spinner if it was previously set
                (editType.selectedView as? TextView)?.error = null
            }
            val amount = editAmount.text.toString().trim()
            val note = editNote.text.toString().trim()

            if (amount.isEmpty()) {
                editAmount.error = "Required Field.."
                return@setOnClickListener
            }

            val ourAmountInt = amount.toInt() // Throws NumberFormatException if parsing fails
            if (note.isEmpty()) {
                editNote.error = "Required Field.."
                return@setOnClickListener
            }

            val mUser = mAuth.currentUser
            val uid: String = mUser!!.uid
            val expenseId: String = mIncomeDatabase.child("IncomeData").push().key!!

            // Get current date
            val currentDate = Calendar.getInstance().time
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val formattedDate: String = dateFormat.format(currentDate)
            val year:String=SimpleDateFormat("YYYY",Locale.getDefault()).format(currentDate)
            val yearMonth: String = SimpleDateFormat("MMM", Locale.getDefault()).format(currentDate) // Format month as "Mar"

            val data = Data(ourAmountInt.toString(), type, note, formattedDate, expenseId) // Save date in "Mar 05, 2024" format

            // Save the data under the current user's year/month
            mIncomeDatabase.child(year).child(yearMonth).child(uid).child(expenseId).setValue(data)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(activity, "Data Added", Toast.LENGTH_SHORT).show()
                        ftAnimation()
                        dialog.dismiss()
                    } else {
                        Toast.makeText(activity, "Failed to add data", Toast.LENGTH_SHORT).show()
                    }
                }
        }
        btnCancel.setOnClickListener{

            ftAnimation()
            dialog.dismiss()
        }
    dialog.show()
    }

    fun expenseDataInsert(){
        val myDialog = AlertDialog.Builder(activity)
        val inflater = LayoutInflater.from(activity)
        val myView = inflater.inflate(R.layout.custom_layout_for_insertdata, null)
        myDialog.setView(myView)

        val dialog = myDialog.create()
        val editAmount = myView.findViewById<EditText>(R.id.amount_edt)

        editType = myView.findViewById(R.id.type_edt)
        val typeAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.Spinner_items, // Array resource containing Spinner items
            android.R.layout.simple_spinner_item
        )
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        editType.adapter = typeAdapter
        val editNote = myView.findViewById<EditText>(R.id.note_edt)

        val btnSave = myView.findViewById<TextView>(R.id.btnSave)
        val btnCancel = myView.findViewById<TextView>(R.id.btnCancel)

        btnSave.setOnClickListener {
            val ttype = editType.selectedItem.toString().trim()
            if (ttype == "Select") {
                // Display an error message on the spinner
                (editType.selectedView as? TextView)?.error = "Please select a correct value"
                return@setOnClickListener
            } else {
                // Clear error message on the spinner if it was previously set
                (editType.selectedView as? TextView)?.error = null
            }
            val tamount = editAmount.text.toString().trim()
            val tnote = editNote.text.toString().trim()

            if (tamount.isEmpty()) {
                editAmount.error = "Required Field.."
                return@setOnClickListener
            }

            val ourAmountInt = tamount.toString() // Throws NumberFormatException if parsing fails
            if (tnote.isEmpty()) {
                editNote.error = "Required Field.."
                return@setOnClickListener
            }

            val mUser = mAuth.currentUser
            val uid: String = mUser!!.uid
            val expenseId: String = mExpenseDatabase.child("IncomeData").push().key!!

            // Get current date
            val currentDate = Calendar.getInstance().time
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val formattedDate: String = dateFormat.format(currentDate)
            val year:String=SimpleDateFormat("YYYY",Locale.getDefault()).format(currentDate)
            val yearMonth: String = SimpleDateFormat("MMM", Locale.getDefault()).format(currentDate) // Format month as "Mar"

            val data = Data(ourAmountInt, ttype, tnote, formattedDate, expenseId) // Save date in "Mar 05, 2024" format

            // Save the data under the current user's year/month
            mExpenseDatabase.child(year).child(yearMonth).child(uid).child(expenseId).setValue(data)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(activity, "Data Added", Toast.LENGTH_SHORT).show()
                        ftAnimation()
                        dialog.dismiss()
                    } else {
                        Toast.makeText(activity, "Failed to add data", Toast.LENGTH_SHORT).show()
                    }
                }
        }


        btnCancel.setOnClickListener{
            ftAnimation()
            dialog.dismiss()

        }
        dialog.show()
    }
}
class dsIncomeAdapter(private val incomeList: List<Data>) :
    RecyclerView.Adapter<dsIncomeAdapter.IncomeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncomeViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.dashboard_income, parent, false)
        return IncomeViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: IncomeViewHolder, position: Int) {
        val currentItem = incomeList[position]
        holder.typeTextView.text = currentItem.type
        holder.amountTextView.text = currentItem.amount.toString()
        holder.dateTextView.text = currentItem.date
    }

    override fun getItemCount() = incomeList.size

    inner class IncomeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val typeTextView: TextView = itemView.findViewById(R.id.type_income_ds)
        val amountTextView: TextView = itemView.findViewById(R.id.amount_income_ds)
        val dateTextView: TextView = itemView.findViewById(R.id.date_income_ds)
    }
}

class dsExpenseAdapter(private val expenseList: List<Data>) :
    RecyclerView.Adapter<dsExpenseAdapter.ExpenseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.dashboard_expense, parent, false)
        return ExpenseViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val currentItem = expenseList[position]
        holder.typeTextView.text = currentItem.type
        holder.amountTextView.text = currentItem.amount.toString()
        holder.dateTextView.text = currentItem.date
    }

    override fun getItemCount() = expenseList.size

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val typeTextView: TextView = itemView.findViewById(R.id.type_expense_ds)
        val amountTextView: TextView = itemView.findViewById(R.id.amount_expense_ds)
        val dateTextView: TextView = itemView.findViewById(R.id.date_expense_ds)
    }
}
