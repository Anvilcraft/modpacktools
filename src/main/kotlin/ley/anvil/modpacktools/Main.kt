@file:JvmName("Main")

package ley.anvil.modpacktools

import com.github.ajalt.mordant.TermColors
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import ley.anvil.modpacktools.command.CommandLoader
import ley.anvil.modpacktools.command.ICommand
import ley.anvil.modpacktools.util.ModpackJsonHandler
import ley.anvil.modpacktools.util.config.Config
import ley.anvil.modpacktools.util.config.MissingConfigValueException
import ley.anvil.modpacktools.util.fPrintln
import net.sourceforge.argparse4j.inf.ArgumentParserException
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import java.io.File
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.MICROSECONDS

//Lazy initialization will prevent objects from being initilized if not needed
val CONFIG by lazy {Config("modpacktoolsconfig.toml")}
val LOADER by lazy {CommandLoader("ley.anvil.modpacktools.commands")}
val MPJH by lazy {ModpackJsonHandler(File(CONFIG.config.pathOrException<String>("Locations/src"), "modpack.json"))}
val GSON: Gson by lazy {GsonBuilder().setPrettyPrinting().create()}
//TODO thinks term has no color support on win with edited registry. probably no big deal
val TERMC by lazy {TermColors()}

//for checking if the client has been initialized when closing it
private val httpClient0 = lazy {
    val timeout = CONFIG.config.pathOrException<Long>("Downloads/httpTimeout")
    OkHttpClient.Builder()
        .callTimeout(timeout, MICROSECONDS)
        .connectTimeout(timeout, MICROSECONDS)
        .readTimeout(timeout, MICROSECONDS)
        .writeTimeout(timeout, MICROSECONDS)
        .dispatcher(Dispatcher(Executors.newFixedThreadPool(CONFIG.config.pathOrException<Long>("Downloads/maxThreads").toInt())))
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
    runCommand(args)

    //Only close if initialized to prevent creation directly before closing
    if(httpClient0.isInitialized()) {
        HTTP_CLIENT.dispatcher.executorService.shutdown()
        HTTP_CLIENT.connectionPool.evictAll()
    }
}

fun runCommand(args: Array<out String>) {
    val errorColor = arrayOf(TERMC.red, TERMC.bold)
    val successColor = TERMC.green

    if(args.isEmpty()) {
        println(helpMessage)
    } else {
        try {
            val ret = LOADER.runCommand(args[0].toLowerCase(), args)
            if(ret.hasRet())
                fPrintln(
                    ret.ret,
                    *if(ret.success)
                        arrayOf(successColor)
                    else
                        errorColor
                )
        } catch(e: NoSuchElementException) {
            fPrintln("Command ${args[0]} not found", *errorColor)
            println(helpMessage)
        } catch(e: CommandLoader.ConfigMissingException) {
            fPrintln("Config is needed for this command yet it is not present. Run 'init' to generate", *errorColor)
        } catch(e: CommandLoader.ModpackJsonMissingException) {
            fPrintln("Modpackjson is needed for this command yet it is not present.", *errorColor)
        } catch(e: MissingConfigValueException) {
            fPrintln("The Config value ${e.missingValue} was expected but was not found", *errorColor)
            e.message?.let {println(it)}
            println('\n')
            e.printStackTrace()
        } catch(e: ArgumentParserException) {
            fPrintln("Invalid args: ${e.message}", *errorColor)
        }
    }
}
