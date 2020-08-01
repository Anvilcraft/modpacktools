package ley.anvil.modpacktools.commands

import ley.anvil.modpacktools.command.CommandReturn
import ley.anvil.modpacktools.command.CommandReturn.Companion.success
import ley.anvil.modpacktools.command.ICommand
import ley.anvil.modpacktools.command.LoadCommand
import ley.anvil.modpacktools.runCommand
import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.inf.ArgumentParser
import net.sourceforge.argparse4j.inf.Namespace

@LoadCommand
object Shell : ICommand {
    override val name: String = "shell"
    override val helpMessage: String = "opens a shell where mpt commands can be entered in a loop."
    override val parser: ArgumentParser = ArgumentParsers.newFor("Shell")
        .build()
        .description(helpMessage)

    override val needsConfig: Boolean = false
    override val needsModpackjson: Boolean = false

    override fun execute(args: Namespace): CommandReturn {
        println("enter \'exit\' to exit the shell\n")

        while(true) {
            print(">>>")
            val arg = readLine()!!.split(' ')
            if(arg.getOrNull(0) == "exit")
                break
            runCommand(arg.toTypedArray())
        }
        return success()
    }
}