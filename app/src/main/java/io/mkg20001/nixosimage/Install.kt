package io.mkg20001.nixosimage

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.mkg20001.nixosimage.data.GitHubReleaseAsset
import io.mkg20001.nixosimage.install.InstallMethods
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class Install : AppCompatActivity() {
    var magic: InstallMagic? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /* enableEdgeToEdge()
        binding = ActivityInstallBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_install)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        } */

        val b = intent.extras ?: return errorOut()

        val asset = b.getSerializable("image", GitHubReleaseAsset::class.java)
            ?: return errorOut()

        val m: String = b.getString("method")
            ?: return errorOut()
        val method = InstallMethods.getMethod(m)
            ?: return errorOut()

        magic = InstallMagic(applicationContext, method, asset)

        Log.w("Install", "launch activity composable")
        setContent {
            InstallComposable(magic!!)
        }

        lifecycleScope.launch {
            bg()
        }
    }

    suspend fun bg() {
        withContext(Dispatchers.IO) {
            magic!!.run()
        }
    }

    fun errorOut() {
        Toast.makeText(applicationContext, "Error during load of Intent!", Toast.LENGTH_LONG).show()
    }
}