package ley.anvil.modpacktools.util

import ley.anvil.addonscript.v1.AddonscriptJSON
import ley.anvil.addonscript.wrapper.ASWrapper
import java.io.File
import java.io.FileReader

class ModpackJsonHandler(val modpackJsonFile: File) {
    var json: ASWrapper? = null
        private set
        get() = field ?: run {
            if(modpackJsonFile.exists()) {
                val reader = FileReader(modpackJsonFile)
                field = ASWrapper(AddonscriptJSON.read(reader, AddonscriptJSON::class.java))
                reader.close()
            }
            field
        }
}