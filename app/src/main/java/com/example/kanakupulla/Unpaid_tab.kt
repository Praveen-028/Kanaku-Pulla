// Unpaid_tab.kt
package com.example.kanakupulla

import android.app.AlertDialog
import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.example.expensesmanager.model.UnData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ValueEventListener

interface OnItemClickListener {
    fun onItemClick(position: UnData)
}

class Unpaid_tab : Fragment(), OnItemClickListener {

    private lateinit var UnpaidRecycler: RecyclerView
    private lateinit var un_fb_main_plus_btn: FloatingActionButton
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mUnpaid: DatabaseReference
    private lateinit var unpaidAdapter: UnpaidAdapter // Create an adapter for your unpaid data

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_unpaid_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAuth = FirebaseAuth.getInstance()

        val mUser = mAuth.currentUser
        val uid: String = mUser!!.uid
        // Initialize RecyclerViews
        UnpaidRecycler = view.findViewById(R.id.unpaidRecycler)
        unpaidAdapter = UnpaidAdapter(this) // Initialize your custom adapter
        UnpaidRecycler.layoutManager = LinearLayoutManager(requireContext())
        UnpaidRecycler.adapter = unpaidAdapter

        mUnpaid = FirebaseDatabase.getInstance().getReference().child("UnpaidData").child(uid)

        un_fb_main_plus_btn = view.findViewById(R.id.un_fb_main_plus_btn)

        mUnpaid.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val unpaidList = mutableListOf<UnData>()
                for (dataSnapshot in snapshot.children) {
                    val undata = dataSnapshot.getValue(UnData::class.java)
                    undata?.let {
                        unpaidList.add(it)
                    }
                }
                unpaidAdapter.submitList(unpaidList) // Pass the fetched data to your adapter
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                Log.e("Unpaid_tab", "Database error: ${error.message}")
            }
        })

        // Set click listener for floating action button
        un_fb_main_plus_btn.setOnClickListener {
            // Your click listener logic goes here
            showToast("Floating action button clicked")
        }
    }

    private fun showToast(message: String) {
        // You can replace this with your desired action
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onItemClick(unData: UnData) {

        mAuth = FirebaseAuth.getInstance()
        val currentUser: FirebaseUser? = mAuth.currentUser
        val uid: String = currentUser?.uid ?: ""

        val databaseReference = FirebaseDatabase.getInstance().getReference("UnpaidData").child(uid).child(unData.id)

        val myDialog = AlertDialog.Builder(requireContext())
        val inflater = LayoutInflater.from(requireContext())
        val myView = inflater.inflate(R.layout.changes_tab, null)
        myDialog.setView(myView)

        val dialog = myDialog.create()

        val btndel = myView.findViewById<TextView>(R.id.deletedata)
        btndel.setOnClickListener {
            databaseReference.removeValue()
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                .addOnFailureListener { e ->
                    Log.e(ContentValues.TAG, "Error deleting data: $e")
                    Toast.makeText(requireContext(), "Can't Delete", Toast.LENGTH_SHORT).show()
                }
            dialog.dismiss()
        }

        val alertDialog = myDialog.show()

// Set the dialog's gravity to CENTER
        val window = alertDialog.window
        window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setGravity(Gravity.CENTER)
    }

}
