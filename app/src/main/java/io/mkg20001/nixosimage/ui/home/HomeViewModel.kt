package io.mkg20001.nixosimage.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.mkg20001.nixosimage.data.GitHubRelease
import io.mkg20001.nixosimage.data.GitHubReleaseClient
import io.mkg20001.nixosimage.install.ImageInstallMethod
import io.mkg20001.nixosimage.install.InstallMethods

class HomeViewModel : ViewModel() {

    private val _state = MutableLiveData<ImageViewState>().apply {
        value = ImageViewState.LOADING
    }
    val state: LiveData<ImageViewState> = _state

    private val _installMethods = MutableLiveData<List<ImageInstallMethod>>().apply {
        value = InstallMethods.availableMethods()
    }
    val installMethods: LiveData<List<ImageInstallMethod>> = _installMethods

    private val _imageReleases = MutableLiveData<List<GitHubRelease>>().apply {
        value = listOf()
    }
    val imageRelease: LiveData<List<GitHubRelease>> = _imageReleases

    suspend fun refresh() {
        _state.value = ImageViewState.LOADING

        try {
            _installMethods.value = InstallMethods.availableMethods()
            _imageReleases.value = GitHubReleaseClient.getReleases()

            _state.value = ImageViewState.READY
        } catch(e: Exception) {
            _state.value = ImageViewState.ERROR
        }

    }
}

enum class ImageViewState {
    LOADING,
    ERROR,
    READY
}