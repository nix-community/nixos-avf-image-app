package io.mkg20001.nixosimage.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState

@Composable
fun HomeComposable(viewModel: HomeViewModel) {
    val state = viewModel.state.observeAsState().value
}