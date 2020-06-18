package ley.anvil.modpacktools;

import ley.anvil.modpacktools.command.CommandLoader;
import ley.anvil.modpacktools.command.CommandReturn;
import ley.anvil.modpacktools.util.Config;

import java.util.NoSuchElementException;

public class Main {
    public static final Config CONFIG = new Config();
    public static final CommandLoader LOADER = new CommandLoader("ley.anvil.modpacktools.commands");

    public static void main(String[] args) {
    	if(args.length <= 0) {
		    printHelp();
		    return;
	    }

        try {
            CommandReturn ret = LOADER.runCommand(args[0], args);
            if(!ret.hadSuccess())
                System.out.println(ret.getRet());
        }catch(NoSuchElementException e) {
            System.out.println(e.getMessage());
            printHelp();
        }
    }

    private static void printHelp() {
	    System.out.println("Commands:");
	    LOADER.getCommands().forEach((k, v) -> System.out.println(k));
    }
}
