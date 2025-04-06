package k4ustu3h.dedupe.components

import com.xwray.groupie.Group
import com.xwray.groupie.GroupDataObserver
import com.xwray.groupie.Item
import k4ustu3h.dedupe.components.card.DuplicateFileCard
import java.io.File

class DuplicateFilesGroup(files: List<File>) : Group {

    private val duplicateFileCard = DuplicateFileCard(files)

    override fun getItemCount(): Int = 1

    override fun getItem(position: Int): Item<*> = duplicateFileCard

    override fun getPosition(item: Item<*>): Int = 0

    override fun registerGroupDataObserver(observer: GroupDataObserver) {}

    override fun unregisterGroupDataObserver(observer: GroupDataObserver) {}

    fun getSelectedFiles(): Set<File> {
        return duplicateFileCard.getSelectedFiles()
    }
}