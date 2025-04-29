package k4ustu3h.dedupe.util

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.core.net.toUri

object PermissionUtils {

    fun checkManageStoragePermission(): Boolean {
        return Environment.isExternalStorageManager()
    }

    fun requestManageStoragePermission(
        activityResultLauncher: ActivityResultLauncher<Intent>, context: Context
    ) {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            intent.data =
                String.format("package:%s", context.applicationContext.packageName).toUri()
            activityResultLauncher.launch(intent)
        } catch (_: Exception) {
            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            activityResultLauncher.launch(intent)
        }
    }
}