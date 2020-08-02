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
import java.util.concurrent.CountDownLatch
import java.util.stream.Collectors


private var latch: CountDownLatch? = null

fun downloadFiles(
    files: Map<URL, File>,
    callback: (DownloadFileTask.Return) -> Unit,
    skipExistingFiles: Boolean
) {
    val tasks = files.entries.stream()
        //remove if it should be skipped
        .filter {!skipExistingFiles || !it.value.exists()}
        .collect(Collectors.toList())
    latch = CountDownLatch(tasks.size)
    tasks.forEach {
        val req = DownloadFileTask(it.key, it.value, callback, latch!!)
        HTTP_CLIENT.newCall(req.request).enqueue(req)
    }
    latch!!.await()
}

open class DownloadFileTask(
    protected open val url: URL,
    protected open val file: File,
    protected open val callback: (Return) -> Unit,
    protected open val latch: CountDownLatch
) : Callback {
    open val request = Request.Builder()
        .get()
        .url(url)
        .build()

    override fun onFailure(call: Call, e: IOException) {
        callback(Return(
            url,
            file,
            null,
            null,
            e
        ))
        latch.countDown()
    }

    override fun onResponse(call: Call, response: Response) {
        callback(try {
            val stream = response.body?.byteStream()
            FileUtils.copyInputStreamToFile(stream, file)
            stream!!.close()

            Return(
                url,
                file,
                response.code,
                response.message,
                null
            )
        } catch(e: NullPointerException) {
            Return(
                url,
                file,
                response.code,
                response.message,
                e
            )
        })
        latch.countDown()
    }

    data class Return(
        val url: URL,
        val file: File,
        val responseCode: Int?,
        val responseMessage: String?,
        val exception: Exception?
    )
}
