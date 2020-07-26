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
            AddonscriptJSON json = Main.MPJH.getJson();
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
                    System.out.println("Reading Addonscript");
                    //Get Mods in manifest file
                    //Check if Mod already exsits
                    for(AddonscriptJSON.Relation file : version.relations) {
                        if(file.file != null && file.file.artifact != null) {
                            String[] parts = file.file.artifact.split(":");
                            if(parts.length == 3 && parts[0].equals("curse")) {
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
                    rel.file.installer = "internal.dir";
                    //Add Mod to array
                    if(version.relations == null) {
                        version.relations = new ArrayList<>();
                    }
                    version.relations.add(rel);
                }catch(CurseException e) {
                    e.printStackTrace();
                }
            }else {
                AddonscriptJSON.Relation rel = new AddonscriptJSON.Relation();
                rel.file = new AddonscriptJSON.File();
                rel.file.link = args[1];
                rel.type = "included";
                rel.file.installer = "internal.dir";
                if(version.relations == null) {
                    version.relations = new ArrayList<>();
                }
                version.relations.add(rel);
            }
            //Overwrite Old Manifest File
            FileWriter manifestWriter = null;
            try {
                manifestWriter = new FileWriter(Main.MPJH.getModpackJsonFile(), false);
                System.out.println("Printing Manifest");
                json.write(manifestWriter);
                manifestWriter.close();
            }catch(IOException e) {
                e.printStackTrace();
            }
        }else {
            return CommandReturn.fail("Syntax: addmod <curseforge url>");
        }
        return CommandReturn.success();
    }

    @Override
    public String getName() {
        return "addmod";
    }

    @Override
    public String getHelpMessage() {
        return "This command adds a mod to the pack";
    }
}
