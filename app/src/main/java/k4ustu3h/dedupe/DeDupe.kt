package k4ustu3h.dedupe

import android.app.Application
import com.google.android.material.color.DynamicColors

class DeDupe : Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}