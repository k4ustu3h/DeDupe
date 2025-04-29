package k4ustu3h.dedupe.transition

import android.transition.AutoTransition
import android.transition.TransitionSet
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.google.android.material.card.MaterialCardView
import k4ustu3h.dedupe.R

object ButtonTransitions {
    fun createButtonTransition(): TransitionSet {
        return TransitionSet().apply {
            ordering = TransitionSet.ORDERING_TOGETHER
            addTransition(AutoTransition().apply {
                duration = 300
                addTarget(R.id.deleteButton)
            })
            addTransition(AutoTransition().apply {
                duration = 300
                addTarget(R.id.scanButton)
            })
        }
    }

    fun applyScanButtonExpand(
        mainLayout: ConstraintLayout, buttonLayout: MaterialCardView, scanButton: Button
    ) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(mainLayout)
        constraintSet.connect(
            scanButton.id, ConstraintSet.START, buttonLayout.id, ConstraintSet.START
        )
        constraintSet.connect(
            scanButton.id, ConstraintSet.END, buttonLayout.id, ConstraintSet.END
        )
        constraintSet.constrainWidth(scanButton.id, ConstraintSet.MATCH_CONSTRAINT)
        constraintSet.applyTo(mainLayout)
    }

    fun applyScanButtonContract(
        mainLayout: ConstraintLayout,
        buttonLayout: MaterialCardView,
        scanButton: Button,
        deleteButton: Button
    ) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(mainLayout)
        constraintSet.connect(
            scanButton.id, ConstraintSet.START, buttonLayout.id, ConstraintSet.START
        )
        constraintSet.connect(
            scanButton.id, ConstraintSet.END, deleteButton.id, ConstraintSet.START
        )
        constraintSet.constrainWidth(scanButton.id, ConstraintSet.WRAP_CONTENT)
        constraintSet.applyTo(mainLayout)
    }
}