package com.example.privaseektv2

import android.content.Context
import android.net.ConnectivityManager
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

object IcmpScanUtility {

    fun isWifiConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo?.type == ConnectivityManager.TYPE_WIFI && networkInfo.isConnected
    }

    suspend fun icmpScan(context: Context): String {
        // Your existing icmp_scan() function code goes here
        // Remember to replace 'this' references with 'context'
        // Setup for Python Code
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(context))
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
}