package ley.anvil.modpacktools.util.addonscript

import ley.anvil.addonscript.curse.ManifestJSON
import ley.anvil.addonscript.wrapper.ASWrapper
import ley.anvil.addonscript.wrapper.IInstaller
import ley.anvil.modpacktools.TERMC
import ley.anvil.modpacktools.util.baseName
import ley.anvil.modpacktools.util.fPrintln
import ley.anvil.modpacktools.util.manifest.ManifestLinksPair
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.PrintStream

/**
 * This will be returned by [installFile]
 *
 * @param success if the file was installer successfully
 * @param msg an optional message returned by [installFile]
 */
data class InstallFileSuccess(
    val success: Boolean,
    val msg: String? = null
) {
    /**
     * Prints a message of this to the [out] [PrintStream]
     *
     * @param out the [PrintStream] to print the message to or [System.out] if not specified
     */
    @JvmOverloads
    fun printf(out: PrintStream = System.out) {
        if(msg != null)
            out.fPrintln(msg, if(success) TERMC.green else TERMC.red)
    }
}

/**
 * This will install a file given an addonscript installer
 *
 * @param installer the installer to use
 * @param file the file to install
 * @param outDir the directory to install the [file] to
 */
fun installFile(installer: IInstaller, file: File, outDir: File): InstallFileSuccess {
    when(installer.installerID()) {
        "internal.override" -> {
            when {
                file.extension == "zip" -> {
                    TODO("unzip to ./.mpt/twitch/overrides")
                }

                file.isDirectory -> {
                    FileUtils.copyDirectory(file, outDir)
                }

                else -> {
                    InstallFileSuccess(false, "Only zip files can be used with \'internal.override\'")
                }
            }
        }

        "internal.dir" -> {
            val (dir) = installer.arguments
            FileUtils.copyFile(file, File(outDir, dir).resolve(file.baseName))
        }

        "internal.zip" -> {
            TODO()
        }

        else -> {
            return InstallFileSuccess(false, "The installer \'$installer\' is not supported")
        }
    }
    return InstallFileSuccess(true, "installed $file")
}

/**
 * Converts a AS file to a twitch manifest
 *
 * @param addonscript the file to convert
 * @param shouldAddLink if a link for a given relation should be added, does not get called if the relations is a curseforge artifact
 */
@JvmOverloads
fun convertAStoManifest(
    addonscript: ASWrapper,
    shouldAddLink: (ASWrapper.RelationWrapper) -> Boolean = {true}
): ManifestLinksPair {
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
                forge.id = "forge-${rel.versions.latestKnown?.split('-')?.getOrNull(1)
                    ?: throw IllegalArgumentException("Forge version format is invalid.")}"
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
            } else if(file.isArtifact && file.artifact.isCurseforge) {
                val art = file.artifact
                val f = ManifestJSON.File()
                f.fileID = art.fileID
                f.projectID = art.projectID
                f.required = "required" in rel.options
                manifest.files.add(f)
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

fun installFile(installer: String, file: File, outDir: File) =
    installFile(
        installer.split(':').let {IInstaller.create(it[0], it.slice(1 until it.size))!!},
        file,
        outDir
    )
