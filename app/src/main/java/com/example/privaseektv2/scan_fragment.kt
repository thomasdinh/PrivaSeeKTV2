package com.example.privaseektv2

import android.content.Context
import android.Manifest
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat


class scan_fragment : Fragment() {

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
        scanButton = view.findViewById(R.id.button)

        scanButton.setOnClickListener {
            performWifiScan()
        }

        return view
    }

    private fun performWifiScan() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            wifiManager.startScan()

            val scanResults = wifiManager.scanResults
            val scanResultsText = StringBuilder()

            for (scanResult in scanResults) {
                scanResultsText.append(scanResult.SSID).append("\n")
            }

            scanResultsTextView.text = scanResultsText.toString()
        } else {
            // Handle permission request if not granted
            // You can request permission here using ActivityCompat.requestPermissions
        }
    }
}