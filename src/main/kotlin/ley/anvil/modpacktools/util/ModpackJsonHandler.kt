package ley.anvil.modpacktools.util

import ley.anvil.addonscript.v1.AddonscriptJSON
import java.io.File
import java.io.FileReader

class ModpackJsonHandler(val modpackJsonFile: File) {
    var json: AddonscriptJSON? = null
        private set
        get() = field ?: run {
            if(modpackJsonFile.exists()) {
                val reader = FileReader(modpackJsonFile)
                field = AddonscriptJSON.read(reader, AddonscriptJSON::class.java)
                reader.close()
            }
            field
        }
}