package io.mkg20001.nixosimage.ui.install

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import io.mkg20001.nixosimage.R
import io.mkg20001.nixosimage.data.GitHubReleaseAsset
import io.mkg20001.nixosimage.data.downloadFile
import io.mkg20001.nixosimage.extra.ExtraImageUtils
import io.mkg20001.nixosimage.install.ImageInstallMethod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

fun OpenTerminal(applicationContext: Context) {
    val packageName = "com.android.virtualization.terminal"
    val className = "com.android.virtualization.terminal.MainActivity"

    val intent = Intent().apply {
        setClassName(packageName, className)
    }
    intent.flags = FLAG_ACTIVITY_NEW_TASK

    try {
        startActivity(applicationContext, intent, null)
    } catch (e: Exception) {
        e.printStackTrace()
        // Handle the case when the target activity is not found or other issues
    }
}


class InstallMagic(
    val applicationContext: Context,
    val method: ImageInstallMethod,
    val asset: GitHubReleaseAsset
) {
    private val _text = MutableStateFlow("")
    val text: StateFlow<String> = _text

    private val _progress = MutableStateFlow(0)
    val progress: StateFlow<Int> = _progress

    private val _done = MutableStateFlow(false)
    val done: StateFlow<Boolean> = _done

    suspend fun run() {
        val extra = ExtraImageUtils()

        updateStatus(R.string.install_step_downloading)
        _progress.tryEmit(0)

        // TODO: include methods needing cleanup properly
        Log.i("Download", "Downloading image")

        val file = downloadFile(
            context = applicationContext,
            fileUrl = asset.url,
            fileName = "image-cached-" + asset.id + "@" + asset.updatedAt
        ) { progress ->
            if (_progress.value != progress) {
                Log.d("Download", "Progress: $progress%")
                // You can update UI with LiveData or State here
                _progress.tryEmit(progress)
            }
        }

        if (file != null) {
            Log.i("Download", "File downloaded: ${file.absolutePath}")

            Log.i("Install", "Installing")
            updateStatus(R.string.install_step_installing)

            fun installOK() {
                Log.i("Install", "ok")
                _done.tryEmit(true)
                if (method.needsLaunchTerminalAfterwards) {
                    OpenTerminal(applicationContext)
                }
            }

            fun installFail() {
                Log.e("Install", "failed")
                errorOut()
            }

            if (method.needsImageClean)  {
                if (!extra.cleanupImage()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(applicationContext, R.string.remove_existing_image, Toast.LENGTH_LONG).show()
                    }
                }
            }

            try {
                val success = method.installImage(applicationContext, file, applicationContext.assets, _progress)

                if (success) {
                    installOK()
                } else {
                    installFail()
                }
            } catch(e: Exception) {
                e.printStackTrace()
                installFail()
            }
        } else {
            Log.e("Download", "Failed to download file")
            errorOut()
        }
    }

    fun updateStatus(task: Int) {
        val out = applicationContext.getString(R.string.install_task) + " " + applicationContext.getString(task) + "\n\n" +
                applicationContext.getString(R.string.install_version) + " " + asset.version + "\n\n" +
                applicationContext.getString(R.string.install_architecture) + " " + asset.arch

        _text.tryEmit(out)
    }

    fun errorOut() {
        // set status to "Error"
        _text.tryEmit("Error!")
    }
}