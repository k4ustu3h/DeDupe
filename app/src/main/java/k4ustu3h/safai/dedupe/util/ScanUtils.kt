package k4ustu3h.safai.dedupe.util

import android.content.Context
import android.os.Environment
import android.widget.Toast
import com.xwray.groupie.GroupAdapter
import k4ustu3h.safai.dedupe.components.DuplicateFilesGroup
import k4ustu3h.safai.dedupe.components.card.DuplicateFileCard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

object ScanUtils {
    fun scanForDuplicates(
        context: Context,
        adapter: GroupAdapter<DuplicateFileCard.DuplicateFileCardViewHolder>,
        onScanComplete: () -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val sharedPreferences =
                context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            val applySizeLimit = sharedPreferences.getBoolean("enable_file_size_limit", false)
            val fileMap = mutableMapOf<String, MutableList<File>>()
            val root = Environment.getExternalStorageDirectory()

            FileUtils.traverseFiles(root, fileMap, applySizeLimit)

            withContext(Dispatchers.Main) {
                adapter.clear()
                fileMap.values.forEach { files ->
                    if (files.size > 1) {
                        adapter.add(DuplicateFilesGroup(files))
                    }
                }
                if (adapter.itemCount == 0) {
                    Toast.makeText(context, "No duplicate files found", Toast.LENGTH_SHORT).show()
                }
                onScanComplete()
            }
        }
    }
}