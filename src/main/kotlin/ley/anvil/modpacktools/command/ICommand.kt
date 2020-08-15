package ley.anvil.modpacktools.command

import net.sourceforge.argparse4j.inf.ArgumentParser
import net.sourceforge.argparse4j.inf.Namespace

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
    fun execute(args: Namespace): CommandReturn

    val parser: ArgumentParser

    /**
     * this is the name of the command
     */
    val name: String

    /**
     * This message will be displayed in the help dialog
     */
    val helpMessage: String
        get() = ""

    /**
     * If this command needs the config file to be present. the command will not run if this returns true and there is no config file
     */
    val needsConfig: Boolean
        get() = true

    /**
     * If this returns true, the command will not run if the modpackjson file doesnt exist
     */
    val needsModpackjson: Boolean
        get() = true
}
