package com.example.privaseektv2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar

import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout

import com.example.privaseektv2.IcmpScanUtility.icmpScan
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val dataRepository = DataRepository.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_main)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawerLayout)
        val navView :NavigationView = findViewById(R.id.nav_view)
        val toolbar :Toolbar =findViewById(R.id.toolbar)


        setSupportActionBar(toolbar)

        // Set up ActionBarDrawerToggle
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        launchICMPScanInBackground()

        // Handle navigation item clicks
        navView.setNavigationItemSelectedListener { menuItem ->
            // Handle navigation item clicks here
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, home_fragment())
                        .commit()
                }
                R.id.nav_scan -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ScanFragment())
                        .commit()
                }
                // Handle other menu items if needed
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
        // Load the HomeFragment as the start fragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, home_fragment()) // Replace with your fragment class
                .commit()
        }

    }


    private fun launchICMPScanInBackground() {
        CoroutineScope(Dispatchers.Main).launch {
            val scanResult = IcmpScanUtility.icmpScan(this@MainActivity)
            updateDataInRepository(scanResult.toString())
            // Handle scan result here (e.g., display it in a TextView)
        }
    }

    private fun updateDataInRepository(newData: String) {
        dataRepository.addOrUpdateData(newData)
    }
}