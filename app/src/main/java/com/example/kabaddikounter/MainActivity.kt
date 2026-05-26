package com.example.kabaddikounter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.example.kabaddikounter.databinding.ActivityMainBinding
import com.example.kabaddikounter.viewModels.ScoreViewModel
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import android.app.AlertDialog
import android.content.Intent
import android.widget.EditText
import android.widget.Toast
import com.google.gson.GsonBuilder
import android.Manifest
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager

class MainActivity : AppCompatActivity() {

    val viewModel: ScoreViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding


    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private var isThemeChanging = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        // Set Toolbar sebagai ActionBar
        setSupportActionBar(binding.toolbar)

        // Setup DrawerToggle (ikon hamburger ☰)
        drawerLayout = binding.drawerLayout
        toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()


        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_riwayat_matchs -> {
                    binding.drawerLayout.closeDrawers()
                    val intent = Intent(this@MainActivity, RiwayatActivity::class.java)
                    this@MainActivity.startActivity(intent)
                    true
                }
                else -> false
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isDarkMode.collect { isDark ->
                    AppCompatDelegate.setDefaultNightMode(
                        if (isDark) AppCompatDelegate.MODE_NIGHT_YES
                        else AppCompatDelegate.MODE_NIGHT_NO
                    )

                    binding.textDarkMode.text = if (isDark) "Light Mode" else "Dark Mode"
                }
            }
        }

        // Tombol Simpan Match → tampilkan dialog
        binding.buttonSimpan.setOnClickListener {
            showSaveDialog()
        }

        // Observer status simpan
        viewModel.saveStatus.observe(this) { status ->
            status?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.clearSaveStatus()
            }
        }

        // eksport
        binding.buttonExport.setOnClickListener {
            exportMatchToJson()
        }

        // live score
        LiveScoreNotificationHelper.createChannel(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    100
                )
            }
        }

        viewModel.scoreA.observe(this) {
            LiveScoreNotificationHelper.showLiveScore(
                context = this@MainActivity,
                teamA = viewModel.teamA.value ?: "",
                teamB = viewModel.teamB.value ?: "",
                scoreA = viewModel.scoreA.value ?: 0,
                scoreB = viewModel.scoreB.value ?: 0
            )
        }
        viewModel.scoreB.observe(this) {
            LiveScoreNotificationHelper.showLiveScore(
                context = this@MainActivity,
                teamA = viewModel.teamA.value ?: "",
                teamB = viewModel.teamB.value ?: "",
                scoreA = viewModel.scoreA.value ?: 0,
                scoreB = viewModel.scoreB.value ?: 0
            )
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        LiveScoreNotificationHelper.cancelNotification(this)
    }

    private fun showSaveDialog() {
        val editText = EditText(this).apply {
            hint = "Masukkan judul match"
            setPadding(48, 32, 48, 16)
        }

        AlertDialog.Builder(this)
            .setTitle("Simpan Match")
            .setMessage("Masukkan judul untuk menyimpan hasil pertandingan ini.")
            .setView(editText)
            .setPositiveButton("Simpan") { dialog, _ ->
                val title = editText.text.toString().trim()
                if (title.isEmpty()) {
                    Toast.makeText(this, "Judul tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.saveMatch(title)
                    dialog.dismiss()
                }
            }
            .setNegativeButton("Batal") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun exportMatchToJson() {
        lifecycleScope.launch {
            // Ambil data dari Room DB
            val matches = viewModel.getAllMatches()

            if (matches.isEmpty()) {
                Toast.makeText(this@MainActivity, "Tidak ada data match!", Toast.LENGTH_SHORT).show()
                return@launch
            }

            // Konversi MatchEntity langsung ke JSON (tanpa MatchResult)
            val gson = GsonBuilder().setPrettyPrinting().create()
            val jsonString = gson.toJson(matches)

            // Simpan ke file
            val fileName = "matches_export_${System.currentTimeMillis()}.json"
            val contentValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/json")
                put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, "Download/Kabaddi")
            }

            val uri = contentResolver.insert(
                android.provider.MediaStore.Files.getContentUri("external"),
                contentValues
            )

            uri?.let {
                contentResolver.openOutputStream(it)?.use { outputStream ->
                    outputStream.write(jsonString.toByteArray())
                }
                Toast.makeText(this@MainActivity, "Tersimpan di Downloads/Kabaddi", Toast.LENGTH_SHORT).show()
            } ?: run {
                Toast.makeText(this@MainActivity, "Gagal menyimpan file!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}