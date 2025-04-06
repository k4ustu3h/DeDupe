package k4ustu3h.dedupe.utils

import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import kotlin.math.log10
import kotlin.math.pow

object FileUtils {

    fun traverseFiles(directory: File, fileMap: MutableMap<String, MutableList<File>>) {
        val files = directory.listFiles()

        if (files == null) return

        for (file in files) {
            if (file.isDirectory) {
                traverseFiles(file, fileMap)
            } else {
                val hash = calculateFileHash(file)

                if (hash != null) {
                    fileMap.getOrPut(hash) { mutableListOf() }.add(file)
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
            e.printStackTrace()
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
            "%.2f %s", bytes / 1024.0.pow(digitGroups.toDouble()), units[digitGroups]
        )
    }
}