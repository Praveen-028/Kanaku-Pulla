package com.example.kanakupulla

import android.app.AlertDialog
import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.example.expensesmanager.model.UnData

class Unpaid_tab : Fragment(), OnItemClickListener {

    private lateinit var UnpaidRecycler: RecyclerView
    private lateinit var un_fb_main_plus_btn: FloatingActionButton
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mUnpaid: DatabaseReference
    private lateinit var unpaidAdapter: UnpaidAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_unpaid_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAuth = FirebaseAuth.getInstance()

        val mUser = mAuth.currentUser
        val uid: String = mUser!!.uid

        UnpaidRecycler = view.findViewById(R.id.unpaidRecycler)
        unpaidAdapter = UnpaidAdapter(this)
        UnpaidRecycler.layoutManager = LinearLayoutManager(requireContext())
        UnpaidRecycler.adapter = unpaidAdapter

        mUnpaid = FirebaseDatabase.getInstance().getReference().child("UnpaidData").child(uid)

        mUnpaid.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val unpaidList = mutableListOf<UnData>()
                for (dataSnapshot in snapshot.children) {
                    val undata = dataSnapshot.getValue(UnData::class.java)
                    undata?.let {
                        unpaidList.add(it)
                    }
                }
                unpaidAdapter.submitList(unpaidList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Unpaid_tab", "Database error: ${error.message}")
            }
        })

        un_fb_main_plus_btn = view.findViewById(R.id.un_fb_main_plus_btn)

        un_fb_main_plus_btn.setOnClickListener {
            val myDialog = AlertDialog.Builder(activity)
            val inflater = LayoutInflater.from(activity)
            val myView = inflater.inflate(R.layout.unpaid_layout, null)
            myDialog.setView(myView)

            val dialog = myDialog.create()

            val uneditamount = myView.findViewById<EditText>(R.id.un_amount_edt)
            val unedittype = myView.findViewById<EditText>(R.id.un_type_edt)
            val uneditnote = myView.findViewById<EditText>(R.id.un_note_edt)

            val radioGroup = myView.findViewById<RadioGroup>(R.id.RadioGroup)
            radioGroup.setOnCheckedChangeListener { group, checkedId ->
                // Handle radio button selection
            }

            val btnca = myView.findViewById<TextView>(R.id.un_btnCancel)
            val btnsa = myView.findViewById<TextView>(R.id.un_btnSave)

            btnsa.setOnClickListener {
                val type = unedittype.text.toString().trim()
                val amount = uneditamount.text.toString().trim()
                val note = uneditnote.text.toString().trim()

                val checkedRadioButtonId = radioGroup.checkedRadioButtonId
                val towhat = when (checkedRadioButtonId) {
                    R.id.chooseincome -> "Income"
                    R.id.chooseexpense -> "Expense"
                    else -> {
                        showToast("Choose the radio button")
                        return@setOnClickListener
                    }
                }

                if (type.isEmpty()) {
                    unedittype.error = "Required Field.."
                }
                if (amount.isEmpty()) {
                    uneditamount.error = "Required Field.."
                }
                val ourAmountInt = amount.toIntOrNull() ?: 0 // Handle invalid input
                if (note.isEmpty()) {
                    uneditnote.error = "Required Field.."
                }

                val mUser = mAuth.currentUser
                val uid: String = mUser!!.uid
                val id: String = mUnpaid.child(uid).push().key!!

                val undata = UnData(ourAmountInt, type, note, towhat, uid)

                mUnpaid.child(id).setValue(undata)
                    .addOnSuccessListener {
                        showToast("Data Added")
                        dialog.dismiss()
                    }
                    .addOnFailureListener {
                        showToast("Failed to add data")
                    }
            }

            btnca.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onItemClick(unData: UnData) {
        // Handle item click...
    }
}

interface OnItemClickListener {
    fun onItemClick(position: UnData)
}
