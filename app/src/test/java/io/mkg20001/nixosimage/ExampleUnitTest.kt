package io.mkg20001.nixosimage

import io.mkg20001.nixosimage.data.GitHubReleaseClient
import kotlinx.coroutines.test.runTest
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun fetch_releases() = runTest {
        assert(GitHubReleaseClient.getReleases()?.isNotEmpty() == true)
    }
}