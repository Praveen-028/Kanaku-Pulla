package com.mad.kanakupulla

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class IncGraph : Fragment() {

    private lateinit var lineChart: LineChart

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_inc_graph, container, false)
        lineChart = view.findViewById(R.id.lineChart)
        fetchIncomeData()
        return view
    }

    private fun fetchIncomeData() {
        val currentDate = Calendar.getInstance().time
        val year:String= SimpleDateFormat("YYYY", Locale.getDefault()).format(currentDate)
        val yearMonth: String = SimpleDateFormat("MMM", Locale.getDefault()).format(currentDate)
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val databaseRef = FirebaseDatabase.getInstance().getReference("IncomeData").child(year).child(yearMonth).child(uid)
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val entries = mutableListOf<Entry>()
                for (snapshot in dataSnapshot.children) {
                    val amount = snapshot.child("amount").getValue(String::class.java)?.toFloatOrNull() ?: 0f
                    val date = snapshot.child("date").getValue(String::class.java) ?: ""

                    // Convert the date to a float value representing the day of the month
                    val dayOfMonth = date.substringBefore(",").toFloatOrNull() ?: 0f
                    entries.add(Entry(dayOfMonth, amount))
                }
                updateLineChart(entries)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle possible errors.
            }
        })
    }

    private fun updateLineChart(entries: List<Entry>) {
        val dataSet = LineDataSet(entries, "Income Data")
        val lineData = LineData(dataSet)
        lineChart.data = lineData
        lineChart.invalidate() // Refresh the chart

        val description = Description()
        description.text = "Income Over Time"
        lineChart.description = description
    }
}
