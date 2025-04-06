package io.mkg20001.nixosimage.install

object MagiskInstallMethod: ImageInstallMethod {
    override val id = "magisk"

    override val displayString = "method_magisk"

    override fun isAvailable(): Boolean {
        // TODO: detect magisk
        return false
    }

    override fun installImage(image: String) {
        DebugInstallMethod.installImage(image)
        // TODO: do "magisk resetprop ro.debuggable 1; stop; start;"
    }

    override val needsCleanup: Boolean = true

    override fun doCleanup() {
        // TODO: undo the prop change
    }
}