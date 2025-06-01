package k4ustu3h.safai.dedupe.util

import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow

object FileUtils {

    private const val TAG = "FileUtils"
    const val FILE_SIZE_LIMIT_MB = 128L
    const val FILE_SIZE_LIMIT_BYTES = FILE_SIZE_LIMIT_MB * 1024 * 1024
    private const val NOMEDIA_FILE = ".nomedia"

    fun traverseFiles(
        directory: File,
        fileMap: MutableMap<String, MutableList<File>>,
        applySizeLimit: Boolean = false
    ) {
        val files = directory.listFiles()

        if (files == null) {
            Log.w(TAG, "listFiles returned null for directory: ${directory.absolutePath}")
            return
        }

        for (file in files) {
            Log.v(TAG, "Processing file/directory: ${file.absolutePath}")
            if (file.name == NOMEDIA_FILE) {
                Log.v(TAG, "Skipping .nomedia file: ${file.absolutePath}")
                continue
            }
            if (file.isDirectory) {
                Log.v(TAG, "Traversing subdirectory: ${file.absolutePath}")
                traverseFiles(file, fileMap, applySizeLimit)
            } else {
                if (applySizeLimit && file.length() > FILE_SIZE_LIMIT_BYTES) {
                    Log.v(
                        TAG,
                        "Skipping file due to size limit: ${file.absolutePath} (${getFileSize(file)})"
                    )
                    continue
                }
                val hash = calculateFileHash(file)
                if (hash != null) {
                    fileMap.getOrPut(hash) { mutableListOf() }.add(file)
                    Log.d(
                        TAG,
                        "File added to map (hash: $hash): ${file.absolutePath} (${getFileSize(file)})"
                    )
                } else {
                    Log.e(TAG, "Could not calculate hash for file: ${file.absolutePath}")
                }
            }
        }
    }

    fun calculateFileHash(file: File): String? {
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            val inputStream = FileInputStream(file)
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
            val hashBytes = digest.digest()
            return bytesToHex(hashBytes)
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating hash for ${file.absolutePath}: ${e.message}")
            return null
        }
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val hexArray = CharArray(bytes.size * 2)
        for (j in bytes.indices) {
            val v = bytes[j].toInt() and 0xFF
            hexArray[j * 2] = "0123456789ABCDEF"[v shr 4]
            hexArray[j * 2 + 1] = "0123456789ABCDEF"[v and 0x0F]
        }
        return String(hexArray)
    }

    fun getFileSize(file: File): String {
        val bytes = file.length()
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt()
        return String.format(
            Locale.getDefault(),
            "%.2f %s",
            bytes / 1024.0.pow(digitGroups.toDouble()),
            units[digitGroups]
        )
    }
}