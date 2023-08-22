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
    private val dataRepository = DataRepository.getInstance()

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

        scanResultsTextView.text="Still running the code... or failed"
        scanResultsTextView.text= dataRepository.getAllData().toString()


        scanButton.setOnClickListener {
            displayIPAndBroadcast()
            if(IcmpScanUtility.isWifiConnected(requireContext())){
                CoroutineScope(Dispatchers.IO).launch {
                    val result = IcmpScanUtility.icmpScan(requireContext()) // Execute Python code here
                    updateDataInRepository(result)

                    // Update the UI on the main thread
                    launch(Dispatchers.Main) {
                        scanResultsTextView.text = result.toString()
                    }
                }

            }else{
                scanResultsTextView.text = "You are not connected to the internet!"
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

    override fun onDestroyView() {
        super.onDestroyView()

    }

    private fun updateDataInRepository(newData: String) {
        dataRepository.addOrUpdateData(newData)
    }





}