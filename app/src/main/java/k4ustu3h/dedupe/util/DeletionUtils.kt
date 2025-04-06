package k4ustu3h.dedupe.util

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.xwray.groupie.GroupAdapter
import k4ustu3h.dedupe.components.item.DuplicateFileItem
import java.io.File

object DeletionUtils {

    fun deleteSelectedFiles(
        adapter: GroupAdapter<*>, context: Context, onScanRequested: () -> Unit
    ) {
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
                    Toast.makeText(context, "File could not be deleted", Toast.LENGTH_SHORT).show()
                }
            }
        }
        onScanRequested()
    }
}