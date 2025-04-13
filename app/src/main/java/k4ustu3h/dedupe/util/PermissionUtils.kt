package k4ustu3h.dedupe.util

import android.content.pm.PackageManager
import android.os.Environment

object PermissionUtils {

    fun isManageExternalStorageGranted(): Boolean {
        return (Environment.isExternalStorageManager())
    }

    fun onRequestPermissionsResult(
        grantResults: IntArray, onPermissionGranted: () -> Unit, onPermissionDenied: () -> Unit
    ) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            onPermissionGranted()
        } else {
            onPermissionDenied()
        }
    }
}