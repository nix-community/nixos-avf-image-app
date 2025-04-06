package io.mkg20001.nixosimage.install

object DebugInstallMethod: ImageInstallMethod {
    override val id = "debug"

    override val displayString = "method_debug"

    override fun isAvailable(): Boolean {
        return System.getProperty("ro.debuggable", "0") == "1"
    }

    override fun installImage(image: String) {
        // TODO: copy image to /sdcard/linux/images.tar.gz
    }
}
