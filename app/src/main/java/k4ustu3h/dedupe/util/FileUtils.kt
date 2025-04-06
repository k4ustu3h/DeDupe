package k4ustu3h.dedupe.util

import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow

object FileUtils {

    /**
     * Traverses the file system using Depth-First Search (DFS) and calculates file hashes.
     *
     * @param directory The starting directory for traversal.
     * @param fileMap A map to store file hashes and their corresponding files.
     */
    fun traverseFiles(directory: File, fileMap: MutableMap<String, MutableList<File>>) {
        // Get the list of files and directories in the current directory.
        val files = directory.listFiles()

        // If the directory is empty or null, return.
        if (files == null) return

        // Iterate through each file and directory.
        for (file in files) {
            // If the current file is a directory, recursively traverse it.
            if (file.isDirectory) {
                traverseFiles(file, fileMap) // Recursive DFS call
            } else {
                // If the current file is a regular file, calculate its SHA-256 hash.
                val hash = calculateFileHash(file)

                // If the hash is not null, add the file to the file map.
                if (hash != null) {
                    fileMap.getOrPut(hash) { mutableListOf() }.add(file)
                }
            }
        }
    }

    /**
     * Calculates the SHA-256 hash of a file.
     *
     * @param file The file to calculate the hash for.
     * @return The SHA-256 hash as a hexadecimal string, or null if an error occurred.
     */
    fun calculateFileHash(file: File): String? {
        try {
            // Create a MessageDigest instance for SHA-256 hashing.
            val digest = MessageDigest.getInstance("SHA-256")

            // Create a FileInputStream to read the file's contents.
            val inputStream = FileInputStream(file)

            // Create a buffer to read data in chunks.
            val buffer = ByteArray(8192)

            // Read data from the file and update the MessageDigest.
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }

            // Get the hash bytes and convert them to a hexadecimal string.
            val hashBytes = digest.digest()
            return bytesToHex(hashBytes)
        } catch (e: Exception) {
            // Handle exceptions (e.g., FileNotFoundException, NoSuchAlgorithmException).
            e.printStackTrace()
            return null
        }
    }

    /**
     * Converts a byte array to a hexadecimal string.
     *
     * @param bytes The byte array to convert.
     * @return The hexadecimal string representation of the byte array.
     */
    private fun bytesToHex(bytes: ByteArray): String {
        // Create a character array to store the hexadecimal representation.
        val hexArray = CharArray(bytes.size * 2)

        // Iterate through each byte and convert it to two hexadecimal characters.
        for (j in bytes.indices) {
            val v = bytes[j].toInt() and 0xFF
            hexArray[j * 2] = "0123456789ABCDEF"[v shr 4]
            hexArray[j * 2 + 1] = "0123456789ABCDEF"[v and 0x0F]
        }

        // Return the hexadecimal string.
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
                val n1 = chunk1.toString().toInt()
                val n2 = chunk2.toString().toInt()
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