package io.mkg20001.nixosimage.data

import android.content.Context
import io.sentry.Sentry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.internal.http2.StreamResetException
import java.io.File
import java.io.FileOutputStream

suspend fun downloadFile(
    context: Context,
    fileUrl: String,
    fileName: String,
    onProgress: (percent: Int) -> Unit,
): File? {
    return withContext(Dispatchers.IO) {
        try {
            val file = File(context.cacheDir, fileName)

            while (true) {
                var retry = 0

                try {
                    val alreadyDownloadedBytes = if (file.exists()) file.length() else 0L

                    val client = OkHttpClient()
                    val request = Request.Builder().url(fileUrl).apply {
                        if (alreadyDownloadedBytes > 0) {
                            addHeader("Range", "bytes=$alreadyDownloadedBytes-")
                        }
                    }.build()

                    val response = client.newCall(request).execute()
                    val body = response.body ?: return@withContext null

                    // re-use already existing file
                    if (file.exists() && body.contentLength() == file.length()) {
                        onProgress(100) // update ui
                        return@withContext file
                    }

                    val progressStream = ProgressStream(
                        body.source().inputStream(),
                        body.contentLength().toDouble(),
                        alreadyDownloadedBytes,
                        onProgress,
                    )
                    val outputStream = FileOutputStream(file, true)

                    progressStream.use { input ->
                        outputStream.use { output ->
                            input.copyTo(output)
                        }
                    }

                    // TODO: maybe we should check the hash here if we have one (github release api might get it)

                    break
                } catch(e: StreamResetException) {
                    if (retry == 3) {
                        throw e
                    } else {
                        retry++
                    }
                } catch (e: java.net.SocketException) {
                    if (e.message?.contains("Software caused connection abort") == true && retry != 3) {
                        retry++
                    } else {
                        throw e
                    }
                }
            }

            file
        } catch (e: Exception) {
            e.printStackTrace()
            Sentry.captureException(e)
            null
        }
    }
}