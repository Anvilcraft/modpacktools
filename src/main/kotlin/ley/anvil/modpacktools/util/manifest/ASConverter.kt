package ley.anvil.modpacktools.util.manifest

import ley.anvil.addonscript.curse.ManifestJSON
import ley.anvil.addonscript.wrapper.ASWrapper

/**
 * Converts a AS file to a twitch manifest
 *
 * @param addonscript the file to convert
 * @param shouldAddLink if a link for a given relation should be added, does not get called if the relations is a curseforge artifact
 */
@JvmOverloads
fun convertAStoManifest(addonscript: ASWrapper, shouldAddLink: (ASWrapper.RelationWrapper) -> Boolean = {true}): ManifestLinksPair {
    val ml = ManifestLinksPair()
    val ver = addonscript.defaultVersion
    val manifest = ManifestJSON()

    val mcv = ver.version!!.mcversion[0]
    manifest.minecraft = ManifestJSON.Minecraft()
    manifest.minecraft.version = mcv
    manifest.minecraft.modLoaders = mutableListOf()
    manifest.files = mutableListOf()

    manifest.manifestType = "minecraftModpack"
    manifest.manifestVersion = 1
    manifest.name = addonscript.json.meta!!.name ?: addonscript.json.id
    manifest.version = ver.versionName
    manifest.author = addonscript.json!!.meta!!.contributors[0].name


    for(rel in ver.getRelations(arrayOf("client"), null)) {
        if(rel.isModloader) {
            if(rel.relation.id == "forge") {
                val forge = ManifestJSON.Modloader()
                forge.primary = true
                forge.id = "forge-" + rel.versions.latestKnown
                manifest.minecraft.modLoaders.add(forge)
            } else {
                println("Curse only allows Forge as a modloader. ${rel.relation.id} is not allowed!")
            }
        } else if(rel.hasFile()) {
            val file = rel.file
            //TODO deduplicate this
            if(file.file.installer == "internal.jar") {
                println("internal.jar is not supported on Curse")
                continue
            } else if(file.isArtifact) {
                val art = file.artifact
                if(art.isCurseforge) {
                    val f = ManifestJSON.File()
                    f.fileID = art.fileID
                    f.projectID = art.projectID
                    f.required = "required" in rel.options
                    manifest.files.add(f)
                }
            } else if(shouldAddLink(rel)) {
                ml.links[file.get()] = file.file.installer
            }
        }
    }

    for(file in ver.getFiles(arrayOf("client", "required"), null)) {
        if(file.file.installer == "internal.jar") {
            println("internal.jar is not supported on Curse")
            continue
        } else if(file.isArtifact) {
            val art = file.artifact
            if(art.isCurseforge) {
                val f = ManifestJSON.File()
                f.fileID = art.fileID
                f.projectID = art.projectID
                f.required = true
                manifest.files.add(f)
            }
        } else {
            ml.links[file.get()] = file.file.installer
        }
    }
    ml.manifest = manifest

    return ml
}