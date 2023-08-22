package com.example.privaseektv2

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat

class settings_Fragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }
}