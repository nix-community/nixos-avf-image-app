package io.mkg20001.nixosimage

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState

@Composable
fun InstallComposable(install: InstallMagic) {
    val text = install.text.collectAsState().value
    val progress = install.progress.collectAsState().value

    Column {
        Text(text = text)
        LinearProgressIndicator(progress = progress.toFloat())
    }
}