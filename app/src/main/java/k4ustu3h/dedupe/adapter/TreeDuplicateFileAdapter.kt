package k4ustu3h.dedupe.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import k4ustu3h.dedupe.R
import java.io.File

class TreeDuplicateFileAdapter(private val duplicateFileGroups: List<List<File>>) :
    RecyclerView.Adapter<TreeDuplicateFileAdapter.DuplicateFileGroupViewHolder>() {

    private val selectedFiles = mutableSetOf<File>()
    private val indentation = 72 // Indentation value in dp

    class DuplicateFileGroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fileCheckBox: CheckBox = itemView.findViewById(R.id.fileCheckBox)
        val filePathTextView: TextView = itemView.findViewById(R.id.filePathTextView)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): DuplicateFileGroupViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.tree_duplicate_item, parent, false)
        return DuplicateFileGroupViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DuplicateFileGroupViewHolder, position: Int) {
        var currentPosition = 0
        var currentGroup: List<File>? = null

        for (group in duplicateFileGroups) {
            if (position < currentPosition + group.size) {
                currentGroup = group
                break
            }
            currentPosition += group.size
        }

        if (currentGroup != null) {
            val fileIndex = position - currentPosition
            val file = currentGroup[fileIndex]

            holder.filePathTextView.text = file.absolutePath

            holder.fileCheckBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) selectedFiles.add(file) else selectedFiles.remove(file)
            }

            holder.fileCheckBox.isChecked = selectedFiles.contains(file)

            // Apply indentation to duplicate items
            if (fileIndex > 0) {
                holder.itemView.updatePadding(left = indentation)
            } else {
                holder.itemView.updatePadding(left = 0) // No indentation for the "original" file
            }
        }
    }

    override fun getItemCount(): Int {
        var totalItems = 0
        for (group in duplicateFileGroups) {
            totalItems += group.size
        }
        return totalItems
    }

    fun getSelectedFiles(): Set<File> {
        return selectedFiles
    }
}