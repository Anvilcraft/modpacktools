package ley.anvil.modpacktools.commands

import ley.anvil.modpacktools.Main.CONFIG
import ley.anvil.modpacktools.command.CommandReturn
import ley.anvil.modpacktools.command.CommandReturn.fail
import ley.anvil.modpacktools.command.CommandReturn.success
import ley.anvil.modpacktools.command.ICommand
import ley.anvil.modpacktools.command.LoadCommand

@LoadCommand
class Init : ICommand {
    override fun getName(): String = "init"
    override fun getHelpMessage(): String = "initializes the MPT dev environment (currently only creates config file)"
    override fun needsConfig(): Boolean = false

    override fun execute(args: Array<out String>?): CommandReturn {
        if(CONFIG.configExists())
            return fail("Config exists")
        CONFIG.copyConfig()
        return success("Config Created")
    }
}