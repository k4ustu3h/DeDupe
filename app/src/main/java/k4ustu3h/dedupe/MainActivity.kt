package k4ustu3h.dedupe

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.skydoves.balloon.ArrowOrientation
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.xwray.groupie.GroupAdapter
import k4ustu3h.dedupe.components.DuplicateFilesGroup
import k4ustu3h.dedupe.components.card.DuplicateFileCard
import k4ustu3h.dedupe.databinding.ActivityMainBinding
import k4ustu3h.dedupe.utils.FileUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val adapter = GroupAdapter<DuplicateFileCard.DuplicateFileCardViewHolder>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var topAppBar: MaterialToolbar
    private lateinit var welcomeLayout: LinearLayout
    private val permissionRequestCode = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left + 32,
                systemBars.top + 32,
                systemBars.right + 32,
                systemBars.bottom + 32
            )
            insets
        }

        val sharedPref = getSharedPreferences("onboarding_prefs", MODE_PRIVATE)
        val tooltipShown = sharedPref.getBoolean("settings_tooltip_shown", false)

        recyclerView = findViewById(R.id.recyclerView)
        topAppBar = findViewById(R.id.topAppBar)
        welcomeLayout = findViewById(R.id.welcomeLayout)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        binding.scanButton.setOnClickListener {
            welcomeLayout.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            checkAndRequestManageStoragePermission()
        }
        binding.deleteButton.setOnClickListener {
            deleteSelectedFiles()
        }

        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.settingsButton -> {
                    val intent = Intent(this@MainActivity, SettingsActivity::class.java)
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }

        if (!tooltipShown) {
            topAppBar.post {
                val settingsButton = topAppBar.findViewById<View>(R.id.settingsButton)
                if (settingsButton != null) {
                    val balloon = Balloon.Builder(this).setArrowOrientation(ArrowOrientation.TOP)
                        .setArrowPosition(0.81f).setArrowSize(10)
                        .setBackgroundColor(getColor(R.color.primaryContainer))
                        .setBalloonAnimation(BalloonAnimation.FADE).setPadding(12)
                        .setText(getString(R.string.tooltip_size_limit)).setTextSize(12f)
                        .setCornerRadius(10f).setWidthRatio(0.5f).setLifecycleOwner(this).build()

                    balloon.showAlignBottom(settingsButton)

                    sharedPref.edit { putBoolean("settings_tooltip_shown", true) }
                }
            }
        }
    }

    private val manageStorageActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (Environment.isExternalStorageManager()) {
                startScan()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private fun checkAndRequestManageStoragePermission() {
        if (!Environment.isExternalStorageManager()) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = String.format("package:%s", applicationContext.packageName).toUri()
                manageStorageActivityResultLauncher.launch(intent)
            } catch (_: Exception) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                manageStorageActivityResultLauncher.launch(intent)
            }
        } else {
            startScan()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == permissionRequestCode) {
            if (Environment.isExternalStorageManager()) {
                startScan()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScan()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startScan() {
        binding.progressBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            val root = Environment.getExternalStorageDirectory()
            val fileMap = mutableMapOf<String, MutableList<File>>()
            val sharedPreferences = getSharedPreferences("app_settings", MODE_PRIVATE)
            val applySizeLimit = sharedPreferences.getBoolean("enable_file_size_limit", false)

            FileUtils.traverseFiles(root, fileMap, applySizeLimit)

            withContext(Dispatchers.Main) {
                binding.progressBar.visibility = View.GONE
                adapter.clear()
                fileMap.values.forEach { files ->
                    if (files.size > 1) {
                        adapter.add(DuplicateFilesGroup(files))
                    }
                }
                if (adapter.itemCount == 0) {
                    Toast.makeText(
                        this@MainActivity, "No duplicate files found", Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun deleteSelectedFiles() {
        val selectedFiles = mutableSetOf<File>()
        for (groupPosition in 0 until adapter.groupCount) {
            val group = adapter.getGroup(groupPosition)
            if (group is DuplicateFilesGroup) {
                selectedFiles.addAll(group.getSelectedFiles())
            }
        }

        selectedFiles.forEach { file ->
            if (file.exists()) {
                if (file.delete()) {
                    Log.d("Delete", "file deleted: ${file.absolutePath}")
                } else {
                    Log.e("Delete", "file could not be deleted: ${file.absolutePath}")
                    Toast.makeText(this, "File could not be deleted", Toast.LENGTH_SHORT).show()
                }
            }
        }
        startScan()
    }
}