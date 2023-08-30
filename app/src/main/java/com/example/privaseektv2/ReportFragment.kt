package com.example.privaseektv2

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.ReportFragment
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate



/**
 * A simple [Fragment] subclass.
 * Use the [ReportFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ReportFragment : Fragment() {
    lateinit var barChart: BarChart

    // on below line we are creating
    // a variable for bar data
    lateinit var barData: BarData

    // on below line we are creating a
    // variable for bar data set
    lateinit var barDataSet: BarDataSet

    // on below line we are creating array list for bar data
    lateinit var barEntriesList: ArrayList<BarEntry>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_report, container, false)

        // Initialize the BarChart view
        barChart = view.findViewById<BarChart>(R.id.barchart)

        // Populate and setup the BarChart
        barEntriesList = ArrayList()
        barEntriesList.add(BarEntry(1f, 10f))
        barEntriesList.add(BarEntry(2f, 20f))
        barEntriesList.add(BarEntry(3f, 15f))

        // Create a BarDataSet from the data entries
        barDataSet = BarDataSet(barEntriesList, "Sample Data")

        val labels = ArrayList<String>()
        labels.add("stud")
        labels.add("mobiles")
        labels.add("Smart-Speaker")
        labels.add("Devices")

        // Create BarData from the BarDataSet
        barData = BarData(barDataSet)

        // Customize the x-axis labels
        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        // Replace with your labels
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        // Set the BarData to the BarChart
        barChart.data = barData

        // Refresh the chart
        barChart.invalidate()

        return view
    }

    private fun setupBarChart() {
        val barEntries = ArrayList<BarEntry>()
        barEntries.add(BarEntry(1f, 10f))
        barEntries.add(BarEntry(2f, 15f))
        barEntries.add(BarEntry(3f, 8f))



        val barDataSet = BarDataSet(barEntries, "Bar Data")
        val barData = BarData(barDataSet)

        // Apply any required customization to the chart appearance
        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM

        // Remove the description label
        barChart.description.text=""
        barChart.description.isEnabled = false

        // Set the data for the BarChart
        barChart.data = barData

        // Refresh the chart to ensure it's displayed
        barChart.invalidate()
    }
}
