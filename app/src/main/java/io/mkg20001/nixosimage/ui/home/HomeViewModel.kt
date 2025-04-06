package io.mkg20001.nixosimage.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.mkg20001.nixosimage.data.GitHubRelease
import io.mkg20001.nixosimage.install.ImageInstallMethod
import io.mkg20001.nixosimage.install.InstallMethods

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = ""
    }
    val text: LiveData<String> = _text

    private val _installMethods = MutableLiveData<List<ImageInstallMethod>>().apply {
        value = InstallMethods.availableMethods()
    }
    val installMethods: LiveData<List<ImageInstallMethod>> = _installMethods

    private val _imageReleases = MutableLiveData<List<GitHubRelease>>().apply {
        value = listOf()
    }
    val imageRelease: LiveData<List<GitHubRelease>> = _imageReleases
}