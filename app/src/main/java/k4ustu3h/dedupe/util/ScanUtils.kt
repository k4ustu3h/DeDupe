package k4ustu3h.dedupe.util

import android.os.Environment
import com.xwray.groupie.Item
import k4ustu3h.dedupe.components.DuplicateFilesGroup
import k4ustu3h.dedupe.components.item.SingleFileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object ScanUtils {

    suspend fun scanForDuplicates(): List<Item<*>> = withContext(Dispatchers.IO) {
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
        return@withContext allItems
    }
}