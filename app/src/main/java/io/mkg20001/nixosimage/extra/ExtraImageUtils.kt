package io.mkg20001.nixosimage.extra

import android.util.Log
import com.topjohnwu.superuser.Shell

class ExtraImageUtils constructor() {
    companion object {
        val RM_EXISTING = "rm -rfv /data/data/com.android.virtualization.terminal/{files/nixos.log,files/debian.log,files/linux,vm/nixos,vm/debian}"
        val LAUNCH_INSTALLER = "am start -n com.android.virtualization.terminal/.InstallerActivity"
    }

    val shell = Shell.getShell()
    var hasRoot = shell.isRoot

    fun executeWithRoot(cmd: String): Boolean {
        if (!hasRoot) return false

        try {
            Log.w("Magisk", "execute with su: ${cmd}")
            val process = Shell.cmd(cmd).exec()
            Log.w("Magisk", " => res ${process.code}")
            process.err.forEach {
                Log.w("Magisk", " => err ${it}")
            }
            return process.code == 0
        } catch (e: InterruptedException) {
            e.printStackTrace()
            return false
        }
    }

    fun cleanupImage(): Boolean {
        return executeWithRoot(RM_EXISTING)
    }

    fun launchInstaller(): Boolean {
        return executeWithRoot(LAUNCH_INSTALLER)
    }
}