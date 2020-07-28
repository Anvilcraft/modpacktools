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

//Lazy initialization will prevent objects from being initilized if not needed
val CONFIG by lazy {Config("modpacktoolsconfig.toml")}
val LOADER by lazy {CommandLoader("ley.anvil.modpacktools.commands")}
val MPJH by lazy {ModpackJsonHandler(File(CONFIG.config.getPath<String>("Locations/src")!!, "modpack.json"))}
val GSON by lazy {GsonBuilder().setPrettyPrinting().create()}

//for checking if the client has been initialized when closing it
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
private val helpMessage by lazy {
    val sb = StringBuilder().append("Commands:\n\n")
    LOADER.commands.entries.stream()
        //Sort by name
        .sorted(Comparator.comparing {e: MutableMap.MutableEntry<String, ICommand> -> e.key})
        .forEach {sb.append("${it.key}: ${it.value.helpMessage}\n")}
    sb.toString()
}


fun main(args: Array<out String>) {
    if(args.isEmpty()) {
        println(helpMessage)
    } else {
        try {
            val ret = LOADER.runCommand(args[0], args)
            if(ret.hasRet())
                println(ret.ret)
        } catch(e: NoSuchElementException) {
            println("Command ${args[0]} not found")
            println(helpMessage)
        } catch(e: CommandLoader.ConfigMissingException) {
            println("Config is needed for this command yet it is not present. Run 'init' to generate")
        } catch(e: CommandLoader.ModpackJsonMissingException) {
            println("Modpackjson is needed for this command yet it is not present.")
        }
    }

    //Only close if initialized to prevent creation directly before closing
    if(httpClient0.isInitialized()) {
        HTTP_CLIENT.dispatcher.executorService.shutdown()
        HTTP_CLIENT.connectionPool.evictAll()
    }
}
