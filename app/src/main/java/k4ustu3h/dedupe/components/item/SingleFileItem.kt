package k4ustu3h.dedupe.components.item

import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import k4ustu3h.dedupe.R
import k4ustu3h.dedupe.databinding.SingleFileItemBinding
import k4ustu3h.dedupe.util.FileUtils
import java.io.File

class SingleFileItem(val file: File) : BindableItem<SingleFileItemBinding>() {
    override fun bind(viewBinding: SingleFileItemBinding, position: Int) {
        val trimmedPath = file.absolutePath.replace("/storage/emulated/0", "")
        val fileSize = FileUtils.getFileSize(file)
        viewBinding.fileNameTextView.text = trimmedPath
        viewBinding.fileSizeTextView.text = fileSize
    }

    override fun getLayout(): Int = R.layout.single_file_item

    override fun initializeViewBinding(view: View): SingleFileItemBinding {
        return SingleFileItemBinding.bind(view)
    }
}