package io.mkg20001.nixosimage

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import io.mkg20001.nixosimage.data.GitHubReleaseAsset
import io.mkg20001.nixosimage.install.InstallMethods
import io.mkg20001.nixosimage.ui.theme.Red400
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
            InstallView {
                InstallComposable(magic!!)
            }
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
        setContent {
            InstallView {
                Text(
                    stringResource(R.string.toast_intent_error),
                    color = Red400,
                    modifier = Modifier.padding(24.dp).fillMaxWidth().fillMaxHeight()
                )
            }
        }
    }
}