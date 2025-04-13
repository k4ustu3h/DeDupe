package k4ustu3h.dedupe

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import k4ustu3h.dedupe.databinding.ActivityMainBinding
import k4ustu3h.dedupe.databinding.TreeDuplicateItemBinding
import k4ustu3h.dedupe.util.DeletionUtils
import k4ustu3h.dedupe.util.PermissionUtils
import k4ustu3h.dedupe.util.ScanUtils
import k4ustu3h.dedupe.util.SortUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val adapter =
        GroupAdapter<com.xwray.groupie.viewbinding.GroupieViewHolder<TreeDuplicateItemBinding>>()
    private var isAscending = false
    private var currentSortMode = SortMode.NAME
    private var currentSortComparator: Comparator<Item<*>>? = SortUtils.compareByName()
    private lateinit var manageStoragePermissionLauncher: ActivityResultLauncher<Intent>

    enum class SortMode {
        NAME, SIZE
    }

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

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        manageStoragePermissionLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (PermissionUtils.isManageExternalStorageGranted()) {
                    startScan()
                } else {
                    Toast.makeText(
                        this,
                        "Storage permission not granted. Please grant it to scan.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

        binding.scanButton.setOnClickListener {
            checkManageStoragePermissionAndProceed()
        }
        binding.deleteButton.setOnClickListener {
            DeletionUtils.deleteSelectedFiles(adapter, this) { startScan() }
        }
        binding.sortButton.setOnClickListener {
            showSortBottomSheet()
        }
        binding.orderToggleButton.setOnClickListener {
            isAscending = !isAscending
            updateToggleButtonIcon()
            sortCurrentList(currentSortComparator)
        }

        updateToggleButtonIcon()
    }

    private fun showSortBottomSheet() {
        val sortBottomSheetFragment = SortBottomSheetFragment(currentSortMode) { sortMode ->
            currentSortMode = sortMode
            updateSortComparator()
            sortCurrentList(currentSortComparator)
        }
        sortBottomSheetFragment.show(supportFragmentManager, sortBottomSheetFragment.tag)
    }

    private fun checkManageStoragePermissionAndProceed() {
        if (PermissionUtils.isManageExternalStorageGranted()) {
            startScan()
        } else {
            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            manageStoragePermissionLauncher.launch(intent)
        }
    }

    private fun updateToggleButtonIcon() {
        if (isAscending) {
            binding.orderToggleButton.setImageResource(R.drawable.sort_ascending)
        } else {
            binding.orderToggleButton.setImageResource(R.drawable.sort_descending)
        }
    }

    private fun updateSortComparator() {
        currentSortComparator = when (currentSortMode) {
            SortMode.NAME -> SortUtils.compareByName()
            SortMode.SIZE -> SortUtils.compareBySize()
        }
    }

    private fun sortCurrentList(comparator: Comparator<Item<*>>?) {
        val currentItems = mutableListOf<Item<*>>()
        for (i in 0 until adapter.itemCount) {
            currentItems.add(adapter.getItem(i))
        }
        SortUtils.sortItems(currentItems, comparator, isAscending)
        adapter.clear()
        adapter.addAll(currentItems)
    }

    private fun startScan() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            val scannedItems = try {
                ScanUtils.scanForDuplicates()
            } catch (e: Exception) {
                Log.e("startScan", "Error during scan", e)
                null
            }

            withContext(Dispatchers.Main) {
                binding.progressBar.visibility = View.GONE
                if (scannedItems != null && scannedItems.isEmpty()) {
                    Toast.makeText(
                        this@MainActivity, R.string.no_duplicates_found, Toast.LENGTH_LONG
                    ).show()
                } else if (scannedItems != null) {
                    adapter.clear()
                    adapter.addAll(scannedItems)
                    sortCurrentList(currentSortComparator)
                } else {
                    Toast.makeText(this@MainActivity, R.string.scan_failed, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionUtils.onRequestPermissionsResult(grantResults, {
            startScan()
        }, {
            Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show()
        })
    }
}