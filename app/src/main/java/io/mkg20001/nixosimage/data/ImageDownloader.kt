package io.mkg20001.nixosimage.data

import android.content.Context
import io.sentry.Sentry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

suspend fun downloadFile(
    context: Context,
    fileUrl: String,
    fileName: String,
    onProgress: (percent: Int) -> Unit
): File? {
    return withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val request = Request.Builder().url(fileUrl).build()

            val response = client.newCall(request).execute()
            val body = response.body ?: return@withContext null

            val file = File(context.cacheDir, fileName)

            // re-use already existing file
            if (file.exists() && body.contentLength() == file.length()) {
                onProgress(100) // update ui
                return@withContext file
            }

            // TODO: currently we're not re-using half-downloaded files.
            // this could be implemented aswell, athough i'm not sure if gh release supports that.

            val progressStream = ProgressStream(body.source().inputStream(), body.contentLength().toDouble(), onProgress)
            val outputStream = FileOutputStream(file)

            progressStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            file
        } catch (e: Exception) {
            Sentry.captureException(e)
            e.printStackTrace()
            null
        }
    }
}