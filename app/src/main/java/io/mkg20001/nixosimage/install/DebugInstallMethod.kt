package io.mkg20001.nixosimage.install

import android.os.Build

object DebugInstallMethod: ImageInstallMethod {
    override val id = "debug"

    override val displayString = "method_debug"

    override fun isAvailable(): Boolean {
        return Build.TYPE != null && (Build.TYPE.equals("userdebug") || Build.TYPE.equals("eng"))
    }

    override fun installImage(image: String) {
        // TODO: copy image to /sdcard/linux/images.tar.gz
    }
}
