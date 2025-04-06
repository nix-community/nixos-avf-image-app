package io.mkg20001.nixosimage.install

class DebugInstallMethod {
    companion object: ImageInstallMethod {
        override fun isAvailable(): Boolean {
            return System.getProperty("ro.debuggable", "0") == "1"
        }

        override fun installImage(image: String) {
            // TODO: copy image to /sdcard/linux/images.tar.gz
        }

        override fun needsCleanup(): Boolean {
            return false
        }

        override fun doCleanup() {
            throw RuntimeException("not applicable")
        }
    }
}