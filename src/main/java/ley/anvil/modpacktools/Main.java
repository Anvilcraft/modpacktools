package ley.anvil.modpacktools;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ley.anvil.modpacktools.command.CommandLoader;
import ley.anvil.modpacktools.command.CommandReturn;
import ley.anvil.modpacktools.util.Config;
import ley.anvil.modpacktools.util.ModpackJsonHandler;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;

import java.io.File;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    public static final Config CONFIG = new Config();
    public static final CommandLoader LOADER = new CommandLoader("ley.anvil.modpacktools.commands");
    public static ModpackJsonHandler MPJH;
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final long httpTimeout = Main.CONFIG.CONFIG.getPath(Long.class, "Downloads/httpTimeout");
    public static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .callTimeout(httpTimeout, TimeUnit.MICROSECONDS)
            .connectTimeout(httpTimeout, TimeUnit.MICROSECONDS)
            .readTimeout(httpTimeout, TimeUnit.MICROSECONDS)
            .writeTimeout(httpTimeout, TimeUnit.MICROSECONDS)
            .dispatcher(new Dispatcher(Executors.newFixedThreadPool(Main.CONFIG.CONFIG.getPath(Long.class, "Downloads/maxThreads").intValue())))
            .build();

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

        HTTP_CLIENT.dispatcher().executorService().shutdown();
        HTTP_CLIENT.connectionPool().evictAll();
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
