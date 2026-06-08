package com.example.kabaddikounter

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.kabaddikounter.viewModels.SharedViewModel
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

    private val sharedViewModel: SharedViewModel by viewModels()
    private lateinit var navView: NavigationView

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

        }

        navView = findViewById<NavigationView>(R.id.navigationView)
        navView.setupWithNavController(navController)

        observeRole()
        observeDestination()

        handleNotificationIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
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

    private fun observeRole() {
        sharedViewModel.activeRole.observe(this) { role ->
            // Ini murni UI logic — boleh di Activity
            val menuRes = when (role) {
                SharedViewModel.Role.ADMIN  -> R.menu.drawer_menu_admin
                SharedViewModel.Role.VIEWER -> R.menu.drawer_menu_viewer
                null -> R.menu.drawer_menu   // default / belum login
            }
            navView.menu.clear()
            navView.inflateMenu(menuRes)
            NavigationUI.setupWithNavController(navView, navController)
        }
    }

    private fun observeDestination() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.welcomeFragment) {
                sharedViewModel.clearRole()  // reset saat keluar
            }
        }
    }

    private fun handleNotificationIntent(intent: Intent) {
        val navigateTo = intent.getStringExtra("navigateTo") ?: return
        val matchId    = intent.getIntExtra("matchId", -1)

        if (navigateTo == "detailMatchFragment" && matchId != -1) {
            val navController = findNavController(R.id.appNavHostFragment) // sesuaikan ID
            val bundle = Bundle().apply { putInt("matchId", matchId) }
            navController.navigate(R.id.detailMatchFragment, bundle) // sesuaikan ID nav_graph
        }
    }

}