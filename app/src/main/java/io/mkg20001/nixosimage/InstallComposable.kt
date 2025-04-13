package io.mkg20001.nixosimage

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.mkg20001.myapplication.ui.theme.NixosImageTheme

@Composable
fun InstallComposable(install: InstallMagic, modifier: Modifier) {
    val text = install.text.collectAsState().value
    val progress = install.progress.collectAsState().value

    Column(
        modifier
    )
        {
            Text(text = text, modifier = Modifier.fillMaxWidth().padding(12.dp))
            LinearProgressIndicator(progress = { progress.toFloat() / 100 }, modifier = Modifier.fillMaxWidth().padding(12.dp))
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstallView(magic: InstallMagic) {
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
            InstallComposable(magic, modifier = Modifier.padding(innerPadding))
        }
    }
}