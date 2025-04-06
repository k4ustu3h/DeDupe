package k4ustu3h.dedupe.util

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Environment
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity

object PermissionUtils {

    const val MANAGE_STORAGE_REQUEST_CODE = 102

    fun checkAndRequestManageStoragePermission(
        activity: FragmentActivity,
        manageStorageActivityResultLauncher: ActivityResultLauncher<Intent>,
        onPermissionGranted: () -> Unit
    ) {
        if (!Environment.isExternalStorageManager()) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = String.format(
                    "package:%s", activity.applicationContext.packageName
                ).toUri()
                manageStorageActivityResultLauncher.launch(intent)
            } catch (e: Exception) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                manageStorageActivityResultLauncher.launch(intent)
            }
        } else {
            onPermissionGranted()
        }
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        grantResults: IntArray,
        onPermissionGranted: () -> Unit,
        onPermissionDenied: () -> Unit
    ) {
        if (requestCode == MANAGE_STORAGE_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted()
            } else {
                onPermissionDenied()
            }
        }
    }
}