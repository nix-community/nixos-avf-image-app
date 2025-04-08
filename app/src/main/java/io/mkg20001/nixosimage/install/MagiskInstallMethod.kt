package io.mkg20001.nixosimage.install

import android.content.Context
import io.mkg20001.nixosimage.R
import java.io.File
import kotlin.io.path.pathString

fun executeWithRoot(cmd: String): Boolean {
    return executeInShell("su -c '${cmd}")
}

fun executeInShell(cmd: String): Boolean {
    TODO("bla")
}

object MagiskInstallMethod: ImageInstallMethod {
    override val id = "magisk"

    override val display = R.string.method_magisk

    override fun isAvailable(): Boolean {
        // TODO: detect magisk
        return false
    }

    override suspend fun installImage(context: Context, image: File): Boolean {
        executeWithRoot("mkdir -p ${DebugInstallMethod.getSdcardPathForTesting().pathString}")
        executeWithRoot("cp ${image.path} ${DebugInstallMethod.fromSdCard().pathString}")
        executeWithRoot("magisk resetprop ro.debuggable 1; stop; start;")
        return true
    }

    override val needsExternalStorage: Boolean = false
    override val needsCleanup: Boolean = true

    override fun doCleanup() {
        // TODO: undo the prop change
    }
}