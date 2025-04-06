package io.mkg20001.nixosimage.data

import android.util.Log
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.network.okHttpClient
import kotlinx.coroutines.delay
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Optional

data class GithHubRelease(
    val tagName: String
)

data class GitHubReleaseAsset(
    val name: String,
    var url: String
)


val apolloClient = ApolloClient.Builder()
    .serverUrl("https://api.github.com/graphql")
    .okHttpClient(
        OkHttpClient.Builder()
            .build()
    )
    .build()

object GitHubRelease {
    fun get() {
        // apolloClient.query(GetReleasesQuery)
    }
}