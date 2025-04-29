package k4ustu3h.dedupe

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.transition.TransitionManager
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.skydoves.androidveil.VeilRecyclerFrameView
import com.skydoves.balloon.ArrowOrientation
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.xwray.groupie.GroupAdapter
import k4ustu3h.dedupe.components.card.DuplicateFileCard
import k4ustu3h.dedupe.databinding.ActivityMainBinding
import k4ustu3h.dedupe.transition.ButtonTransitions
import k4ustu3h.dedupe.transition.ViewTransitions
import k4ustu3h.dedupe.util.DeletionUtils
import k4ustu3h.dedupe.util.PermissionUtils
import k4ustu3h.dedupe.util.ScanUtils

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val adapter = GroupAdapter<DuplicateFileCard.DuplicateFileCardViewHolder>()
    private lateinit var buttonLayoutContainer: MaterialCardView
    private lateinit var deleteButton: Button
    private lateinit var mainLayout: ConstraintLayout
    private lateinit var recyclerView: VeilRecyclerFrameView
    private lateinit var scanButton: Button
    private lateinit var topAppBar: MaterialToolbar
    private lateinit var welcomeLayout: LinearLayout
    private var isScanning = false

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

        buttonLayoutContainer = findViewById(R.id.buttonLayout)
        deleteButton = findViewById(R.id.deleteButton)
        mainLayout = findViewById(R.id.main)
        recyclerView = findViewById(R.id.recyclerView)
        scanButton = findViewById(R.id.scanButton)
        topAppBar = findViewById(R.id.topAppBar)
        welcomeLayout = findViewById(R.id.welcomeLayout)

        recyclerView.setAdapter(adapter)
        recyclerView.setLayoutManager(LinearLayoutManager(this))
        recyclerView.addVeiledItems(1)
        recyclerView.visibility = View.GONE

        deleteButton.visibility = View.GONE

        binding.scanButton.setOnClickListener {
            if (!isScanning) {
                isScanning = true
                scanButton.isEnabled = false
                ViewTransitions.fadeOut(welcomeLayout)
                ViewTransitions.fadeIn(recyclerView)
                binding.deleteButton.visibility = View.GONE
                TransitionManager.beginDelayedTransition(
                    binding.buttonLayout, ButtonTransitions.createButtonTransition()
                )
                ButtonTransitions.applyScanButtonExpand(
                    binding.main, binding.buttonLayout, binding.scanButton
                )
                checkAndRequestManageStoragePermission()
            }
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
                isScanning = false
                scanButton.isEnabled = true
                recyclerView.unVeil()
                ViewTransitions.fadeIn(recyclerView)
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                TransitionManager.beginDelayedTransition(
                    binding.buttonLayout, ButtonTransitions.createButtonTransition()
                )
                ButtonTransitions.applyScanButtonContract(
                    binding.main, binding.buttonLayout, binding.scanButton, binding.deleteButton
                )
                binding.deleteButton.visibility =
                    if (adapter.itemCount > 0) View.VISIBLE else View.GONE
            }
        }

    private fun checkAndRequestManageStoragePermission() {
        if (!PermissionUtils.checkManageStoragePermission()) {
            PermissionUtils.requestManageStoragePermission(
                manageStorageActivityResultLauncher, this
            )
        } else {
            startScan()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun startScan() {
        isScanning = true
        scanButton.isEnabled = false
        recyclerView.veil()
        binding.deleteButton.visibility = View.GONE
        TransitionManager.beginDelayedTransition(
            binding.buttonLayout, ButtonTransitions.createButtonTransition()
        )
        ButtonTransitions.applyScanButtonExpand(
            binding.main, binding.buttonLayout, binding.scanButton
        )

        ScanUtils.scanForDuplicates(this, adapter) {
            isScanning = false
            scanButton.isEnabled = true
            recyclerView.unVeil()
            TransitionManager.beginDelayedTransition(
                binding.buttonLayout, ButtonTransitions.createButtonTransition()
            )
            if (adapter.itemCount > 0) {
                ButtonTransitions.applyScanButtonContract(
                    binding.main, binding.buttonLayout, binding.scanButton, binding.deleteButton
                )
                binding.deleteButton.visibility = View.VISIBLE
                welcomeLayout.visibility = View.GONE
            } else {
                ButtonTransitions.applyScanButtonExpand(
                    binding.main, binding.buttonLayout, binding.scanButton
                )
                ViewTransitions.fadeIn(welcomeLayout)
                recyclerView.visibility = View.GONE
                binding.deleteButton.visibility = View.GONE
                Toast.makeText(this, "No duplicate files found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteSelectedFiles() {
        TransitionManager.beginDelayedTransition(
            binding.buttonLayout, ButtonTransitions.createButtonTransition()
        )
        binding.deleteButton.visibility = View.GONE
        DeletionUtils.deleteSelected(this, adapter) { startScan() }
    }
}