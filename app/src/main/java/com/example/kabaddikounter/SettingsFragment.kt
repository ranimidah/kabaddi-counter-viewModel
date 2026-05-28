package com.example.kabaddikounter

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat

/**
 * SettingsFragment menggunakan AndroidX Preference Library.
 *
 * Preference Library secara otomatis:
 * - Menampilkan UI settings dari res/xml/preferences.xml
 * - Menyimpan nilai ke SharedPreferences tanpa kode tambahan
 * - MainActivity akan listen perubahan SharedPreferences dan apply tema
 *
 * Tidak perlu ViewModel di sini — Preference Library sudah handle persistence sendiri.
 */
class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // Load tampilan dari res/xml/preferences.xml
        setPreferencesFromResource(R.xml.preferences, rootKey)

        // Listen perubahan dark_mode switch dan langsung apply
        findPreference<SwitchPreferenceCompat>("dark_mode")
            ?.setOnPreferenceChangeListener { _, newValue ->
                val isDark = newValue as Boolean
                AppCompatDelegate.setDefaultNightMode(
                    if (isDark) AppCompatDelegate.MODE_NIGHT_YES
                    else AppCompatDelegate.MODE_NIGHT_NO
                )
                true // return true = simpan nilai ke SharedPreferences
            }
    }
}