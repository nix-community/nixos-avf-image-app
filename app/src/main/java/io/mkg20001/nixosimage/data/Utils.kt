package io.mkg20001.nixosimage.data

import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FilterInputStream
import java.io.InputStream
import java.security.MessageDigest

fun copyFile(source: File, dest: File, onProgress: (Int) -> Unit) {
    ProgressStream(FileInputStream(source), source.length().toDouble(), 0, onProgress).use { input ->
        FileOutputStream(dest).use { output ->
            input.copyTo(output)
        }
    }
}

fun mkdirp(path: String): Boolean {
    val dir = File(path)
    return if (!dir.exists()) {
        dir.mkdirs() // returns true if the directories were created
    } else {
        dir.isDirectory // true if it already exists and is a directory
    }
}

class ProgressStream(val baseStream: InputStream, val totalSize: Double, var bytesRead: Long, val onProgress: (Int) -> Unit): FilterInputStream(baseStream) {
    override fun read(): Int = super.read().also {
        if (it != -1) updateProgress(1)
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int = super.read(b, off, len).also {
        if (it > 0) updateProgress(it)
    }

    fun updateProgress(count: Int) {
        bytesRead += count
        onProgress(((bytesRead / totalSize) * 100).toInt().coerceIn(0, 100))
    }
}

fun hexToByteArray(hex: String): ByteArray {
    val cleanHex = hex.replace(" ", "")
    require(cleanHex.length % 2 == 0) { "Hex string must have even length" }

    return ByteArray(cleanHex.length / 2) { i ->
        cleanHex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
    }
}

class DigestStream(val baseStream: InputStream, val digest: MessageDigest): FilterInputStream(baseStream) {
    override fun read(): Int = super.read().also {
        if (it != -1) {
            digest.update(it.toByte())
        }
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int = super.read(b, off, len).also {
       if (it > 0) {
           digest.update(b, off, it)
       }
    }

    fun validate(expectedHex: String): Boolean {
        val actual = digest.digest()
        return actual contentEquals hexToByteArray(expectedHex)
    }
}

fun clearOldFiles(dir: File) {
    val cutoff = System.currentTimeMillis() - 24 * 60 * 60 * 1000 // 1 day in milliseconds

    dir.listFiles()?.forEach { file ->
        if (file.isFile && file.lastModified() < cutoff) {
            Log.w("ClearCache", "Clearing old cache: $file")
            file.delete()
        }
    }
}