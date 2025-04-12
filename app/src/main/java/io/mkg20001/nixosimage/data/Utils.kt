package io.mkg20001.nixosimage.data

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FilterInputStream
import java.io.InputStream

fun copyFile(source: File, dest: File, onProgress: (Int) -> Unit) {
    ProgressStream(FileInputStream(source), source.length().toDouble(), onProgress).use { input ->
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

class ProgressStream(val baseStream: InputStream, val totalSize: Double, val onProgress: (Int) -> Unit): FilterInputStream(baseStream) {
    var bytesRead = 0L

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