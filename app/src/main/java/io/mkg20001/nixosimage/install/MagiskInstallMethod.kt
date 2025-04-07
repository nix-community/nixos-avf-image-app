package io.mkg20001.nixosimage.install

import android.content.Context
import io.mkg20001.nixosimage.R
import java.io.File

object MagiskInstallMethod: ImageInstallMethod {
    override val id = "magisk"

    override val display = R.string.method_magisk

    override fun isAvailable(): Boolean {
        // TODO: detect magisk
        return false
    }

    override fun installImage(context: Context, image: File): Boolean {
        if (!DebugInstallMethod.installImage(context, image)) {
            return false
        }
        // TODO: do "magisk resetprop ro.debuggable 1; stop; start;"
        return false
    }

    override val needsCleanup: Boolean = true

    override fun doCleanup() {
        // TODO: undo the prop change
    }
}