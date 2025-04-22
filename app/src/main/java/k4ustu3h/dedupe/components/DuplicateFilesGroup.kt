package k4ustu3h.dedupe.components

import com.xwray.groupie.Group
import com.xwray.groupie.GroupDataObserver
import com.xwray.groupie.Item
import k4ustu3h.dedupe.components.card.DuplicateFileCard
import java.io.File

class DuplicateFilesGroup(private val files: List<File>) : Group {

    override fun getItemCount(): Int = 1

    override fun getItem(position: Int): Item<*> = DuplicateFileCard(files)

    override fun getPosition(item: Item<*>): Int = 0

    override fun registerGroupDataObserver(observer: GroupDataObserver) {}

    override fun unregisterGroupDataObserver(observer: GroupDataObserver) {}

    fun getSelectedFiles(): Set<File> {
        return (getItem(0) as? DuplicateFileCard)?.getSelectedFiles() ?: emptySet()
    }
}