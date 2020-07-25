package ley.anvil.modpacktools.util

import ley.anvil.modpacktools.Main.HTTP_CLIENT
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

open class FileDownloader(
    private val files: Map<URL, File>,
    private val callback: (DownloadFileTask.Return) -> Unit,
    private val existingFileBehaviour: ExistingFileBehaviour
) {
    private var latch: CountDownLatch? = null

    init {
        dispatchTasks()
    }

    private fun dispatchTasks() {
        val tasks = files.entries.stream()
            .filter {existingFileBehaviour == ExistingFileBehaviour.OVERWRITE || !it.value.exists()}
            .collect(Collectors.toList())
        latch = CountDownLatch(tasks.size)
        tasks.forEach {
            val req = DownloadFileTask(it.key, it.value, callback, latch!!)
            HTTP_CLIENT.newCall(req.request).enqueue(req)
        }
        latch!!.await()
    }

    class DownloadFileTask(private val url: URL,
                           private val file: File,
                           private val callback: (Return) -> Unit,
                           private val latch: CountDownLatch) : Callback {
        val request = Request.Builder()
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
            } finally {
                latch.countDown()
            })
        }

        data class Return(
            val url: URL,
            val file: File,
            val responseCode: Int?,
            val responseMessage: String?,
            val exception: Exception?
        ) {}
    }

    enum class ExistingFileBehaviour {
        OVERWRITE,
        SKIP
    }
}