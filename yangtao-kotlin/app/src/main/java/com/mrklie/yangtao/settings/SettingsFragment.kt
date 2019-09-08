package com.mrklie.yangtao.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.mrklie.yangtao.R

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
    }
}