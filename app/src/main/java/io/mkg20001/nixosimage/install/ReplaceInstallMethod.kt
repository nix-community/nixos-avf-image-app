package io.mkg20001.nixosimage.install

import android.content.Context
import android.content.res.AssetManager
import android.os.Environment
import android.util.Log
import android.widget.Toast
import io.mkg20001.nixosimage.R
import io.mkg20001.nixosimage.data.ProgressStream
import io.mkg20001.nixosimage.data.mkdirp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import okio.IOException
import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

object ReplaceInstallMethod: ImageInstallMethod {
    val TAG = "ReplaceInstall"

    override val id = "replace"

    override val display = R.string.method_replace
    override val needsImageClean: Boolean = false

    override fun isAvailable(): Boolean {
        // no special requirements
        return true
    }

    fun getImageDownloadsDir(): Path {
        return Environment.getExternalStoragePublicDirectory("Download/image").toPath()
    }

    fun getImageAlternateDir(): Path {
        return Environment.getExternalStoragePublicDirectory("image").toPath()
    }

    @Throws(IOException::class)
    fun installTo(source: File, dir: Path, onProgress: (Int) -> Unit) {
        Log.i(TAG, "Extracting. source: $source, destination: $dir")

        val progressStream = ProgressStream(source.inputStream(), source.length().toDouble(), 0, onProgress)

        TarArchiveInputStream(GzipCompressorInputStream(progressStream)).use { tarStream ->
            Files.createDirectories(dir)
            var entry: ArchiveEntry?
            while ((tarStream.nextEntry.also { entry = it }) != null) {
                val to = dir.resolve(entry!!.name)
                if (Files.isDirectory(to)) {
                    Files.createDirectories(to)
                    continue
                }
                Files.copy(tarStream, to, StandardCopyOption.REPLACE_EXISTING)
            }
        }
        Log.i(TAG, "Done extracting!")
    }

    override suspend fun installImage(
        context: Context,
        image: File,
        assets: AssetManager,
        progress: MutableStateFlow<Int>
    ): Boolean {

        val dir = getImageDownloadsDir()
        val altDir = getImageAlternateDir()

        if (!mkdirp(dir.toString())) {
            return false
        }

        if (!mkdirp(altDir.toString())) {
            return false
        }

        withContext(Dispatchers.IO) {
            Log.i(TAG, "Scripts")

            // extract image to /sdcard/Download/image
            installTo(image, dir) {
                if (progress.value != it) {
                    Log.d(TAG, "Extract progress: $it%")
                    progress.tryEmit(it)
                }
            }

            // force scripts to be executable
            // also copy to alt location, for qpr3 fix
            listOf("replace.sh").forEach {
                val file = File(dir.toFile(), it)
                file.setExecutable(true)
                val altFile = File(altDir.toFile(), it)
                altFile.writeBytes(file.readBytes())
                altFile.setExecutable(true)
            }
        }

        withContext(Dispatchers.Main) {
            // tell user to run "bash /mnt/shared/image/replace.sh"
            Toast.makeText(context, R.string.toast_replace_script, Toast.LENGTH_LONG).show()
        }

        Log.i(TAG, "Done!")

        return true
    }
}
