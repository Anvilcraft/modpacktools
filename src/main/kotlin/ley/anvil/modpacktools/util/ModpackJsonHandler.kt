package ley.anvil.modpacktools.util

import ley.anvil.addonscript.v1.AddonscriptJSON
import ley.anvil.addonscript.wrapper.ASWrapper
import ley.anvil.addonscript.wrapper.ArtifactDestination
import ley.anvil.addonscript.wrapper.MetaData
import ley.anvil.modpacktools.MPJH
import java.io.File
import java.io.FileReader

/**
 * This Class is responsible for reading and parsing the [modpackJsonFile]
 *
 * @param modpackJsonFile the file where modpack.json is located
 */
class ModpackJsonHandler(val modpackJsonFile: File) {
    /**
     * the [ASWrapper] for this [modpackJsonFile].
     * null if [modpackJsonFile] does not exist
     */
    val asWrapper: ASWrapper?
        get() {
            return if(modpackJsonFile.exists()) {
                val reader = FileReader(modpackJsonFile)
                val ret = ASWrapper(AddonscriptJSON.read(reader, AddonscriptJSON::class.java))
                reader.close()
                ret
            } else null
        }

    /**
     * returns all [MetaData]s from the [AddonscriptJSON.Relation]s in this [modpackJsonFile]
     *
     * @param shouldInclude a Predicate which is used to determine if
     * a given [AddonscriptJSON.Relation] should be included in the list.
     * will include all relations by default
     * @return the list of [MetaData]s of relations in this [modpackJsonFile]
     */
    @JvmOverloads
    fun getModMetas(shouldInclude: (AddonscriptJSON.Relation) -> Boolean = {true}): List<MetaData> {
        val asJson = MPJH.asWrapper
        val mods = mutableListOf<MetaData>()
        val toGet = mutableListOf<ArtifactDestination>()

        for(rel in asJson!!.defaultVersion.getRelations(shouldInclude)) {
            if(rel.hasLocalMeta())
                mods.add(rel.localMeta)
            else if(rel.hasFile() && rel.file.isArtifact)
                toGet.add(rel.file.artifact)
        }
        mods.addAll(ASWrapper.getMetaData(toGet.toTypedArray()).values)
        return mods
    }
}
