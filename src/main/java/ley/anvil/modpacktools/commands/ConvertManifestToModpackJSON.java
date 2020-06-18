package ley.anvil.modpacktools.commands;

import com.google.gson.stream.JsonReader;
import ley.anvil.addonscript.curse.ManifestJSON;
import ley.anvil.addonscript.v1.AddonscriptJSON;
import ley.anvil.modpacktools.Main;
import ley.anvil.modpacktools.command.CommandReturn;
import ley.anvil.modpacktools.command.ICommand;
import ley.anvil.modpacktools.command.LoadCommand;

import javax.annotation.Nonnull;
import java.io.*;

@LoadCommand
public class ConvertManifestToModpackJSON implements ICommand {
    @Override
    public CommandReturn execute(String[] args) {
        File manifestFile = new File(args[1]);
        if(args.length < 1)
            return CommandReturn.fail("Syntax: <manifest file>");
        if(!manifestFile.exists())
            return CommandReturn.fail("File not Found");
        if(Main.MPJH.getFile().exists())
            return CommandReturn.fail("The ModpackJSON file already exists!");

        try {
            System.out.println("Reading Manifest");
            JsonReader jsonReader = new JsonReader(new FileReader(manifestFile));
            ManifestJSON manifest = Main.GSON.fromJson(jsonReader, ManifestJSON.class);

            System.out.println("Converting");
            AddonscriptJSON addonscriptJSON = manifest.toAS();

            System.out.println("Writing");
            FileWriter modpackJsonWriter = new FileWriter(Main.MPJH.getFile());
            addonscriptJSON.write(modpackJsonWriter);
            modpackJsonWriter.close();

        }catch(FileNotFoundException e) {
            return CommandReturn.fail("File is invalid Json");
        }catch(IOException e) {
            e.printStackTrace();
        }
        return CommandReturn.success();
    }

    @Nonnull
    @Override
    public String getName() {
        return "convertmanifesttomodpackjson";
    }

    @Nonnull
    @Override
    public String getHelpMessage() {
        return "Converts a given manifest file to a modpackjson file";
    }
}
