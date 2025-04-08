package io.mkg20001.nixosimage.install

import android.content.Context
import android.util.Log
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.Shell.GetShellCallback
import io.mkg20001.nixosimage.BuildConfig
import io.mkg20001.nixosimage.R
import java.io.File
import kotlin.io.path.pathString

fun initShell() {
    Shell.enableVerboseLogging = BuildConfig.DEBUG
    Shell.setDefaultBuilder(
        Shell.Builder.create()
            .setFlags(Shell.FLAG_MOUNT_MASTER)
            // .setInitializers(ShellInit::class.java)
            .setTimeout(10)
    )
}

object MagiskInstallMethod: ImageInstallMethod {
    override val id = "magisk"

    override val display = R.string.method_magisk

    override fun isAvailable(): Boolean {
        return Shell.isAppGrantedRoot() == true
    }

    override suspend fun installImage(context: Context, image: File): Boolean {
        Shell.getShell(GetShellCallback { shell: Shell? ->
            fun executeWithRoot(cmd: String): Boolean {
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

            // The main shell is now constructed and cached
            executeWithRoot("mkdir -p ${DebugInstallMethod.getSdcardPathForTesting().pathString}")
            executeWithRoot("cp ${image.path} ${DebugInstallMethod.fromSdCard().pathString}")
            executeWithRoot("magisk resetprop ro.debuggable 1")
            executeWithRoot("stop")
            executeWithRoot("start")
        })

        return true
    }

    override val needsExternalStorage: Boolean = false
    override val needsCleanup: Boolean = true

    override fun doCleanup() {
        // TODO: undo the prop change
    }
}