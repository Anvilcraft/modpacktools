package ley.anvil.modpacktools.command

import ley.anvil.modpacktools.CONFIG
import ley.anvil.modpacktools.MPJH
import ley.anvil.modpacktools.command.CommandReturn.Companion.fail
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner

/**
 * The command loader will scan the given package for {@link ICommand} classes and add them to the command list
 *
 * @param pkg The package to scan for commands
 */
class CommandLoader(private val pkg: String) {
    val commands = HashMap<String, ICommand>()

    companion object {
        @JvmStatic
        fun ICommand.runStatic(args: Array<out String>): CommandReturn {
            if(this.needsConfig && !CONFIG.configExists())
                return fail("Config is needed for this command yet it is not present. Run 'init' to generate")
            if(this.needsModpackjson && MPJH.asWrapper == null)
                return fail("Modpackjson is needed for this command yet it is not present.")
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
            //Only annotated classes
            .filter {it.isAnnotationPresent(LoadCommand::class.java)}
            //can be object
            .map {it.kotlin.objectInstance ?: it}
            .forEach {if(it is ICommand) addCommand(it) else addClass(it as Class<out ICommand>)}
    }

    /**
     * Creates a new instance of the given class and adds it to the command list
     *
     * @param clazz the class to add
     * @return if it was successful
     */
    fun addClass(clazz: Class<out ICommand>) = addCommand(clazz.newInstance())

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
