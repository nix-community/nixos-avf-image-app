package io.mkg20001.nixosimage.install

class MagiskInstallMethod {
    companion object: ImageInstallMethod {
        override fun isAvailable(): Boolean {
            // TODO: detect magisk
            return true
        }

        override fun installImage(image: String) {
            DebugInstallMethod.installImage(image)
            // TODO: do "magisk resetprop ro.debuggable 1; stop; start;"
        }

        override fun needsCleanup(): Boolean {
            return true
        }

        override fun doCleanup() {
            // TODO: undo the prop change
        }
    }
}