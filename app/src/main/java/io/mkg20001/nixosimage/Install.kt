package io.mkg20001.nixosimage

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import io.mkg20001.nixosimage.data.GitHubReleaseAsset
import io.mkg20001.nixosimage.data.downloadFile
import io.mkg20001.nixosimage.databinding.ActivityInstallBinding
import io.mkg20001.nixosimage.databinding.FragmentHomeBinding
import io.mkg20001.nixosimage.install.ImageInstallMethod
import io.mkg20001.nixosimage.install.InstallMethods
import io.mkg20001.nixosimage.ui.home.HomeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class Install : AppCompatActivity() {
    var method: ImageInstallMethod? = null
    var asset: GitHubReleaseAsset? = null

    private lateinit var binding: ActivityInstallBinding

    private val _text = MutableLiveData<String>().apply {
        value = ""
    }
    val text: LiveData<String> = _text

    private val _progress = MutableLiveData<Int>().apply {
        value = 0
    }
    val progress: LiveData<Int> = _progress

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_install)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding = ActivityInstallBinding.inflate(layoutInflater)

        val b = intent.extras ?: return errorOut()

        asset = b.getSerializable("image", GitHubReleaseAsset::class.java)
            ?: return errorOut()

        val m: String = b.getString("method")
            ?: return errorOut()
        method = InstallMethods.getMethod(m)
            ?: return errorOut()

        text.observeForever {
            binding.status.text = it
        }
        progress.observeForever {
            binding.progress.progress = it
        }

        lifecycleScope.launch {
            // TODO: download to cache, re-use downloads on failure and also include methods needing cleanup properly
            val file = downloadFile(
                context = applicationContext,
                fileUrl = asset!!.url,
                fileName = "images.tar.gz"
            ) { progress ->
                if (_progress.value != progress) {
                    Log.d("Download", "Progress: $progress%")
                    // You can update UI with LiveData or State here
                    _progress.postValue(progress)
                }
            }

            if (file != null) {
                Log.d("Download", "File downloaded: ${file.absolutePath}")

                Log.d("Install", "Installing")
                updateStatus(R.string.install_step_installing)

                method!!.installImage(file.absolutePath)
            } else {
                Log.e("Download", "Failed to download file")
            }
        }

        doInstall()
    }

    fun doInstall() {
        updateStatus(R.string.install_step_downloading)
        _progress.postValue(20)
    }

    fun updateStatus(task: Int) {
        val out = getString(R.string.install_task) + " " + getString(task) + "\n\n" +
                getString(R.string.install_version) + " " + asset!!.version + "\n\n" +
                getString(R.string.install_architecture) + " " + asset!!.arch

        _text.postValue(out)
    }

    fun errorOut() {
        // set status to "Error"
        _text.postValue("Error!")
    }
}