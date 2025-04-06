package k4ustu3h.dedupe

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import k4ustu3h.dedupe.databinding.ActivityMainBinding
import k4ustu3h.dedupe.databinding.TreeDuplicateItemBinding
import k4ustu3h.dedupe.utils.FileUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val adapter =
        GroupAdapter<com.xwray.groupie.viewbinding.GroupieViewHolder<TreeDuplicateItemBinding>>()
    private val MANAGE_STORAGE_REQUEST_CODE = 102
    private val manageStorageActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (Environment.isExternalStorageManager()) {
                startScan()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private var isAscending = true
    private var currentSortComparator: Comparator<Item<*>>? = compareByName()

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
            checkAndRequestManageStoragePermission()
        }
        binding.deleteButton.setOnClickListener {
            deleteSelectedFiles()
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
                    val nameComparator = compareByName()
                    currentSortComparator = nameComparator
                    sortCurrentList(nameComparator)
                    true
                }

                R.id.sort_by_size -> {
                    val sizeComparator = compareBySize()
                    currentSortComparator = sizeComparator
                    sortCurrentList(sizeComparator)
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

        if (comparator != null) {
            val finalComparator = if (isAscending) {
                comparator
            } else {
                comparator.reversed()
            }
            currentItems.sortWith(finalComparator)
        } else {
            currentItems.sortWith(compareByName())
        }

        adapter.clear()
        adapter.addAll(currentItems)
    }

    private fun compareByName(): Comparator<Item<*>> = Comparator { item1, item2 ->
        when {
            item1 is DuplicateFileItem && item2 is DuplicateFileItem -> {
                item1.file.name.compareToNatural(item2.file.name, true)
            }

            item1 is SingleFileItem && item2 is SingleFileItem -> {
                item1.file.name.compareToNatural(item2.file.name, true)
            }

            item1 is DuplicateFileItem && item2 is SingleFileItem -> {
                item1.file.name.compareToNatural(item2.file.name, true)
            }

            item1 is SingleFileItem && item2 is DuplicateFileItem -> {
                item1.file.name.compareToNatural(item2.file.name, true)
            }

            else -> 0
        }
    }

    private fun compareBySize(): Comparator<Item<*>> = Comparator { item1, item2 ->
        val size1 = when (item1) {
            is DuplicateFileItem -> item1.file.length()
            is SingleFileItem -> item1.file.length()
            else -> 0
        }
        val size2 = when (item2) {
            is DuplicateFileItem -> item2.file.length()
            is SingleFileItem -> item2.file.length()
            else -> 0
        }
        size1.compareTo(size2)
    }

    private fun checkAndRequestManageStoragePermission() {
        if (!Environment.isExternalStorageManager()) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = String.format("package:%s", applicationContext.packageName).toUri()
                manageStorageActivityResultLauncher.launch(intent)
            } catch (e: Exception) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                manageStorageActivityResultLauncher.launch(intent)
            }
        } else {
            startScan()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MANAGE_STORAGE_REQUEST_CODE) {
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
            FileUtils.traverseFiles(root, fileMap)

            val allItems = mutableListOf<Item<*>>()

            fileMap.values.forEach { files ->
                if (files.size > 1) {
                    val duplicateGroup = DuplicateFilesGroup(files)
                    for (i in 0 until duplicateGroup.itemCount) {
                        allItems.add(duplicateGroup.getItem(i))
                    }
                } else if (files.size == 1 && files[0].parentFile?.absolutePath == root.absolutePath) {
                    allItems.add(SingleFileItem(files[0]))
                }
            }

            withContext(Dispatchers.Main) {
                binding.progressBar.visibility = View.GONE
                adapter.clear()
                adapter.addAll(allItems)
                sortCurrentList(currentSortComparator)
            }
        }
    }

    private fun deleteSelectedFiles() {
        val selectedFiles = mutableSetOf<File>()
        for (i in 0 until adapter.itemCount) {
            val item = adapter.getItem(i)
            if (item is DuplicateFileItem && item.isSelected()) {
                selectedFiles.add(item.file)
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

    fun String.compareToNatural(other: String, ignoreCase: Boolean = false): Int {
        var i = 0
        var j = 0
        while (i < this.length || j < other.length) {
            val chunk1 = StringBuilder()
            while (i < this.length && this[i].isDigit()) {
                chunk1.append(this[i++])
            }
            val chunk2 = StringBuilder()
            while (j < other.length && other[j].isDigit()) {
                chunk2.append(other[j++])
            }

            if (chunk1.isNotEmpty() && chunk2.isNotEmpty()) {
                val n1 = chunk1.toString().toInt()
                val n2 = chunk2.toString().toInt()
                val comparison = n1.compareTo(n2)
                if (comparison != 0) return comparison
            } else {
                val char1 = if (i < this.length) this[i++] else null
                val char2 = if (j < other.length) other[j++] else null

                if (char1 == null && char2 == null) return 0
                if (char1 == null) return -1
                if (char2 == null) return 1

                val c1 = if (ignoreCase) char1.lowercaseChar() else char1
                val c2 = if (ignoreCase) char2.lowercaseChar() else char2

                val comparison = c1.compareTo(c2)
                if (comparison != 0) return comparison
            }
        }
        return 0
    }
}