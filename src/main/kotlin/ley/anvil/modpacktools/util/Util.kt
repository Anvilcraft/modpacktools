@file:JvmName("Util")

package ley.anvil.modpacktools.util

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import ley.anvil.modpacktools.HTTP_CLIENT
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.net.URI
import java.net.URL
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
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
    require(this.exists()) {"File to read doesn't exist"}

    return JsonParser.parseReader(reader()) as JsonObject
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
fun URL.httpPostStr(payload: String, contentType: MediaType? = null, additionalHeaders: Map<String, String>? = null): Response =
    HTTP_CLIENT.newCall(
        Request.Builder()
            .url(this)
            .post(payload.toRequestBody(contentType))
            .apply {additionalHeaders?.forEach {addHeader(it.key, it.value)}}
            .build()
    ).execute()

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
fun URL.httpPostStr(payload: String, contentType: String, additionalHeaders: Map<String, String>? = null): Response {
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

fun URL.sanitize(): URL? =
    try {
        URI(
            this.protocol,
            this.userInfo,
            this.host,
            this.port,
            this.path,
            this.query,
            this.ref
        ).toURL()
    } catch(e: Exception) {
        null
    }

/**
 * gets a function from the receiver and makes it accessible
 *
 * @param name the name of the function to get
 * @receiver the class to get the function from
 */
fun KClass<*>.getFun(name: String): KFunction<*>? = this.functions.find {it.name == name}?.apply {isAccessible = true}

/**
 * merges 2 file's basically moving [other] into a directory represented by the receiver
 *
 * @receiver the parent directory
 * @param other the file to put into [other]
 * @return the combined file
 */
infix fun File.mergeTo(other: File): File = File(this.path, other.name)

/**
 * zips a directory.
 *
 * @receiver the directory to zip
 * @param zStream the zip stream to write to
 */
fun Path.toZip(zStream: ZipOutputStream) {
    require(this.toFile().exists()) {"File must exist"}

    Files.walkFileTree(
        this,
        object : SimpleFileVisitor<Path>() {
            override fun visitFile(file: Path, attrs: BasicFileAttributes?): FileVisitResult {
                zStream.putNextEntry(ZipEntry(this@toZip.relativize(file).toString()))
                Files.copy(file, zStream)
                zStream.closeEntry()
                return FileVisitResult.CONTINUE
            }
        }
    )
}

/**
 * zips a directory.
 *
 * @receiver the directory to zip
 * @param zStream the zip stream to write to
 */
fun File.toZip(zStream: ZipOutputStream) = this.toPath().toZip(zStream)

/**
 * Unzips a zip file to a given directory
 *
 * @receiver the zip file to unzip
 * @param outputDir the dir to unzip to
 */
fun File.unzip(outputDir: File) {
    require(this.exists()) {"File must exist"}

    val stream = ZipInputStream(FileInputStream(this))
    while(true) {
        val entry = stream.nextEntry ?: break
        val outfile = File(outputDir, entry.name)
        FileUtils.copyToFile(stream, outfile)
    }
    stream.close()
}
