package ley.anvil.modpacktools.command

/**
 * This must be implemented by all commands
 */
interface ICommand {
    /**
     * Executes this Command
     *
     * @param args Arguments for the Command
     * @return If the Command was executed successful
     */
    fun execute(args: Array<out String>): CommandReturn

    /**
     * this is the name of the command
     */
    val name: String

    /**
     * This message will be displayed in the help dialog
     */
    @JvmDefault
    val helpMessage: String
        get() = ""

    /**
     * If this command needs the config file to be present. the command will not run if this returns true and there is no config file
     */
    @JvmDefault
    val needsConfig: Boolean
        get() = true

    /**
     * If this returns true, the command will not run if the modpackjson file doesnt exist
     */
    @JvmDefault
    val needsModpackjson: Boolean
        get() = true
}