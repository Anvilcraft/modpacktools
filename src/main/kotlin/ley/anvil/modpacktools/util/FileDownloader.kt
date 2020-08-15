package ley.anvil.modpacktools.util

import ley.anvil.modpacktools.HTTP_CLIENT
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.net.URL
import java.nio.file.Paths
import java.util.concurrent.CountDownLatch

/**
 * Downloads all supplied urls to the given files
 *
 * @param files the files to download
 * @param callback the callback which will be called once a download finishes
 */
fun downloadFiles(
    files: List<FileToDownload>,
    callback: (DownloadFileTask.Return) -> Unit
) {
    val latch = CountDownLatch(files.size)
    files.forEach {
        val req = DownloadFileTask(callback, latch, it)
        HTTP_CLIENT.newCall(req.request).enqueue(req)
    }
    latch.await()
}

/**
 * A file that should be downloaded
 *
 * @param fileOrDir the file to save to
 * @param url the url to download from
 * @param shouldResolveFileName if true, the fileOrDir will be treated as directory and the file name will be resolved from the URL
 */
data class FileToDownload(
    val fileOrDir: File,
    val url: URL,
    val shouldResolveFileName: Boolean = false,
    val shouldSkipIfExists: Boolean = false
)

open class DownloadFileTask(
    protected open val callback: (Return) -> Unit,
    protected open val latch: CountDownLatch,
    protected open val file: FileToDownload
) : Callback {
    open val request = Request.Builder()
        .get()
        .url(file.url)
        .build()

    override fun onFailure(call: Call, e: IOException) {
        callback(
            Return(
                file,
                null,
                null,
                null,
                e,
                true
            )
        )
        latch.countDown()
    }

    override fun onResponse(call: Call, response: Response) {
        callback(
            run {
                var wasSkipped = true
                val outFile =
                    if(file.shouldResolveFileName)
                        file.fileOrDir mergeTo Paths.get(response.request.url.toUri().path).fileName.toFile()
                    else
                        file.fileOrDir

                if(!file.shouldSkipIfExists || !outFile.exists()) {
                    response.body?.byteStream().use {
                        FileUtils.copyInputStreamToFile(it, outFile)
                    }
                    wasSkipped = false
                }

                Return(
                    file,
                    outFile,
                    response.code,
                    response.message,
                    null,
                    wasSkipped
                )
            }
        )
        latch.countDown()
    }

    data class Return(
        val file: FileToDownload,
        val downloadedFile: File?,
        val responseCode: Int?,
        val responseMessage: String?,
        val exception: Exception?,
        val wasSkipped: Boolean
    )
}
