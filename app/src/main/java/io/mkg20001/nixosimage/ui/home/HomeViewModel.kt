package io.mkg20001.nixosimage.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.mkg20001.nixosimage.data.GitHubReleaseAsset
import io.mkg20001.nixosimage.data.GitHubReleaseClient
import io.mkg20001.nixosimage.install.ImageInstallMethod
import io.mkg20001.nixosimage.install.InstallMethods
import io.mkg20001.nixosimage.BuildConfig

class HomeViewModel : ViewModel() {

    private val _state = MutableLiveData<ImageViewState>().apply {
        value = ImageViewState.LOADING
    }
    val state: LiveData<ImageViewState> = _state

    private val _installMethods = MutableLiveData<List<ImageInstallMethod>>().apply {
        value = listOf()
    }
    val installMethods: LiveData<List<ImageInstallMethod>> = _installMethods

    private val _imageReleases = MutableLiveData<List<GitHubReleaseAsset>>().apply {
        value = listOf()
    }
    val imageRelease: LiveData<List<GitHubReleaseAsset>> = _imageReleases

    suspend fun refresh() {
        _state.postValue(ImageViewState.LOADING)

        try {
            if (BuildConfig.ALLOW_ANY_METHOD) {
                _installMethods.postValue(InstallMethods.methods)
            } else {
                _installMethods.postValue(InstallMethods.availableMethods())
            }
            val rel = GitHubReleaseClient.getReleases()
            if (rel == null) {
                _state.postValue(ImageViewState.ERROR)
                return
            }
            _imageReleases.postValue(rel.map { it.getSupported() }.flatten())

            _state.postValue(ImageViewState.READY)
        } catch(e: Exception) {
            e.printStackTrace()
            _state.postValue(ImageViewState.ERROR)
        }

    }
}

enum class ImageViewState {
    LOADING,
    ERROR,
    READY
}