package k4ustu3h.dedupe.util

import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow

object FileUtils {

    private const val SIZE_LIMIT = 128 * 1024 * 1024

    fun traverseFiles(
        directory: File,
        fileMap: MutableMap<String, MutableList<File>>,
        enableFileSizeLimit: Boolean
    ) {
        val files = directory.listFiles()

        if (files == null) return

        for (file in files) {
            if (file.isDirectory) {
                traverseFiles(file, fileMap, enableFileSizeLimit)
            } else {
                Log.d("FileUtils", "Processing file: ${file.absolutePath}, size: ${file.length()}")
                if (!enableFileSizeLimit || file.length() <= SIZE_LIMIT) {
                    Log.d("FileUtils", "Including file for hashing: ${file.absolutePath}")
                    val hash = calculateFileHash(file)
                    if (hash != null) {
                        fileMap.getOrPut(hash) { mutableListOf() }.add(file)
                    }
                } else {
                    Log.d(
                        "FileUtils",
                        "Skipping large file (size limit): ${file.absolutePath}, size: ${file.length()}"
                    )
                }
            }
        }
    }

    private fun calculateFileHash(file: File): String? {
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
            Locale.getDefault(),
            "%.2f %s",
            bytes / 1024.0.pow(digitGroups.toDouble()),
            units[digitGroups]
        )
    }

    fun String.compareToNatural(other: String, ignoreCase: Boolean = false): Int {
        var i = 0
        var j = 0
        while (i < this.length || j < other.length) {
            val chunk1 = StringBuilder()
            while (i < this.length && this[i].isDigit()) {
                chunk1.append(this[i++])
            }
            val chunk2 = StringBuilder()
            while (j < other.length && other[j].isDigit()) {
                chunk2.append(other[j++])
            }

            if (chunk1.isNotEmpty() && chunk2.isNotEmpty()) {
                val n1 = chunk1.toString().toLongOrNull() ?: 0L
                val n2 = chunk2.toString().toLongOrNull() ?: 0L
                val comparison = n1.compareTo(n2)
                if (comparison != 0) return comparison
            } else {
                val char1 = if (i < this.length) this[i++] else null
                val char2 = if (j < other.length) other[j++] else null

                if (char1 == null && char2 == null) return 0
                if (char1 == null) return -1
                if (char2 == null) return 1

                val c1 = if (ignoreCase) char1.lowercaseChar() else char1
                val c2 = if (ignoreCase) char2.lowercaseChar() else char2

                val comparison = c1.compareTo(c2)
                if (comparison != 0) return comparison
            }
        }
        return 0
    }
}