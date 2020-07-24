package ley.anvil.modpacktools.commands;

import ley.anvil.modpacktools.Main;
import ley.anvil.modpacktools.command.CommandReturn;
import ley.anvil.modpacktools.command.ICommand;
import ley.anvil.modpacktools.command.LoadCommand;

@LoadCommand
public class Init implements ICommand {
    @Override
    public CommandReturn execute(String[] args) {
        if(Main.CONFIG.configExists())
            return CommandReturn.fail("Config already exists");
        Main.CONFIG.copyConfig();
        System.out.println("Config Created");
        return CommandReturn.success();
    }

    @Override
    public String getName() {
        return "init";
    }

    @Override
    public boolean needsConfig() {
        return false;
    }

    @Override
    public String getHelpMessage() {
        return "initializes the MPT dev environment (currently only creates config file)";
    }
}
