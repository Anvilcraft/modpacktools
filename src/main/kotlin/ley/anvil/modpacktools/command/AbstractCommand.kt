package ley.anvil.modpacktools.command

import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.inf.ArgumentParser

/**
 * Implement this for commands. it is meant to reduce boilerplate.
 *
 * @param displayName the name of this command to be displayed in the help message
 * @param name the internal name of the command. will be the display name in lower case and with _ instead of spaces by default
 */
abstract class AbstractCommand

@JvmOverloads
constructor(
    val displayName: String,
    override val name: String = displayName.toLowerCase().replace(' ', '_')
) : ICommand {
    override val parser: ArgumentParser by lazy {
        ArgumentParsers.newFor(displayName)
            .build()
            .description(helpMessage)
            .apply {addArgs()}
    }

    /**
     * This will be called to add arguments to the arg parser of this command.
     * override this to add arguments.
     *
     * @receiver the parser to add the args to
     */
    protected open fun ArgumentParser.addArgs() {}
}
