package k4ustu3h.dedupe.components.card

import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.checkbox.MaterialCheckBox
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import k4ustu3h.dedupe.R
import k4ustu3h.dedupe.util.FileUtils
import java.io.File

class DuplicateFileCard(private val files: List<File>) :
    Item<DuplicateFileCard.DuplicateFileCardViewHolder>() {

    private val selectedFilesInternal = mutableSetOf<File>()

    override fun bind(viewHolder: DuplicateFileCardViewHolder, position: Int) {
        viewHolder.duplicateContainer.removeAllViews()

        if (files.isNotEmpty()) {
            val originalFile = files.first()
            val sdCard = "/storage/emulated/0/"

            val originalView = LayoutInflater.from(viewHolder.itemView.context)
                .inflate(R.layout.item_duplicate_entry, viewHolder.duplicateContainer, false)
            val originalCheckBox = originalView.findViewById<MaterialCheckBox>(R.id.fileCheckBox)
            val originalFileSize = originalView.findViewById<TextView>(R.id.fileSizeTextView)

            originalCheckBox.text = originalFile.absolutePath.replace(sdCard, "")
            originalFileSize.text = FileUtils.getFileSize(originalFile)
            originalCheckBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) selectedFilesInternal.add(originalFile) else selectedFilesInternal.remove(
                    originalFile
                )
            }
            originalCheckBox.isChecked = selectedFilesInternal.contains(originalFile)
            viewHolder.duplicateContainer.addView(originalView)

            for (i in 1 until files.size) {
                val duplicateFile = files[i]
                val duplicateView = LayoutInflater.from(viewHolder.itemView.context)
                    .inflate(R.layout.item_duplicate_entry, viewHolder.duplicateContainer, false)
                val duplicateCheckBox =
                    duplicateView.findViewById<MaterialCheckBox>(R.id.fileCheckBox)
                val duplicateFileSize = duplicateView.findViewById<TextView>(R.id.fileSizeTextView)

                duplicateCheckBox.text = duplicateFile.absolutePath.replace(sdCard, "")
                duplicateFileSize.text = FileUtils.getFileSize(duplicateFile)
                duplicateCheckBox.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) selectedFilesInternal.add(duplicateFile) else selectedFilesInternal.remove(
                        duplicateFile
                    )
                }
                duplicateCheckBox.isChecked = selectedFilesInternal.contains(duplicateFile)

                val params = duplicateView.layoutParams as LinearLayout.LayoutParams
                params.marginStart =
                    viewHolder.itemView.resources.getDimensionPixelOffset(R.dimen.duplicate_indentation)
                duplicateView.layoutParams = params

                val divider = LayoutInflater.from(viewHolder.itemView.context)
                    .inflate(R.layout.item_divider, viewHolder.duplicateContainer, false)
                val dividerParams = divider.layoutParams as LinearLayout.LayoutParams
                dividerParams.marginStart =
                    viewHolder.itemView.resources.getDimensionPixelOffset(R.dimen.duplicate_indentation)
                divider.layoutParams = dividerParams
                viewHolder.duplicateContainer.addView(divider)

                viewHolder.duplicateContainer.addView(duplicateView)
            }
        }
    }

    fun getSelectedFiles(): Set<File> = selectedFilesInternal

    override fun getLayout(): Int = R.layout.card_duplicate_group

    override fun createViewHolder(view: View): DuplicateFileCardViewHolder {
        return DuplicateFileCardViewHolder(view)
    }

    class DuplicateFileCardViewHolder(itemView: View) : GroupieViewHolder(itemView) {
        val duplicateContainer: LinearLayout = itemView.findViewById(R.id.duplicateContainer)
    }
}