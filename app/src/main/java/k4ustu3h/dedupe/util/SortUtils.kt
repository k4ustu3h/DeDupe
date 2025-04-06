package k4ustu3h.dedupe.util

import com.xwray.groupie.Item
import k4ustu3h.dedupe.components.item.DuplicateFileItem
import k4ustu3h.dedupe.components.item.SingleFileItem
import k4ustu3h.dedupe.util.FileUtils.compareToNatural

object SortUtils {

    fun compareByName(): Comparator<Item<*>> = Comparator { item1, item2 ->
        when {
            item1 is DuplicateFileItem && item2 is DuplicateFileItem -> {
                item1.file.name.compareToNatural(item2.file.name, true)
            }

            item1 is SingleFileItem && item2 is SingleFileItem -> {
                item1.file.name.compareToNatural(item2.file.name, true)
            }

            item1 is DuplicateFileItem && item2 is SingleFileItem -> {
                item1.file.name.compareToNatural(item2.file.name, true)
            }

            item1 is SingleFileItem && item2 is DuplicateFileItem -> {
                item1.file.name.compareToNatural(item2.file.name, true)
            }

            else -> 0
        }
    }

    fun compareBySize(): Comparator<Item<*>> = Comparator { item1, item2 ->
        val size1 = when (item1) {
            is DuplicateFileItem -> item1.file.length()
            is SingleFileItem -> item1.file.length()
            else -> 0
        }
        val size2 = when (item2) {
            is DuplicateFileItem -> item2.file.length()
            is SingleFileItem -> item2.file.length()
            else -> 0
        }
        size1.compareTo(size2)
    }

    fun sortItems(
        items: MutableList<Item<*>>, comparator: Comparator<Item<*>>?, isAscending: Boolean
    ) {
        if (comparator != null) {
            val finalComparator = if (isAscending) {
                comparator
            } else {
                comparator.reversed()
            }
            items.sortWith(finalComparator)
        } else {
            items.sortWith(compareByName())
        }
    }
}