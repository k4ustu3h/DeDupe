package k4ustu3h.safai.dedupe.util

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.xwray.groupie.GroupAdapter
import k4ustu3h.safai.dedupe.components.DuplicateFilesGroup
import k4ustu3h.safai.dedupe.components.card.DuplicateFileCard
import java.io.File

object DeletionUtils {
    fun deleteSelected(
        context: Context,
        adapter: GroupAdapter<DuplicateFileCard.DuplicateFileCardViewHolder>,
        onDeletionFinished: () -> Unit
    ) {
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
                    Log.d("Delete", "File deleted successfully: ${file.absolutePath}")
                } else {
                    Log.e("Delete", "Failed to delete file: ${file.absolutePath}")
                    Toast.makeText(context, "Could not delete ${file.name}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
        onDeletionFinished()
    }
}