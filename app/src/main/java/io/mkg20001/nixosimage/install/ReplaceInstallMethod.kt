package io.mkg20001.nixosimage.install

import android.content.Context
import android.content.res.AssetManager
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import io.mkg20001.nixosimage.R
import io.mkg20001.nixosimage.data.mkdirp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import okio.IOException
import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import java.io.File
import java.io.FilterInputStream
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

    @Throws(IOException::class)
    fun installTo(source: File, dir: Path, onProgress: (Int) -> Unit) {
        Log.i(TAG, "Extracting. source: $source, destination: $dir")

        val progressStream = source.inputStream().let { baseStream ->
            object : FilterInputStream(baseStream) {
                val totalSize = source.length().toDouble()
                var bytesRead = 0L

                override fun read(): Int = super.read().also {
                    if (it != -1) updateProgress(1)
                }

                override fun read(b: ByteArray, off: Int, len: Int): Int = super.read(b, off, len).also {
                    if (it > 0) updateProgress(it)
                }

                fun updateProgress(count: Int) {
                    bytesRead += count
                    onProgress(((bytesRead / totalSize) * 100).toInt().coerceIn(0, 100))
                }
            }
        }

        TarArchiveInputStream(GzipCompressorInputStream(progressStream)).use { tarStream ->
            Files.createDirectories(dir)
            var entry: ArchiveEntry?
            while ((tarStream.nextEntry.also { entry = it }) != null) {
                val to = dir.resolve(entry!!.getName())
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

        if (!mkdirp(dir.toString())) {
            return false
        }

        withContext(Dispatchers.IO) {
            Log.i(TAG, "Scripts")
            listOf("replace.sh").forEach {
                val file = File(dir.toFile(), it)
                assets.open(it).use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                file.setExecutable(true)
            }

            // extract image to /sdcard/Download/image
            installTo(image, dir) {
                if (progress.value != it) {
                    Log.d(TAG, "Extract progress: $it%")
                    progress.tryEmit(it)
                }
            }
        }

        // tell user to run "bash /mnt/shared/image/replace.sh"
        Toast.makeText(context, R.string.toast_replace_script, Toast.LENGTH_LONG).show()

        Log.i(TAG, "Done!")

        return true
    }
}
