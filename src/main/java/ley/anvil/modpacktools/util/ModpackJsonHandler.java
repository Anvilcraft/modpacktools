package ley.anvil.modpacktools.util;

import ley.anvil.addonscript.v1.AddonscriptJSON;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ModpackJsonHandler {
    File modpackJsonFile;

    public ModpackJsonHandler(File modpackJsonFile) {
        this.modpackJsonFile = modpackJsonFile;
    }

    public AddonscriptJSON getJson() {
        try(FileReader reader = new FileReader(modpackJsonFile)) {
            return AddonscriptJSON.read(reader, AddonscriptJSON.class);
        }catch(IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public File getFile() {
        return modpackJsonFile;
    }

}
