@file:JvmName("Main")
package ley.anvil.modpacktools

import com.google.gson.GsonBuilder
import ley.anvil.modpacktools.command.CommandLoader
import ley.anvil.modpacktools.command.ICommand
import ley.anvil.modpacktools.util.Config
import ley.anvil.modpacktools.util.ModpackJsonHandler
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import java.io.File
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.MICROSECONDS

val CONFIG by lazy {Config("modpacktoolsconfig.toml")}
val LOADER by lazy {CommandLoader("ley.anvil.modpacktools.commands")}
val MPJH by lazy {ModpackJsonHandler(File(CONFIG.config.getPath<String>("Locations/modpackjsonFile")!!))}
val GSON by lazy {GsonBuilder().setPrettyPrinting().create()}
private val httpClient0 = lazy {
    val timeout = CONFIG.config.getPath<Long>("Downloads/httpTimeout")!!
    OkHttpClient.Builder()
        .callTimeout(timeout, MICROSECONDS)
        .connectTimeout(timeout, MICROSECONDS)
        .readTimeout(timeout, MICROSECONDS)
        .writeTimeout(timeout, MICROSECONDS)
        .dispatcher(Dispatcher(Executors.newFixedThreadPool(CONFIG.config.getPath<Long>("Downloads/maxThreads")!!.toInt())))
        .build()
}
val HTTP_CLIENT by httpClient0


fun main(args: Array<out String>) {
    if(args.isEmpty()) {
        printHelp()
    } else {

        try {
            val ret = LOADER.runCommand(args[0], args)
            if(ret.hasRet())
                println(ret.ret)
        } catch(e: NoSuchElementException) {
            println(e.message)
            printHelp()
        }
    }
    if(httpClient0.isInitialized()) {
        HTTP_CLIENT.dispatcher.executorService.shutdown()
        HTTP_CLIENT.connectionPool.evictAll()
    }
}

fun printHelp() {
    println("Commands:")
    LOADER.commands.entries.stream()
        .sorted(Comparator.comparing {e: MutableMap.MutableEntry<String, ICommand> -> e.key})
        .forEach {println("${it.key}: ${it.value.helpMessage}")}
}