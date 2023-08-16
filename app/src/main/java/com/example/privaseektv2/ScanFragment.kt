package com.example.privaseektv2

import android.content.Context
import android.Manifest
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.InetAddress
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScanFragment : Fragment() {

    private lateinit var wifiManager: WifiManager
    private lateinit var scanResultsTextView: TextView
    private lateinit var scanButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.scan_fragment, container, false)

        wifiManager = requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        scanResultsTextView = view.findViewById(R.id.textView_scanResults)
        scanButton = view.findViewById(R.id.button_scan)

        //Setup for Python Code
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(requireContext()));
        }

        scanButton.setOnClickListener {
            displayIPAndBroadcast()
            CoroutineScope(Dispatchers.IO).launch {
                val result = icmp_scan() // Execute Python code here

                // Update the UI on the main thread
                launch(Dispatchers.Main) {
                    scanResultsTextView.text = result.toString()
                }
            }

        }

        return view
    }

    private fun displayOwnIP() {
        val ownIP = getOwnIP(requireContext())
        scanResultsTextView.text = ownIP ?: "Couldn't retrieve IP"
    }

    private fun displayBroadcastAddress() {
        val subnetMask = getSubnetMask(requireContext())
        val ownIP = getOwnIP(requireContext())
        val broadcastIP = calculateBroadcastIP(ownIP, subnetMask)
        scanResultsTextView.text = "Broadcast Address: $broadcastIP"
    }

    private fun displayIPAndBroadcast() {
        val subnetMask = getSubnetMask(requireContext())
        val ownIP = getOwnIP(requireContext())
        val broadcastIP = calculateBroadcastIP(ownIP, subnetMask)

        val displayText = "IP Address: $ownIP\nBroadcast Address: $broadcastIP"
        scanResultsTextView.text = displayText
    }

    private fun getSubnetMask(context: Context?): String? {
        if (context == null) {
            return null
        }

        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val dhcpInfo = wifiManager.dhcpInfo
        val subnetMask = dhcpInfo.netmask

        return String.format(
            "%d.%d.%d.%d",
            subnetMask and 0xff,
            (subnetMask shr 8) and 0xff,
            (subnetMask shr 16) and 0xff,
            (subnetMask shr 24) and 0xff
        )
    }

    private fun calculateBroadcastIP(ip: String?, subnetMask: String?): String? {
        if (ip == null || subnetMask == null) {
            return null
        }

        val ipParts = ip.split('.').map { it.toInt() }
        val subnetMaskParts = subnetMask.split('.').map { it.toInt() }

        val invertedSubnetMaskParts = subnetMaskParts.map { 255 - it }
        val broadcastIPParts = ipParts.zip(invertedSubnetMaskParts) { a, b -> a or b }

        return broadcastIPParts.joinToString(".")
    }


    private fun getOwnIP(context: Context?):String?{
        if (context == null) {
            return null
        }

        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo
        val ipAddress = wifiInfo.ipAddress

        return String.format(
            "%d.%d.%d.%d",
            ipAddress and 0xff,
            ipAddress shr 8 and 0xff,
            ipAddress shr 16 and 0xff,
            ipAddress shr 24 and 0xff
        )
    }

    //private val UDP_PORT = 12345 // Use the port you want to scan on
    private val executor: ExecutorService = Executors.newFixedThreadPool(10) // Adjust the number of threads as needed



    private suspend fun icmp_scan(): String {
        // Setup for Python Code
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(requireContext()))
        }
        val py = Python.getInstance()
        val pyScript = py.getModule("icmp_scan")

        val startRange = 1
        val endRange = 254
        val segmentSize = 2 // Adjust this based on your preference

        val tasks = mutableListOf<Deferred<PyObject>>()

        var results = tasks.awaitAll()
        try {
            for (i in startRange..endRange step segmentSize) {
                val segmentEnd = minOf(i + segmentSize, endRange + 1)
                val task = CoroutineScope(Dispatchers.IO).async {
                    pyScript.callAttr("main", i, segmentEnd)
                }
                tasks.add(task)
            }
            results = tasks.awaitAll()

            val outputText = StringBuilder()

            for ((index, result) in results.withIndex()) {
                val hostResult = result.toString()
                if (hostResult.isNotEmpty()) {
                    outputText.append("Host segment ${index + 1}:\n$hostResult\n")
                }
            }

            return outputText.toString()
        } catch (e: Exception) {
            return "Task failed: ${e.message}"
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        executor.shutdown() // Shut down the executor when the view is destroyed
    }





}