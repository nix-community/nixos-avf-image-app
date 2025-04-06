package io.mkg20001.nixosimage.data

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.network.okHttpClient
import okhttp3.OkHttpClient
import io.mkg20001.nixosimage.GetReleasesQuery
import okhttp3.Interceptor
import okhttp3.Response
import java.io.Serializable

private val regexTag = Regex("^nixos-(?<version>[a-z0-9.]+)$")
private val regexImage = Regex("^image-(?<version>[a-z0-9.]+)-(?<arch>[a-z0-9_-]+).tar.gz$")

private val currentArch = System.getProperty("os.arch")
val WANTED_ARCH = when(currentArch) {
    "arm64" -> "aarch64"
    else -> currentArch
}

data class GitHubRelease constructor(
    val tagName: String,
    val assets: List<GitHubReleaseAsset>
) {
    var nixosVersion = ""

    // nixos-24.11
    init {
        val res = regexTag.matchEntire(tagName)
        if (res != null) {
            nixosVersion = res.groups["version"]!!.value
        }
    }

    fun hasAnyForArch(): Boolean {
        return assets.any { it.isForCurrentArch() }
    }

    fun getForArch(): GitHubReleaseAsset {
        return assets.first { it.isForCurrentArch() }
    }
}

data class GitHubReleaseAsset(
    val name: String,
    var url: String,
): Serializable {
    var arch = ""
    var version = ""

    // image-unstable-aarch64.tar.gz
    init {
        val res = regexImage.matchEntire(name)
        if (res != null) {
            version = res.groups["version"]!!.value
            arch = res.groups["arch"]!!.value
        }
    }

    fun isForCurrentArch(): Boolean {
        return WANTED_ARCH == arch
    }
}

private class AuthorizationInterceptor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .apply {
                    addHeader("Authorization", AUTH_HEADER)
            }
            .build()
        return chain.proceed(request)
    }
}

val apolloClient = ApolloClient.Builder()
    .serverUrl("https://api.github.com/graphql")
    .okHttpClient(
        OkHttpClient.Builder()
            .addInterceptor(AuthorizationInterceptor())
            .build()
    )
    .build()

object GitHubReleaseClient {
    suspend fun getReleases(): List<GitHubRelease>? {
        val resp = apolloClient.query(GetReleasesQuery()).execute()

        if (resp.exception != null) {
            resp.exception!!.printStackTrace()
            return null
        }

        return resp.data!!.repository!!.releases.nodes!!.map {
            GitHubRelease(
                it!!.tagName,
                it.releaseAssets.nodes!!.map {
                    GitHubReleaseAsset(it!!.name, it.url.toString())
                }.filter { it.version != "" }
            )
        }.filter { it.nixosVersion != "" && it.hasAnyForArch() }
    }
}