// DuplicateFilesGroup.kt
package k4ustu3h.dedupe

import com.xwray.groupie.Group
import com.xwray.groupie.GroupDataObserver
import java.io.File

class DuplicateFilesGroup(private val files: List<File>) : Group {

    override fun getItemCount(): Int = files.size

    override fun getItem(position: Int): com.xwray.groupie.Item<*> =
        DuplicateFileItem(files[position], position == 0)

    override fun getPosition(item: com.xwray.groupie.Item<*>): Int =
        files.indexOf((item as DuplicateFileItem).file)

    override fun registerGroupDataObserver(observer: GroupDataObserver) {
        // Not needed for this implementation, but required by Group interface
    }

    override fun unregisterGroupDataObserver(observer: GroupDataObserver) {
        // Not needed for this implementation, but required by Group interface
    }

    fun notifyChanged() {
        // Not needed for this implementation, but required by Group interface
    }

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