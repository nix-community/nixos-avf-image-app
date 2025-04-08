package io.mkg20001.nixosimage.install

import android.content.Context
import java.io.File

interface ImageInstallMethod {
    val id: String
    val display: Int

    fun isAvailable(): Boolean
    suspend fun installImage (context: Context, image: File): Boolean
    val needsCleanup: Boolean
        get() = false
    val needsExternalStorage: Boolean
        get() = true

    fun doCleanup() {
        throw RuntimeException("not applicable")
    }
}

object InstallMethods {
    val methods = listOf(DebugInstallMethod, MagiskInstallMethod)
    fun availableMethods(): List<ImageInstallMethod> {
        return methods.filter { it.isAvailable() }
    }
    fun getMethod(id: String): ImageInstallMethod? {
        return methods.filter { it.id == id }.getOrNull(0)
    }
}