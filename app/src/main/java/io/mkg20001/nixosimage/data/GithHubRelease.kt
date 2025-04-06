package io.mkg20001.nixosimage.data

data class GithHubRelease(
    val tagName: String
)

data class GitHubReleaseAsset(
    val name: String,
    var url: String
)