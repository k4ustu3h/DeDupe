// FileUtils.kt
package k4ustu3h.dedupe.utils

import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import java.util.PriorityQueue
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
        if (files == null) return;

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

    /**
     * Prioritizes files for deletion based on a cost function.
     *
     * @param files The list of files to prioritize.
     * @param costFunction A function that calculates the cost of deleting a file.
     * @return A list of files in prioritized order (ascending cost).
     */
    fun prioritizeFilesForDeletion(files: List<File>, costFunction: (File) -> Int): List<File> {
        // Create a priority queue to store files and their costs.
        val priorityQueue = PriorityQueue<Pair<File, Int>>(compareBy { it.second })

        // Iterate through each file and add it to the priority queue with its cost.
        for (file in files) {
            val cost = costFunction(file)
            priorityQueue.offer(Pair(file, cost))
        }

        // Create a list to store the prioritized files.
        val prioritizedFiles = mutableListOf<File>()

        // Poll files from the priority queue and add them to the prioritized list.
        while (priorityQueue.isNotEmpty()) {
            prioritizedFiles.add(priorityQueue.poll().first)
        }

        // Return the list of prioritized files.
        return prioritizedFiles
    }

    /**
     * Example cost function: Cost based on file size (larger files have higher cost).
     *
     * @param file The file to calculate the cost for.
     * @return The cost of deleting the file.
     */
    fun exampleCost(file: File): Int {
        // Calculate the cost based on the file's length (size).
        return file.length().toInt()
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