package k4ustu3h.dedupe.transition

import android.transition.Fade
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup

object ViewTransitions {
    private const val DEFAULT_DURATION = 300L

    fun fadeOut(view: View, duration: Long = DEFAULT_DURATION) {
        TransitionManager.beginDelayedTransition(view.parent as ViewGroup, Fade(Fade.OUT).apply { this.duration = duration })
        view.visibility = View.GONE
    }

    fun fadeIn(view: View, duration: Long = DEFAULT_DURATION) {
        TransitionManager.beginDelayedTransition(view.parent as ViewGroup, Fade(Fade.IN).apply { this.duration = duration })
        view.visibility = View.VISIBLE
    }
}