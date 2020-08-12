package ley.anvil.modpacktools.util

import ley.anvil.addonscript.v1.AddonscriptJSON
import ley.anvil.addonscript.wrapper.ASWrapper
import ley.anvil.addonscript.wrapper.ArtifactDestination
import ley.anvil.addonscript.wrapper.MetaData
import ley.anvil.modpacktools.MPJH
import java.io.File
import java.io.FileReader

class ModpackJsonHandler(val modpackJsonFile: File) {
    //Null if no file exists
    val asWrapper: ASWrapper?
        get() {
            return if (modpackJsonFile.exists()) {
                val reader = FileReader(modpackJsonFile)
                val ret = ASWrapper(AddonscriptJSON.read(reader, AddonscriptJSON::class.java))
                reader.close()
                ret
            } else null
        }

    fun getModMetas(types: Array<String>? = null): List<MetaData> {
        val asJson = MPJH.asWrapper
        val mods = mutableListOf<MetaData>()
        val toGet = mutableListOf<ArtifactDestination>()

        for (
            rel in asJson!!.defaultVersion.getRelations(
                arrayOf("included"), /*TODO TILERA MAKE THIS NONSESE TAKE A PREDICATE AND NOT A LIST*/
                types
            )
        ) {
            if (rel.hasLocalMeta())
                mods.add(rel.localMeta)
            else if (rel.hasFile() && rel.file.isArtifact)
                toGet.add(rel.file.artifact)
        }
        mods.addAll(ASWrapper.getMetaData(toGet.toTypedArray()).values)
        return mods
    }

    val json: AddonscriptJSON?
        get() = asWrapper?.json
}
