package ley.anvil.modpacktools;

public class Commands {

    /**
     * Prints out all available commands
     */
    public static void help() {

    }

    /**
     * Creates a modpack dev environment in the current folder
     */
    public static void init() {

    }

    //Commands for modpack devs (only available in a modpack dev environment)

    /**
     * Adds a mod to the modpack
     * @param modlink Can be a link to a curseforge file or to a file download
     */
    public static void addMod(String modlink) {

    }

    /**
     * Builds the modpack as a Twitch modpack zip
     */
    public static void buildTwitch() {

    }

    /**
     * Builds the modpack as a modpack.json
     */
    public static void buildModpackJSON() {

    }

    /**
     * Builds the modpack as a raw zip file (for example for the Technic Launcher)
     */
    public static void buildRaw() {

    }

    /**
     * Builds the modpack as a server
     * @param dir The directory where to create the server
     */
    public static void buildServer(String dir) {

    }

    /**
     * Downloads all mods in this pack
     * @param dir The mods directory
     */
    public static void downloadMods(String dir) {

    }

    /**
     * Creates a modlist of this pack
     * @param format Can be html or csv
     */
    public static void createModlist(String format) {

    }

    //Commands for users (available outside a modpack dev environment)

    /**
     * Creates a server from a modpack zip file
     * @param modpackZip The path to the zip file
     * @param dir The directory where to create the server
     */
    public static void makeServer(String modpackZip, String dir) {

    }



}
