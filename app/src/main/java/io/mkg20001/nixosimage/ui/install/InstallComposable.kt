package io.mkg20001.nixosimage.ui.install

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.jeziellago.compose.markdowntext.MarkdownText
import io.mkg20001.myapplication.ui.theme.NixosImageTheme
import io.mkg20001.nixosimage.R

@Composable
fun InstallComposable(install: InstallMagic) {
    val text = install.text.collectAsState().value
    val progress = install.progress.collectAsState().value

    Column {
        Text(text = text, modifier = Modifier.fillMaxWidth().padding(12.dp))
        LinearProgressIndicator(progress = { progress.toFloat() / 100 }, modifier = Modifier.fillMaxWidth().padding(12.dp))
        Instructions(install.method.id)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstallView(content: @Composable () -> Unit) {
    NixosImageTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    title = {
                        Text(stringResource(R.string.install))
                    }
                )
            },
        ) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding).testTag("install_ui")) {
                content()
            }
        }
    }
}

@Composable
fun Instructions(method: String) {
    val applicationContext = LocalContext.current.applicationContext

    fun GetContent(file: String): String {
        return String(applicationContext.assets.open("instructions_$file.md").readAllBytes(),
            Charsets.UTF_8)
    }

    val markdown = GetContent(method) + "\n" + GetContent("generic")

    MarkdownText(
        modifier = Modifier.padding(8.dp),
        markdown = markdown,
        onLinkClicked = {
            if (it == "terminal://") {
                OpenTerminal(applicationContext)
            }
        },
    )
}