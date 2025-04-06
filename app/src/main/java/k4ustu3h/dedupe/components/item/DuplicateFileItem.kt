package k4ustu3h.dedupe.components.item

import android.graphics.Typeface
import android.view.View
import androidx.core.view.updatePadding
import com.xwray.groupie.viewbinding.BindableItem
import k4ustu3h.dedupe.R
import k4ustu3h.dedupe.databinding.TreeDuplicateItemBinding
import k4ustu3h.dedupe.util.FileUtils
import java.io.File

class DuplicateFileItem(val file: File, private val isOriginal: Boolean) :
    BindableItem<TreeDuplicateItemBinding>() {

    private var isSelected = false // Track selection state

    override fun bind(viewBinding: TreeDuplicateItemBinding, position: Int) {
        // Trim the file path
        val sdCard = "/storage/emulated/0/"
        val trimmedPath = file.absolutePath.replace(sdCard, "")
        val fileSize = FileUtils.getFileSize(file) // Get file size

        viewBinding.filePathTextView.text = trimmedPath
        viewBinding.fileSizeTextView.text = fileSize // Display file size
        viewBinding.fileCheckBox.visibility = View.VISIBLE

        if (isOriginal) {
            viewBinding.root.updatePadding(left = 0)
            viewBinding.filePathTextView.setTypeface(null, Typeface.BOLD) // Set bold text
        } else {
            viewBinding.root.updatePadding(left = 64) // Indentation
            viewBinding.filePathTextView.setTypeface(null, Typeface.NORMAL) // Ensure normal text
        }

        // Checkbox logic
        viewBinding.fileCheckBox.setOnCheckedChangeListener { _, isChecked ->
            isSelected = isChecked
        }

        viewBinding.fileCheckBox.isChecked = isSelected
    }

    override fun getLayout(): Int = R.layout.tree_duplicate_item

    override fun initializeViewBinding(view: View): TreeDuplicateItemBinding {
        return TreeDuplicateItemBinding.bind(view)
    }

    fun isSelected(): Boolean {
        return isSelected
    }
}