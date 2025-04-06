package k4ustu3h.dedupe

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.PopupMenu
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
    private var isAscending = true
    private var currentSortComparator: Comparator<Item<*>>? = SortUtils.compareByName()
    private val manageStorageActivityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (Environment.isExternalStorageManager()) {
                startScan()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
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

        binding.scanButton.setOnClickListener {
            PermissionUtils.checkAndRequestManageStoragePermission(
                this, manageStorageActivityResultLauncher
            ) {
                startScan()
            }
        }
        binding.deleteButton.setOnClickListener {
            DeletionUtils.deleteSelectedFiles(
                adapter, this
            ) { startScan() } // Passing 'this' as the context
        }
        binding.sortButton.setOnClickListener { v ->
            showSortPopupMenu(v)
        }
        binding.orderToggleButton.setOnClickListener {
            isAscending = !isAscending
            updateToggleButtonIcon()
            sortCurrentList(currentSortComparator)
        }
        updateToggleButtonIcon()
    }

    private fun updateToggleButtonIcon() {
        if (isAscending) {
            binding.orderToggleButton.setImageResource(R.drawable.sort_ascending)
        } else {
            binding.orderToggleButton.setImageResource(R.drawable.sort_descending)
        }
    }

    private fun showSortPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.sort_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.sort_by_name -> {
                    currentSortComparator = SortUtils.compareByName()
                    sortCurrentList(currentSortComparator)
                    true
                }

                R.id.sort_by_size -> {
                    currentSortComparator = SortUtils.compareBySize()
                    sortCurrentList(currentSortComparator)
                    true
                }

                else -> false
            }
        }
        popupMenu.show()
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

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionUtils.onRequestPermissionsResult(requestCode, grantResults, {
            lifecycleScope.launch {
                startScan()
            }
        }, {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        })
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
                if (scannedItems != null) {
                    adapter.clear()
                    adapter.addAll(scannedItems)
                    sortCurrentList(currentSortComparator)
                } else {
                    Toast.makeText(this@MainActivity, "File scan failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}