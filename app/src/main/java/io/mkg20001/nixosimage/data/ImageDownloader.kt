package io.mkg20001.nixosimage.data

import android.content.Context
import android.util.Log
import io.sentry.Sentry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.internal.http2.StreamResetException
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.security.MessageDigest

suspend fun downloadFile(
    context: Context,
    fileUrl: String,
    fileName: String,
    digest: String,
    onProgress: (percent: Int) -> Unit,
): File? {
    return withContext(Dispatchers.IO) {
        try {
            val file = File(context.cacheDir, fileName)
            var retry = 0

            val digestSplit = digest.split(":", limit = 2)
            if (digestSplit.size != 2) {
                throw IllegalArgumentException("Invalid digest format: $digest")
            }

            val digestAlgo = when (digestSplit[0].lowercase()) {
                "sha256" -> "SHA-256"
                "sha512" -> "SHA-512"
                else -> throw IllegalArgumentException("Unsupported digest algorithm: ${digestSplit[0]}")
            }

            val expectedHex = digestSplit[1].lowercase()

            Log.d("DL", "Expected digest=${digest}, algo=${digestAlgo}, expectedHex=${expectedHex}")

            while (true) {
                Log.d("DL", "Trying download, try $retry/3")

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

                    val digest = MessageDigest.getInstance(digestAlgo)
                    val outputStream = FileOutputStream(file, true)
                    var digestStream: DigestStream

                    if (alreadyDownloadedBytes < 1) {
                        Log.d("DL", "Full download, hash during download")

                        digestStream = DigestStream(progressStream, digest)

                        digestStream.use { input ->
                            outputStream.use { output ->
                                input.copyTo(output)
                            }
                        }
                    } else {
                        Log.d("DL", "Partial, rehash fully")
                        // data is only partial in this case, hash at the end
                        progressStream.use { input ->
                            outputStream.use { output ->
                                input.copyTo(output)
                            }
                        }

                        digestStream = DigestStream(file.inputStream(), digest)
                        digestStream.copyTo(OutputStream.nullOutputStream())
                    }

                    if (!digestStream.validate(expectedHex)) {
                        // TODO: toast
                        Log.w("DL", "Hashsum missmatch - wanted ${expectedHex}")
                        file.delete()
                        retry++
                        continue
                    }

                    break
                } catch(e: StreamResetException) {
                    if (retry != 3) {
                        retry++
                    } else {
                        throw e
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