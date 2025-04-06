package io.mkg20001.nixosimage

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import io.mkg20001.nixosimage.data.GitHubReleaseAsset
import io.mkg20001.nixosimage.install.InstallMethods

class Install : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_install)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val b = intent.extras ?: return errorOut()

        val r: GitHubReleaseAsset = b.getSerializable("image", GitHubReleaseAsset::class.java)
            ?: return errorOut()
        val m: String = b.getString("method")
            ?: return errorOut()

        val method = InstallMethods.getMethod(m)
            ?: return errorOut()

        // TODO: downloaded = download(r.url)
        // TODO: method.installImage(downloaded)
    }

    fun errorOut() {
        // set status to "Error"
    }
}