package io.mkg20001.nixosimage.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import okio.ForwardingSource
import okio.Okio
import okio.Source
import okio.buffer
import java.io.File
import java.io.FileOutputStream

class ProgressResponseBody(
    private val responseBody: ResponseBody,
    private val progressCallback: (bytesRead: Long, contentLength: Long, done: Boolean) -> Unit
) : ResponseBody() {

    private var bufferedSource = source(responseBody.source()).buffer()

    override fun contentType() = responseBody.contentType()

    override fun contentLength() = responseBody.contentLength()

    override fun source(): BufferedSource = bufferedSource

    private fun source(source: Source): Source {
        return object : ForwardingSource(source) {
            var totalBytesRead = 0L

            override fun read(sink: Buffer, byteCount: Long): Long {
                val bytesRead = super.read(sink, byteCount)
                totalBytesRead += if (bytesRead != -1L) bytesRead else 0
                progressCallback(totalBytesRead, responseBody.contentLength(), bytesRead == -1L)
                return bytesRead
            }
        }
    }
}

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

            val progressBody = ProgressResponseBody(body) { bytesRead, contentLength, _ ->
                val percent = (100 * bytesRead / contentLength).toInt()
                onProgress(percent)
            }

            val inputStream = progressBody.byteStream()
            val outputStream = FileOutputStream(file)

            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}