package k4ustu3h.dedupe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import k4ustu3h.dedupe.databinding.BottomSheetSortBinding

class SortBottomSheetFragment(
    private val currentSortMode: MainActivity.SortMode,
    private val onSortOptionSelected: (MainActivity.SortMode) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetSortBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetSortBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateSelectionState(currentSortMode)

        binding.sortByNameLayout.setOnClickListener {
            onSortOptionSelected(MainActivity.SortMode.NAME)
            updateSelectionState(MainActivity.SortMode.NAME)
            dismiss()
        }

        binding.sortBySizeLayout.setOnClickListener {
            onSortOptionSelected(MainActivity.SortMode.SIZE)
            updateSelectionState(MainActivity.SortMode.SIZE)
            dismiss()
        }
    }

    private fun updateSelectionState(sortMode: MainActivity.SortMode) {
        val iconWidth = resources.getDimensionPixelOffset(R.dimen.sort_icon_width)

        binding.sortByNameIcon.isVisible = sortMode == MainActivity.SortMode.NAME
        binding.sortByNameTextView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            marginStart = if (sortMode == MainActivity.SortMode.NAME) 0 else iconWidth
        }

        binding.sortBySizeIcon.isVisible = sortMode == MainActivity.SortMode.SIZE
        binding.sortBySizeTextView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            marginStart = if (sortMode == MainActivity.SortMode.SIZE) 0 else iconWidth
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}