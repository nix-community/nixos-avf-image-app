package io.mkg20001.nixosimage.data

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.network.okHttpClient
import okhttp3.OkHttpClient
import io.mkg20001.nixosimage.GetReleasesQuery

val regexTag = Regex("^nixos-(?<version>[a-z0-9.]+)$")
val regexImage = Regex("^image-(?<version>[a-z0-9.]+)-(?<arch>[a-z0-9_-]+).tar.gz$")

class GitHubRelease constructor(
    val tagName: String,
    val assets: List<GitHubReleaseAsset>
) {
    var nixosVersion = ""

    // nixos-24.11
    init {
        val res = regexTag.matchEntire(tagName)
        if (res != null) {
            nixosVersion = res.groups.get("version").toString()
        }
    }
}

class GitHubReleaseAsset constructor(
    val name: String,
    var url: String,
) {
    var arch = ""
    var version = ""

    // image-unstable-aarch64.tar.gz
    init {
        val res = regexImage.matchEntire(name)
        if (res != null) {
            version = res.groups.get("version").toString()
            arch = res.groups.get("arch").toString()
        }
    }
}


val apolloClient = ApolloClient.Builder()
    .serverUrl("https://api.github.com/graphql")
    .okHttpClient(
        OkHttpClient.Builder()
            .build()
    )
    .build()

object GitHubReleaseClient {
    suspend fun getReleases(): List<GitHubRelease> {
        val resp = apolloClient.query(GetReleasesQuery()).execute()

        return resp.data!!.repository!!.releases.nodes!!.map {
            GitHubRelease(
                it!!.tagName,
                it.releaseAssets.nodes!!.map {
                    GitHubReleaseAsset(it!!.name, it.url.toString())
                }.filter { it.version != "" }
            )
        }.filter { it.nixosVersion != "" }
    }
}