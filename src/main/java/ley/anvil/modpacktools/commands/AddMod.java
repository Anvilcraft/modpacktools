package ley.anvil.modpacktools.commands;

import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.project.CurseProject;
import ley.anvil.addonscript.curse.CurseTools;
import ley.anvil.addonscript.v1.AddonscriptJSON;
import ley.anvil.modpacktools.Main;
import ley.anvil.modpacktools.command.CommandReturn;
import ley.anvil.modpacktools.command.ICommand;
import ley.anvil.modpacktools.command.LoadCommand;
import okhttp3.HttpUrl;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@LoadCommand
public class AddMod implements ICommand {

    @Override
    public CommandReturn execute(String[] args) {
        //Check if the command has the correct number of args
        if(args.length >= 2) {
            //TODO Theres no way this command will work in this state of course
            AddonscriptJSON json = null;
            AddonscriptJSON.Version version = null;
            if(json != null && json.versions != null) {
                if(json.versions.size() == 1) {
                    version = json.versions.get(0);
                }else {
                    for(AddonscriptJSON.Version v : json.versions) {
                        if(v.versionid == -1) {
                            version = v;
                            break;
                        }
                    }
                }
            }
            if(version == null) {
                throw new RuntimeException("Error: The modpack.json does not include a version with id -1");
            }
            //The url must match this
            String regex = "(?m)^(http)(s)?://(www\\.)?(curseforge.com/minecraft/mc-mods/)[0-z,\\-]+/(files)/[0-9]+$";
            String endPartRegex = "(/files/)[0-9]+$";
            if(args[1].matches(regex)) {
                CurseTools.addCurseRepo(json);
                try {
                    //remove fileID
                    System.out.println("Getting ID");
                    CurseProject project = CurseAPI.project(HttpUrl.get(args[1].replaceAll(endPartRegex, ""))).get();
                    int projectID = project.id();
                    //extract fileID
                    Pattern pattern = Pattern.compile("[0-9]+$");
                    Matcher matcher = pattern.matcher(args[1]);
                    int fileID = 0;
                    if(matcher.find()) {
                        fileID = Integer.parseInt(matcher.group(0));
                    }
                    File manifestFile = new File(Main.CONFIG.JAR_LOCATION, Main.CONFIG.CONFIG
                            .getTable("Locations")
                            .getString("manifestFile"));
                    System.out.println("Reading Addonscript");
                    //Get Mods in manifest file
                    //Check if Mod already exsits
                    for(AddonscriptJSON.Relation file : version.relations) {
                        if(file.file != null) {
                            String[] parts = file.file.split(":");
                            if(parts.length == 3 && parts[0].equals("curse")) { //TODO check, if it is a Curse repo, waiting for Addonscript to update this
                                int projID = Integer.parseInt(parts[1]);
                                if(projID == projectID) {
                                    return CommandReturn.fail("The Mod Is already Installed");
                                }
                            }
                        }
                    }
                    System.out.println("Adding Mod " + project.name());
                    //Construct Mod
                    AddonscriptJSON.Relation rel = new AddonscriptJSON.Relation();
                    rel.file = CurseTools.toArtifact(projectID, fileID);
                    rel.type = "included";
                    rel.installer = "internal.dir";
                    //Add Mod to array
                    if(version.relations == null) {
                        version.relations = new ArrayList<>();
                    }
                    version.relations.add(rel);
                    //Overwrite Old Manifest File
                    FileWriter manifestWriter = new FileWriter(manifestFile, false);
                    System.out.println("Printing Manifest");
                    json.write(manifestWriter);
                    manifestWriter.close();
                }catch(CurseException | IOException e) {
                    e.printStackTrace();
                }
            }else {
                AddonscriptJSON.Relation rel = new AddonscriptJSON.Relation();
                rel.file = args[1];
                rel.type = "included";
                rel.installer = "internal.dir";
                if(version.relations == null) {
                    version.relations = new ArrayList<>();
                }
                version.relations.add(rel);
            }
        }else {
            return CommandReturn.fail("Syntax: addmod <curseforge url>");
        }
        return CommandReturn.success();
    }

    @Nonnull
    @Override
    public String getName() {
        return "addmod";
    }

    @Nonnull
    @Override
    public String getHelpMessage() {
        return "This command adds a mod to the pack";
    }
}
