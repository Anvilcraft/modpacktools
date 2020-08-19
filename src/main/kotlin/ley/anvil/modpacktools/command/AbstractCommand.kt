package ley.anvil.modpacktools.command

import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.inf.ArgumentParser

/**
 * an implementation of [ICommand] meant to reduce boilerplate.
 * this automatically creates a base [ArgumentParser]
 * with the [helpMessage] as description which can be modified by using [argParser]
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
    protected fun argParser(block: ArgumentParser.() -> Unit) = lazy {
        ArgumentParsers.newFor(this.displayName)
            .build()
            .description(this.helpMessage)
            .apply(block)
    }

    override val parser: ArgumentParser by argParser {}
}
