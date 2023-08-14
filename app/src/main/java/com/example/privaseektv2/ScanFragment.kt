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
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
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

        scanButton.setOnClickListener {
            displayIPAndBroadcast()
            performIcmpScan()

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

    private fun performIcmpScan() {
        val ownIP = getOwnIP(requireContext())
        val subnetMask = getSubnetMask(requireContext())

        if (ownIP == null || subnetMask == null) {
            scanResultsTextView.text = "Couldn't retrieve IP or subnet mask"
            return
        }

        val baseIP = calculateBaseIP(ownIP, subnetMask)
        val ipAddressesToScan = mutableListOf<String>()

        for (i in 1..254) {
            val ipAddress = "$baseIP.$i"
            ipAddressesToScan.add(ipAddress)
        }

        // Use executor to perform the scan in the background
        executor.submit {
            val activeHosts = mutableListOf<String>()
            for (ip in ipAddressesToScan) {
                if (isHostReachable(ip)) {
                    activeHosts.add(ip)
                }
            }

            // Update UI with active hosts
            activity?.runOnUiThread {
                val resultText = activeHosts.joinToString("\n")
                scanResultsTextView.text = resultText
            }
        }
    }

    private fun isHostReachable(host: String): Boolean {
        return try {
            val inetAddress = InetAddress.getByName(host)
            inetAddress.isReachable(3000) // Timeout in milliseconds
        } catch (e: IOException) {
            false
        }
    }

    private fun calculateBaseIP(ownIP: String, subnetMask: String): String {
        val ownIPParts = ownIP.split('.').map { it.toInt() }
        val subnetMaskParts = subnetMask.split('.').map { it.toInt() }

        val baseIPParts = ownIPParts.zip(subnetMaskParts) { ipPart, maskPart ->
            ipPart and maskPart
        }

        return baseIPParts.joinToString(".")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        executor.shutdown() // Shut down the executor when the view is destroyed
    }





}