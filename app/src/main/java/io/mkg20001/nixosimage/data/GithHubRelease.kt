package io.mkg20001.nixosimage.data

import com.apollographql.apollo.ApolloClient
import io.mkg20001.nixosimage.GetReleasesQuery
import java.io.Serializable

private val regexTag = Regex("^nixos-(?<version>[a-z0-9.]+)$")
private val regexImage = Regex("^image-(?<version>[a-z0-9.]+)-(?<arch>[a-z0-9_-]+).tar.gz$")

private val currentArch = System.getProperty("os.arch")
val WANTED_ARCH = when(currentArch) {
    "arm64" -> "aarch64"
    else -> currentArch
}

data class GitHubRelease(
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

    fun getSupported(): List<GitHubReleaseAsset> {
        return assets.filter { it.isSupported() }
    }
}

data class GitHubReleaseAsset(
    val id: String,
    val name: String,
    var url: String,
    var updatedAt: Any
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

    fun isSupported(): Boolean {
        return WANTED_ARCH == arch
    }
}

/* private class AuthorizationInterceptor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .apply {
                    addHeader("Authorization", AUTH_HEADER)
            }
            .build()
        return chain.proceed(request)
    }
} */

val apolloClient = ApolloClient.Builder()
    .serverUrl("https://nixos-image.mkg20001.io/graphql")
    // If you want to develop on the server, run "cargo run" and fill in your IP here
    // .serverUrl("http://192.168.178.69:8000/graphql")
    // Access github directly with auth
    /* .okHttpClient(
        OkHttpClient.Builder()
            .addInterceptor(AuthorizationInterceptor())
            .build()
    ) */
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
                    GitHubReleaseAsset(it!!.id, it.name, it.url.toString(), it.updatedAt)
                }.filter { it.version != "" }
            )
        }.filter { it.nixosVersion != "" }
    }
}