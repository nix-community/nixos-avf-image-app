package io.mkg20001.nixosimage.install

import android.content.Context
import android.content.res.AssetManager
import android.os.Build
import android.os.Environment
import androidx.lifecycle.MutableLiveData
import io.mkg20001.nixosimage.R
import io.mkg20001.nixosimage.data.copyFile
import io.mkg20001.nixosimage.data.mkdirp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.IOException
import java.io.File
import java.nio.file.Path

object DebugInstallMethod: ImageInstallMethod {
    override val id = "debug"

    override val display = R.string.method_debug

    override fun isAvailable(): Boolean {
        return Build.TYPE != null && (Build.TYPE.equals("userdebug") || Build.TYPE.equals("eng"))
    }

    // mostly verbatim from packages/modules/Virtualization/ android/TerminalApp/java/com/android/virtualization/terminal/ImageArchive.kt
    private const val DIR_IN_SDCARD = "linux"
    private const val ARCHIVE_NAME = "images.tar.gz"
    fun getSdcardPathForTesting(): Path {
        return Environment.getExternalStoragePublicDirectory(DIR_IN_SDCARD).toPath()
    }
    fun fromSdCard(): Path {
        return getSdcardPathForTesting().resolve(ARCHIVE_NAME)
    }

    override suspend fun installImage(
        context: Context,
        image: File,
        assets: AssetManager,
        progress: MutableLiveData<Int>
    ): Boolean {
        try {
            if (!mkdirp(getSdcardPathForTesting().toAbsolutePath().toString())) {
                return false
            }
            withContext(Dispatchers.IO) {
                copyFile(image, fromSdCard().toFile())
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }

        return true
    }
}
