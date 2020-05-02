package ley.anvil.modpacktools.commands;

import com.google.gson.*;
import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.project.CurseProject;
import j2html.TagCreator;
import j2html.tags.ContainerTag;
import ley.anvil.modpacktools.Main;
import ley.anvil.modpacktools.util.Util;
import okhttp3.HttpUrl;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static j2html.TagCreator.*;

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
        //Check if the command has the correct number of args
        if(modlink.length >= 2) {
            //The url must match this
            String regex = "(?m)^(http)(s)?://(www\\.)?(curseforge.com/minecraft/mc-mods/)[0-z,\\-]+/(files)/[0-9]+$";
            String endPartRegex = "(/files/)[0-9]+$";
            if(modlink[1].matches(regex)) {
                try {
                    //remove fileID
                    System.out.println("Getting ID");
                    CurseProject project = CurseAPI.project(HttpUrl.get(modlink[1].replaceAll(endPartRegex, ""))).get();
                    int projectID = project.id();
                    //extract fileID
                    Pattern pattern = Pattern.compile("[0-9]+$");
                    Matcher matcher = pattern.matcher(modlink[1]);
                    int fileID = 0;
                    if(matcher.find()) {
                        fileID = Integer.parseInt(matcher.group(0));
                    }
                    File manifestFile = new File(Main.CONFIG.JAR_LOCATION, Main.CONFIG.CONFIG.get("manifestFile").getAsString());
                    System.out.println("Reading Manifest");
                    JsonObject manifest = Util.readJsonFile(manifestFile);
                    //Get Mods in manifest file
                    JsonArray files = manifest.getAsJsonArray("files");
                    //Check if Mod already exsits
                    for(JsonElement file : files) {
                        if(file.getAsJsonObject().get("projectID").getAsInt() == projectID) {
                            System.out.println("The mod is already installed!");
                            return;
                        }
                    }
                    System.out.println("Adding Mod " + project.name());
                    //Construct Mod
                    JsonObject mod = new JsonObject();
                    mod.addProperty("projectID", projectID);
                    mod.addProperty("fileID", fileID);
                    //Add Mod to array
                    files.add(mod);
                    //Remove old file array from manifest
                    manifest.remove("files");
                    //Add new file array to manifest
                    manifest.add("files", files);
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    //Overwrite Old Manifest File
                    FileWriter manifestWriter = new FileWriter(manifestFile, false);
                    System.out.println("Printing Manifest");
                    gson.toJson(manifest, manifestWriter);
                    manifestWriter.close();
                } catch(CurseException | IOException e) {
                    e.printStackTrace();
                }
            }else {
                System.out.println("Link Must match " + regex);
            }
        }else {
            System.out.println("Syntax: addmod <curseforge url>");
        }
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
     * @param format 1 Can be html or csv, 2 can be any valid file to write to
     */
    public static void createModlist(String[] format) {
        if(format.length >= 3) {
            if(format[1].equalsIgnoreCase("csv")) {
                File csvFile = new File(format[2]);
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
                File htmlFile = new File(format[2]);
                if(htmlFile.exists()) {
                    System.out.println("Delete " + htmlFile);
                    return;
                }
                ContainerTag table = body(
                        TagCreator.table(TagCreator.attrs("#mods"), TagCreator.tbody(
                                tr(td(b("Name")),
                                        td(b("Authors")),
                                        td(b("ID")),
                                        td(b("Downloads"))
                                ),
                                TagCreator.each(ModInfo.getModInfo(), i -> {
                                    StringBuilder sb = new StringBuilder();
                                    for(String author : i.getAuthors()) {
                                        sb.append(author);
                                        sb.append(", ");
                                    }
                                    String authors = sb.toString();
                                    authors = authors.substring(0, authors.length() - 2);

                                    return tr(td(a(i.getName()).withHref(i.getLink())),
                                            td(authors),
                                            td(String.valueOf(i.getId())),
                                            td(String.valueOf(i.getDownloads())));
                                })
                        ))
                );
                try {
                    System.out.println("Writing HTML");
                    FileWriter htmlWrite = new FileWriter(htmlFile);
                    htmlWrite.write(table.render());
                    htmlWrite.close();
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }else {
                System.out.println("Expected Either HTML or CSV as format");
            }
        }else {
            System.out.println("Syntax: createmodlist <csv/html> <file>");
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
