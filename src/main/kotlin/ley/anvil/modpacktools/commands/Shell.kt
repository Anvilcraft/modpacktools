package ley.anvil.modpacktools.commands

import ley.anvil.modpacktools.TERMC
import ley.anvil.modpacktools.command.AbstractCommand
import ley.anvil.modpacktools.command.CommandReturn
import ley.anvil.modpacktools.command.CommandReturn.Companion.success
import ley.anvil.modpacktools.command.LoadCommand
import ley.anvil.modpacktools.runCommand
import ley.anvil.modpacktools.util.fPrint
import net.sourceforge.argparse4j.inf.Namespace

@LoadCommand
object Shell : AbstractCommand("Shell") {
    override val helpMessage: String = "opens a shell where mpt commands can be entered in a loop."

    override val needsConfig: Boolean = false
    override val needsModpackjson: Boolean = false

    override fun execute(args: Namespace): CommandReturn {
        println("enter \'exit\' to exit the shell\n")

        while(true) {
            fPrint(">>>", TERMC.bold, TERMC.cyan)
            val arg = readLine()!!.split(' ')
            if(arg.getOrNull(0) == "exit")
                break
            runCommand(arg.toTypedArray())
        }
        return success()
    }
}
