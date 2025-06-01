package k4ustu3h.safai

import android.app.Application
import com.google.android.material.color.DynamicColors

class Safai : Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}