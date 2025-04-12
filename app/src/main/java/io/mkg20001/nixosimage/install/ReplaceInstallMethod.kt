package io.mkg20001.nixosimage.install

import android.content.Context
import android.content.res.AssetManager
import android.os.Environment
import android.util.Log
import android.widget.Toast
import io.mkg20001.nixosimage.R
import io.mkg20001.nixosimage.data.copyFile
import io.mkg20001.nixosimage.data.mkdirp
import io.mkg20001.nixosimage.install.DebugInstallMethod.fromSdCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.IOException
import org.apache.commons.codec.Resources.getInputStream
import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

object ReplaceInstallMethod: ImageInstallMethod {
    override val id = "replace"

    override val display = R.string.method_replace

    override fun isAvailable(): Boolean {
        // no special requirements
        return true
    }

    fun getImageDownloadsDir(): Path {
        return Environment.getExternalStoragePublicDirectory("Download/image").toPath()
    }

    @Throws(IOException::class)
    fun installTo(source: File, dir: Path) {
        Log.i("ReplaceInstall", "Extracting. source: $source, destination: $dir")
        TarArchiveInputStream(GzipCompressorInputStream(source.inputStream())).use { tarStream ->
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
        Log.i("ReplaceInstall", "Done extracting!")
    }


    override suspend fun installImage(context: Context, image: File, assets: AssetManager): Boolean {

        val dir = getImageDownloadsDir()

        if (!mkdirp(dir.toString())) {
            return false
        }

        withContext(Dispatchers.IO) {
            Log.i("ReplaceInstall", "Scripts")
            listOf("replace.sh", "post_setup.sh").forEach {
                val file = File(dir.toFile(), it)
                assets.open(it).use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                file.setExecutable(true)
            }

            // extract image to /sdcard/Download/image
            installTo(image, dir)
        }

        // tell user to run "bash /mnt/shared/image/replace.sh"
        Toast.makeText(context, R.string.toast_replace_script, Toast.LENGTH_LONG)

        Log.i("ReplaceInstall", "Done!")

        return true
    }

    override val needsCleanup: Boolean = true

    override fun doCleanup() {
        // TODO: delete /sdcard/Download/image
    }
}
