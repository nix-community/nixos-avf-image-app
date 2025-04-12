package io.mkg20001.nixosimage.install

import android.content.Context
import android.content.res.AssetManager
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File

interface ImageInstallMethod {
    val id: String
    val display: Int

    fun isAvailable(): Boolean
    suspend fun installImage (
        context: Context,
        image: File,
        assets: AssetManager,
        progress: MutableStateFlow<Int>
    ): Boolean
    val needsCleanup: Boolean
        get() = false
    val needsExternalStorage: Boolean
        get() = true
    val needsLaunchTerminalAfterwards: Boolean
        get() = true
    val needsImageClean: Boolean
        get() = true

    fun doCleanup() {
        throw RuntimeException("not applicable")
    }
}

object InstallMethods {
    val methods = listOf(DebugInstallMethod, MagiskInstallMethod, ReplaceInstallMethod)
    fun availableMethods(): List<ImageInstallMethod> {
        return methods.filter { it.isAvailable() }
    }
    fun getMethod(id: String): ImageInstallMethod? {
        return methods.filter { it.id == id }.getOrNull(0)
    }
}