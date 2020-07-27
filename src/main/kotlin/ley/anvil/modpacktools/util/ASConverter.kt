package ley.anvil.modpacktools.util

import ley.anvil.addonscript.curse.ManifestJSON
import ley.anvil.addonscript.wrapper.ASWrapper

fun convertAStoManifest(addonscript: ASWrapper): ManifestLinksPair {
    var ml = ManifestLinksPair()
    var ver = addonscript.defaultVersion
    var manifest = ManifestJSON()

    var mcv = ver.version?.mcversion!![0]
    manifest.minecraft = ManifestJSON.Minecraft()
    manifest.minecraft.version = mcv
    manifest.minecraft.modLoaders = mutableListOf()
    manifest.files = mutableListOf()

    manifest.manifestType = "minecraftModpack"
    manifest.manifestVersion = 1
    manifest.name = addonscript.json.meta!!.name
    if (manifest.name == null)
        manifest.name = addonscript.json.id
    manifest.version = ver.versionName
    manifest.author = addonscript.json!!.meta!!.contributors[0].name


    for (rel in ver.getRelations(arrayOf("client"), null)) {
        if (rel.isModloader) {
            if (rel.relation.id == "forge") {
                var forge = ManifestJSON.Modloader()
                forge.primary = true
                forge.id = "forge-" + rel.versions.latestKnown
                manifest.minecraft.modLoaders.add(forge)
            } else {
                println("Curse only allows Forge as a modloader. " + rel.relation.id + " is not allowed!")
            }
        } else if (rel.hasFile()) {
            var file = rel.file
            if (file.file.installer == "internal.jar") {
                println("internal.jar is not supportet on Curse")
                continue
            } else if (file.isArtifact) {
                var art = file.artifact
                if (art.isCurseforge) {
                    var f = ManifestJSON.File()
                    f.fileID = art.fileID
                    f.projectID = art.projectID
                    f.required = rel.options.contains("required")
                    manifest.files.add(f);
                }
            } else if (rel.options.contains("required")) {
                ml.links.put(file.link, file.file.installer)
            }
        }
    }

    for (file in ver.getFiles(arrayOf("client", "required"), null)) {
        if (file.file.installer == "internal.jar") {
            println("internal.jar is not supportet on Curse")
            continue
        } else if (file.isArtifact) {
            var art = file.artifact
            if (art.isCurseforge) {
                var f = ManifestJSON.File()
                f.fileID = art.fileID
                f.projectID = art.projectID
                f.required = true
                manifest.files.add(f);
            }
        } else {
            ml.links.put(file.link, file.file.installer)
        }
    }
    ml.manifest = manifest

    return ml
}