package ley.anvil.modpacktools.command

import ley.anvil.modpacktools.CONFIG
import ley.anvil.modpacktools.MPJH
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

/**
 * The command loader will scan the given package for {@link ICommand} classes and add them to the command list
 *
 * @param pkg The package to scan for commands
 */
class CommandLoader(private val pkg: String) {
    class ConfigMissingException : IllegalStateException()
    class ModpackJsonMissingException : IllegalStateException()

    val commands = HashMap<String, ICommand>()

    companion object {
        /**
         * Runs a command statically.
         *
         * @param args the args to pass to the command
         * @throws ConfigMissingException if the command requires a config and it is not found
         * @throws ModpackJsonMissingException if the command requires a modpackjson file and it is not found
         */
        @JvmStatic
        @Throws(ConfigMissingException::class, ModpackJsonMissingException::class)
        fun ICommand.runStatic(args: Array<out String>): CommandReturn {
            if(this.needsConfig && !CONFIG.exists)
                throw ConfigMissingException()

            if(this.needsModpackjson && MPJH.asWrapper == null)
                throw ModpackJsonMissingException()

            return this.execute(args)
        }
    }

    init {
        loadCommands()
    }

    @Suppress("UNCHECKED_CAST")
    private fun loadCommands() {
        //Get ICommands in package
        val refs = Reflections(pkg, SubTypesScanner(false))

        refs.getSubTypesOf(ICommand::class.java).stream()
            .map {it.kotlin}
            //Only annotated classes
            //Cannot use it.hasAnnotation because it is experimental and requires everything to be annotated so this makes more sense
            .filter {it.annotations.any {ann ->  ann.annotationClass == LoadCommand::class}}
            //can be object
            .map {it.objectInstance ?: it}
            //create new instance if it is a class, otherwise just add the current instance
            .forEach {if(it is ICommand) addCommand(it) else addClass(it as KClass<out ICommand>)}
    }

    /**
     * Creates a new instance of the given class and adds it to the command list
     *
     * @param clazz the class to add
     * @return if it was successful
     */
    fun addClass(clazz: KClass<out ICommand>) = addCommand(clazz.createInstance())

    /**
     * Adds a command to the command list with the name that getName() returns
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
     * Runs the given command
     *
     * @param name the name of the command to be run
     * @param args the arguments passed into the command
     * @return the return of the command
     * @throws NoSuchElementException if there's no command with the given name
     */
    @Throws(NoSuchElementException::class)
    fun runCommand(name: String, args: Array<out String>) = commands.computeIfAbsent(name) {throw NoSuchElementException("Command $name Not Found")}.runStatic(args)
}
