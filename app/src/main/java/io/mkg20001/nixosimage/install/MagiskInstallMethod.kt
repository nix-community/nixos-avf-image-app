package io.mkg20001.nixosimage.install

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.topjohnwu.superuser.Shell
import io.mkg20001.nixosimage.BuildConfig
import io.mkg20001.nixosimage.R
import io.mkg20001.nixosimage.extra.ExtraImageUtils
import kotlinx.coroutines.flow.MutableStateFlow
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
    override val showOpenTerminalAgainBtn: Boolean = false

    override fun isAvailable(): Boolean {
        return Shell.getShell().isRoot
    }

    override suspend fun installImage(
        context: Context,
        image: File,
        assets: AssetManager,
        progress: MutableStateFlow<Int>
    ): Boolean {
        val shell = Shell.getShell()
        if (!shell.isRoot) {
            Log.e("Magisk", "acquired shell is not root, giving up")
            return false
        }

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
        executeWithRoot(ExtraImageUtils.RM_EXISTING)
        executeWithRoot("magisk resetprop ro.debuggable 1")
        // This will likely tear down the entire app, so we just start the activity right after
        executeWithRoot("stop; start; " + ExtraImageUtils.LAUNCH_INSTALLER)

        return true
    }

    override val needsExternalStorage: Boolean = false
    override val needsLaunchTerminalAfterwards: Boolean = false
    override val needsCleanup: Boolean = true

    override fun doCleanup() {
        // TODO: undo the prop change
    }
}