package io.mkg20001.nixosimage.install

interface ImageInstallMethod {
    val id: String
    val displayString: String

    fun isAvailable(): Boolean
    fun installImage (image: String)
    val needsCleanup: Boolean
        get() = false

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