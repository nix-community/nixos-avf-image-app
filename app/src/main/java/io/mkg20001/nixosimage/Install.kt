package io.mkg20001.nixosimage

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.activity.ComponentActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.lifecycle.lifecycleScope
import io.mkg20001.myapplication.ui.theme.NixosImageTheme
import io.mkg20001.nixosimage.data.GitHubReleaseAsset
import io.mkg20001.nixosimage.install.InstallMethods
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class Install : ComponentActivity() {
    var magic: InstallMagic? = null

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

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
            InstallView(magic!!)
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