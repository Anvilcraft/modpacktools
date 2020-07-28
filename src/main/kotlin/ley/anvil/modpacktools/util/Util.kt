@file:JvmName("Util")

package ley.anvil.modpacktools.util

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import ley.anvil.modpacktools.HTTP_CLIENT
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.*
import java.net.URI
import java.net.URL
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.isAccessible

/**
 * Reads a Json File
 *
 * @receiver the file to read from
 * @return the file content as JsonObject
 */
fun File.readAsJson(): JsonObject {
    val reader = FileReader(this)
    val out = JsonParser.parseReader(reader) as JsonObject
    reader.close()
    return out
}

/**
 * sends a http post request
 *
 * @receiver the url to send the request to
 * @param contentType what content type should be used. Example: {@code MediaType.parse("application/json; utf-8")}
 * @param payload the payload to send
 * @param additionalHeaders additional headers that should be added to the request
 * @return the response as string
 */
@Throws(IOException::class)
fun URL.httpPostStr(payload: String, contentType: MediaType? = null, additionalHeaders: Map<String, String>): String? {
    val builder = Request.Builder()
        .url(this)
        .post(payload.toRequestBody(contentType))

    additionalHeaders.forEach {builder.addHeader(it.key, it.value)}
    val resp = HTTP_CLIENT.newCall(builder.build()).execute()
    val ret = resp.body?.string()
    resp.close()
    return ret
}

/**
 * sends a http post request
 *
 * @receiver the url to send the request to
 * @param contentType what content type should be used. Example: `"application/json; utf-8"`
 * @param payload the payload to send
 * @param additionalHeaders additional headers that should be added to the request
 * @return the response as string
 */
@Throws(IOException::class)
fun URL.httpPostStr(payload: String, contentType: String, additionalHeaders: Map<String, String>): String? {
    return this.httpPostStr(
        payload,
        contentType.toMediaType(),
        additionalHeaders
    )
}

/**
 * Sanitizes a URL to be valid by encoding illegal chars like spaces
 *
 * @receiver the URL to sanitize
 * @return the sanitized URL
 */

fun URL.sanitize(): URL? {
    return try {
        URI(this.protocol,
            this.userInfo,
            this.host,
            this.port,
            this.path,
            this.query,
            this.ref).toURL()
    } catch(e: Exception) {
        null
    }
}

/**
 * gets a function from the receiver and makes it accessible
 *
 * @param name the name of the function to get
 * @receiver the class to get the function from
 */
fun KClass<*>.getFun(name: String): KFunction<*>? = this.functions.find {it.name == name}?.apply {isAccessible = true}

fun zipDir(dir: File, parent: String?, zip: ZipOutputStream) {
    for (file in dir.listFiles()){
        if (file.isDirectory) {
            zipDir(file, parent + file.name + "/", zip)
            continue
        }
        zip.putNextEntry(ZipEntry(parent + file.name))
        var inp = BufferedInputStream(FileInputStream(file))
        var bytesRead: Long = 0
        val bytesIn = ByteArray(4096)
        var read = 0
        while (inp.read(bytesIn).also { read = it } != -1) {
            zip.write(bytesIn, 0, read)
            bytesRead += read.toLong()
        }
        zip.closeEntry()
    }
}