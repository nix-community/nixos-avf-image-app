package io.mkg20001.nixosimage.data

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

fun copyFile(source: File, dest: File) {
    FileInputStream(source).use { input ->
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