package com.example.kabaddikounter

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView

/**
 * MainActivity hanya bertugas sebagai HOST.
 * - Tidak ada logic bisnis di sini
 * - Tidak ada ViewModel di sini
 * - Semua konten ada di masing-masing Fragment
 *
 * Cocok dengan activity_main.xml yang sudah ada:
 * - NavHostFragment id: appNavHostFragment
 * - BottomNavigationView id: bottom_nav
 * - navGraph: @navigation/navigation
 */
class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)  // langsung, tanpa DataBinding

        // toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Ambil NavController — ID harus sama dengan XML: appNavHostFragment
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.appNavHostFragment) as NavHostFragment
        navController = navHostFragment.navController

        // 3. Hubungkan DrawerLayout ke AppBarConfiguration
        //    ID di setOf() harus sama dengan ID fragment di nav_graph.xml
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.welcomeFragment,
                R.id.homeFragment,
                R.id.matchListFragment,
                R.id.riwayatFragment,
                R.id.settingsFragment
            ),
            drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        val navigationView = findViewById<NavigationView>(R.id.navigationView)
        navigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.welcomeFragment) {
                // Sembunyikan toolbar di halaman welcome (opsional)
                supportActionBar?.hide()
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            } else {
                supportActionBar?.show()
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            }
        }

        // dark mode
        val sharedPrefs = androidx.preference.PreferenceManager
            .getDefaultSharedPreferences(this)
        applyTheme(sharedPrefs.getBoolean("dark_mode", false))

        sharedPrefs.registerOnSharedPreferenceChangeListener { prefs, key ->
            if (key == "dark_mode") applyTheme(prefs.getBoolean("dark_mode", false))



//        // Hubungkan BottomNavigationView ke NavController secara otomatis
//        // ID item menu di bottom_navigation_menu.xml harus sama dengan ID fragment di nav_graph
//        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
//        bottomNav.setupWithNavController(navController)
        }
    }

    private fun applyTheme(isDark: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        // wajib pakai appBarConfiguration agar drawer dan back button bekerja
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}