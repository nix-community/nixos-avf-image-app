package io.mkg20001.nixosimage.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
fun HomeComposable(viewModel: HomeViewModel) {
    val state = remember { mutableStateOf(ImageViewState.LOADING) }
}