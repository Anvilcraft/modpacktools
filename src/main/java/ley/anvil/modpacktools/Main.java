package ley.anvil.modpacktools;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ley.anvil.modpacktools.command.CommandLoader;
import ley.anvil.modpacktools.command.CommandReturn;
import ley.anvil.modpacktools.util.Config;
import ley.anvil.modpacktools.util.ModpackJsonHandler;

import java.io.File;
import java.util.Map;
import java.util.NoSuchElementException;

public class Main {
    public static final Config CONFIG = new Config();
    public static final CommandLoader LOADER = new CommandLoader("ley.anvil.modpacktools.commands");
    public static ModpackJsonHandler MPJH;
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) {
        if(CONFIG.configExists())
            MPJH = new ModpackJsonHandler(new File((CONFIG.CONFIG.getPath(String.class, "Locations", "modpackjsonFile"))));

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
        LOADER.getCommands()
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> System.out.println(e.getKey() + ": " + e.getValue().getHelpMessage()));
    }
}
