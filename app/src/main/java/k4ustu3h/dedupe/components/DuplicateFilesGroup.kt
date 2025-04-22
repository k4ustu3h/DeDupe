package k4ustu3h.dedupe.components

import com.xwray.groupie.Group
import com.xwray.groupie.GroupDataObserver
import com.xwray.groupie.Item
import k4ustu3h.dedupe.components.item.DuplicateFileItem
import java.io.File

class DuplicateFilesGroup(private val files: List<File>) : Group {

    override fun getItemCount(): Int = files.size

    override fun getItem(position: Int): Item<*> =
        DuplicateFileItem(files[position], position == 0)

    override fun getPosition(item: Item<*>): Int =
        files.indexOf((item as DuplicateFileItem).file)

    override fun registerGroupDataObserver(observer: GroupDataObserver) {}

    override fun unregisterGroupDataObserver(observer: GroupDataObserver) {}

    fun getSelectedFiles(): Set<File> {
        val selectedFiles = mutableSetOf<File>()
        for (i in 0 until itemCount) {
            val item = getItem(i) as? DuplicateFileItem
            if (item?.isSelected() == true) {
                selectedFiles.add(item.file)
            }
        }
        return selectedFiles
    }
}