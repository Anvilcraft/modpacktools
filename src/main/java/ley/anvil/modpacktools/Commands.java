package ley.anvil.modpacktools;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import ley.anvil.modpacktools.util.ModInfo;
import ley.anvil.modpacktools.util.Util;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Commands {

    /**
     * Prints out all available commands
     */
    public static void help() {
        System.out.println("Help Goes here!");
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
    public static void addMod(String[] modlink) {

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
    public static void buildServer(String[] dir) {

    }

    /**
     * Downloads all mods in this pack
     * @param dir The mods directory
     */
    public static void downloadMods(String[] dir) {

    }

    /**
     * Creates a modlist of this pack
     * @param format Can be html or csv
     */
    public static void createModlist(String[] format) {
        if(format[1].equalsIgnoreCase("csv")) {
            File csvFile = new File(Main.CONFIG.CONFIG.get("csvExportFile").getAsString());
            if(csvFile.exists()) {
                System.out.println("Delete " + csvFile);
                return;
            }
            System.out.println("Printing CSV into " + csvFile);
            Appendable out;
            CSVFormat format1;
            try(CSVPrinter printer = new CSVPrinter(new FileWriter(csvFile), CSVFormat.EXCEL.withDelimiter(';'))) {
                printer.printRecord("Name", "Authors", "Link", "Downloads", "ID");
                printer.println();
                ArrayList<ModInfo> modlist = ModInfo.getModInfo();
                Collections.sort(modlist, Comparator.comparing(ModInfo :: getName));
                for(ModInfo mod : modlist) {
                    String name = mod.getName();
                    String[] authorArr = mod.getAuthors();
                    String link = mod.getLink();
                    int downloads = mod.getDownloads();
                    int id = mod.getId();
                    StringBuilder sb = new StringBuilder();
                    for(String author : authorArr) {
                        sb.append(author);
                        sb.append(", ");
                    }
                    String authors = sb.toString();
                    authors = authors.substring(0, authors.length() - 2);

                    printer.printRecord(name, authors, link, downloads, id);
                }
            }catch(IOException e) {
                e.printStackTrace();
            }
        } else if(format[1].equalsIgnoreCase("html")) {
            //TODO implement html mod list
        }else {
            System.out.println("Expected Either HTML or CSV as format");
        }
    }


    //Commands for users (available outside a modpack dev environment)

    /**
     * Creates a server from a modpack zip file
     * @param args The path to the zip file, The directory where to create the server
     */
    public static void makeServer(String[] args) {

    }



}
