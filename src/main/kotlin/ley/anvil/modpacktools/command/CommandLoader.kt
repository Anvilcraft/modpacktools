package ley.anvil.modpacktools.command

import eu.infomas.annotation.AnnotationDetector
import ley.anvil.modpacktools.CONFIG
import ley.anvil.modpacktools.MPJH
import net.sourceforge.argparse4j.inf.ArgumentParserException
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.isSubclassOf

/**
 * The command loader will scan the given package for [ICommand] classes and add them to the command list
 *
 * @param pkg The package to scan for commands
 */
class CommandLoader(private val pkg: String) {
    class ConfigMissingException : IllegalStateException()
    class ModpackJsonMissingException : IllegalStateException()

    val commands = HashMap<String, ICommand>()

    companion object {
        /**
         * Parses arguments for a given [ICommand] and executes it.
         * Also checks if the [ICommand] has [ICommand.needsConfig] or [ICommand.needsModpackjson] set
         * and throws an exception throws an exception if invalid
         *
         * @param args the args to pass to the command
         * @throws ConfigMissingException if the command requires a config and it is not found
         * @throws ModpackJsonMissingException if the command requires a modpackjson file and it is not found
         * @throws ArgumentParserException if the arguments are not valid
         */
        @JvmStatic
        @Throws(ConfigMissingException::class, ModpackJsonMissingException::class, ArgumentParserException::class)
        fun ICommand.runStatic(args: Array<out String>): CommandReturn {
            if(this.needsConfig && !CONFIG.exists)
                throw ConfigMissingException()

            if(this.needsModpackjson && MPJH.asWrapper == null)
                throw ModpackJsonMissingException()

            return this.execute(this.parser.parseArgs(args.slice(1 until args.size).toTypedArray()))
        }
    }

    init {
        loadCommands()
    }

    @Suppress("UNCHECKED_CAST")
    private fun loadCommands() {
        val reporter = object : AnnotationDetector.TypeReporter {
            override fun annotations() = arrayOf(LoadCommand::class.java)

            override fun reportTypeAnnotation(annotation: Class<out Annotation>?, className: String?) {
                val clazz = Class.forName(className).kotlin
                if(clazz.isSubclassOf(ICommand::class)) {
                    clazz.objectInstance?.let {addCommand(it as ICommand)} ?: addClass(clazz as KClass<out ICommand>)
                }
            }
        }

        AnnotationDetector(reporter).detect(pkg)
    }

    /**
     * Creates a new instance of the given [KClass] and adds it to the command list
     *
     * @param clazz the [KClass] to add
     * @return if it was successful
     */
    fun addClass(clazz: KClass<out ICommand>) = addCommand(clazz.createInstance())

    /**
     * Adds a command to the command list with the name that [ICommand.name] returns
     *
     * @param command the command to add
     * @return if it was successful
     */
    fun addCommand(command: ICommand): Boolean {
        if(commands.containsKey(command.name))
            return false
        commands[command.name] = command
        return true
    }

    /**
     * Runs an [ICommand] that has been loaded by this [CommandLoader]
     * given a name.
     *
     * @param name the name of the command to be run
     * @param args the arguments passed into the command
     * @return the return of the command
     * @throws NoSuchElementException if there's no command with the given name
     * @throws ArgumentParserException if the arguments are not valid
     */
    @Throws(
        NoSuchElementException::class,
        ConfigMissingException::class,
        ModpackJsonMissingException::class,
        ArgumentParserException::class
    )
    fun runCommand(name: String, args: Array<out String>) =
        commands.computeIfAbsent(name) {throw NoSuchElementException("Command $name Not Found")}.runStatic(args)
}
