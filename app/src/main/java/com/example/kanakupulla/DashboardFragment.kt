package com.example.kanakupulla

import android.app.AlertDialog
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.expensesmanager.model.Data
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.DateFormat
import java.util.Date

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
    private var isOpen = false
    private lateinit var FadOpen: Animation
    private lateinit var FadClose: Animation

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val myview = inflater.inflate(R.layout.fragment_dashboard, container, false)

        mAuth=FirebaseAuth.getInstance()

        val mUser = mAuth.currentUser
        val uid:String = mUser!!.uid

        mIncomeDatabase = FirebaseDatabase.getInstance().getReference().child("IncomeData").child(uid)
        mExpenseDatabase = FirebaseDatabase.getInstance().getReference().child("ExpenseData").child(uid)


        fab_main_btn = myview.findViewById(R.id.fb_main_plus_btn)
        fab_income_btn = myview.findViewById(R.id.income_ft_btn)
        fab_expense_btn = myview.findViewById(R.id.expense_ft_btn)

        fab_income_txt = myview.findViewById(R.id.expense_ft_text)
        fab_expense_txt = myview.findViewById(R.id.income_ft_text)

        FadOpen = AnimationUtils.loadAnimation(requireActivity(), R.anim.fade_open)
        FadClose = AnimationUtils.loadAnimation(requireActivity(), R.anim.fade_close)

        fab_main_btn.setOnClickListener {
            addData()
            if (isOpen) {
                closeMenu()
            } else {
                openMenu()
            }
        }
        return myview
    }

    private fun openMenu() {
        fab_income_btn.startAnimation(FadOpen)
        fab_expense_btn.startAnimation(FadOpen)
        fab_income_btn.isClickable = true
        fab_expense_btn.isClickable = true

        fab_income_txt.startAnimation(FadOpen)
        fab_expense_txt.startAnimation(FadOpen)
        fab_income_txt.isClickable = true
        fab_expense_txt.isClickable = true

        isOpen = true
    }

    private fun closeMenu() {
        fab_income_btn.startAnimation(FadClose)
        fab_expense_btn.startAnimation(FadClose)
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
    }
    fun incomeDataInsert() {
        val myDialog = AlertDialog.Builder(activity)
        val inflater = LayoutInflater.from(activity)
        val myView = inflater.inflate(R.layout.custom_layout_for_insertdata, null)
        myDialog.setView(myView)

        val dialog = myDialog.create()
        val editAmount = myView.findViewById<EditText>(R.id.amount_edt)
        val editType = myView.findViewById<EditText>(R.id.type_edt)
        val editNote = myView.findViewById<EditText>(R.id.note_edt)

        val btnSave = myView.findViewById<TextView>(R.id.btnSave)
        val btnCancel = myView.findViewById<TextView>(R.id.btnCancel)

        btnSave.setOnClickListener {
            val type = editType.text.toString().trim()
            val amount = editAmount.text.toString().trim()
            val note = editNote.text.toString().trim()

            if (type.isEmpty()) {
                editType.error = "Required Field.."
            }
            if (amount.isEmpty()) {
                editAmount.error = "Required Field.."
            }

            val ourAmountInt = amount.toInt() // Throws NumberFormatException if parsing fails
            if (note.isEmpty()) {
                editNote.error = "Required Field.."
            }
            val mUser = mAuth.currentUser
            val uid: String = mUser!!.uid
            val id:String = mIncomeDatabase.child(uid).push().key!!

            val mDate:String=DateFormat.getDateInstance().format(Date())

            val data = Data(ourAmountInt, type, note, id, mDate)

            mIncomeDatabase.child(id).setValue(data)
            Toast.makeText(activity,"Data Added",Toast.LENGTH_SHORT).show()
            ftAnimation()
            dialog.dismiss()

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
        val editType = myView.findViewById<EditText>(R.id.type_edt)
        val editNote = myView.findViewById<EditText>(R.id.note_edt)

        val btnSave = myView.findViewById<TextView>(R.id.btnSave)
        val btnCancel = myView.findViewById<TextView>(R.id.btnCancel)

        btnSave.setOnClickListener {
            val ttype = editType.text.toString().trim()
            val tamount = editAmount.text.toString().trim()
            val tnote = editNote.text.toString().trim()

            if (ttype.isEmpty()) {
                editType.error = "Required Field.."
            }
            if (tamount.isEmpty()) {
                editAmount.error = "Required Field.."
            }

            val inAmountInt = tamount.toInt() // Throws NumberFormatException if parsing fails
            if (tnote.isEmpty()) {
                editNote.error = "Required Field.."
            }
            val mUser = mAuth.currentUser
            val uid: String = mUser!!.uid
            val id:String = mExpenseDatabase.child(uid).push().key!!

            val mDate:String=DateFormat.getDateInstance().format(Date())

            val data = Data(inAmountInt, ttype, tnote, id, mDate)

            mExpenseDatabase.child(id).setValue(data)
            Toast.makeText(activity,"Data Added",Toast.LENGTH_SHORT).show()
            ftAnimation()
            dialog.dismiss()

        }
        btnCancel.setOnClickListener{
            ftAnimation()
            dialog.dismiss()

        }
        dialog.show()
    }
}

