package ley.anvil.modpacktools.command

import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.inf.ArgumentParser

/**
 * an implementation of [ICommand] meant to reduce boilerplate.
 * this automatically creates a base [ArgumentParser]
 * with the [helpMessage] as description and then applies [addArgs] to it
 * and uses [displayName] as the name for the command in the help message.
 *
 * the [name] of the command will be a converted version of the [displayName] by default
 *
 * @param displayName the name of this command to be displayed in the help message
 * @param name the internal name of the command. will be the [displayName] lower case and with _ instead of spaces by default
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
     * This will be called to add arguments to the [ArgumentParser] of this command.
     * override this to add arguments.
     *
     * @receiver the [ArgumentParser] to add the args to
     */
    protected open fun ArgumentParser.addArgs() {}
}
