package io.mkg20001.nixosimage

import android.os.Bundle
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
import io.mkg20001.nixosimage.databinding.ActivityInstallBinding
import io.mkg20001.nixosimage.databinding.FragmentHomeBinding
import io.mkg20001.nixosimage.install.ImageInstallMethod
import io.mkg20001.nixosimage.install.InstallMethods
import io.mkg20001.nixosimage.ui.home.HomeViewModel


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

        // TODO: downloaded = download(r.url)
        // TODO: method.installImage(downloaded)

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