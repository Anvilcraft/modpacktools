package ley.anvil.modpacktools.commands

import ley.anvil.addonscript.wrapper.FileOrLink
import ley.anvil.modpacktools.CONFIG
import ley.anvil.modpacktools.MPJH
import ley.anvil.modpacktools.TERMC
import ley.anvil.modpacktools.command.AbstractCommand
import ley.anvil.modpacktools.command.CommandReturn
import ley.anvil.modpacktools.command.CommandReturn.Companion.fail
import ley.anvil.modpacktools.command.LoadCommand
import ley.anvil.modpacktools.util.FileToDownload
import ley.anvil.modpacktools.util.addonscript.installFile
import ley.anvil.modpacktools.util.downloadFiles
import ley.anvil.modpacktools.util.fPrintln
import ley.anvil.modpacktools.util.toZip
import net.sourceforge.argparse4j.inf.Namespace
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipOutputStream

@LoadCommand
object BuildTechnic : AbstractCommand("BuildTechnic") {
    override val helpMessage = "Builds a technic export"

    private val tempDir by lazy {File(CONFIG.config.pathOrException<String>("Locations/tempDir"))}
    private val tmp: File by lazy {File(tempDir, "technic")}
    private val download: File by lazy {File(tempDir, "download")}
    val srcDir by lazy {File(CONFIG.config.pathOrException<String>("Locations/src"))}

    override fun execute(args: Namespace): CommandReturn {
        //Setup dirs
        arrayOf(tmp, download)
            .forEach {
                it.mkdirs()
                FileUtils.cleanDirectory(it)
            }

        val fileList = mutableListOf<FileOrLink>()
        //Map of File to Installer
        val toDownload = mutableMapOf<FileToDownload, String>()

        //RELATIONS
        fileList.addAll(
            MPJH.asWrapper!!.defaultVersion.getRelations {"client" in it.options}.filter {it.hasFile()}
                .map {it.file.get()}
        )
        //FILES
        fileList.addAll(MPJH.asWrapper!!.defaultVersion.getFiles {true}.map {it.get()})

        //FORGE
        @Suppress("DEPRECATION") //no idea why this is deprecated. Too Bad!
        val forge = MPJH.asWrapper!!.defaultVersion.getRelations {it.id.toLowerCase() == "forge"}.first().forgeUniversal
        toDownload[
            FileToDownload(
                //Technic wants it to be called modpack.jar
                File(download, "modpack.jar"),
                forge.url
            )
        ] = "internal.dir:bin"

        fileList.forEach {
            when {
                !it.isURL -> {
                    if(!it.isASDirSet)
                        it.setASDir(srcDir)
                    if(it.file.exists())
                        installFile(it.installer, it.file, tmp).printf()
                }
                it.isURL -> toDownload[FileToDownload(download, it.url, true)] = it.installer
                else -> return fail("${it.link} is neither a file nor an URL")
            }
        }

        downloadFiles(toDownload.keys.toList()) {
            if(it.downloadedFile != null) {
                fPrintln("${it.responseCode} ${it.responseMessage} ${it.file.url} ${it.downloadedFile}", TERMC.brightBlue)
                //Use map of file to installer to get installer for given file
                installFile(toDownload[it.file]!!, it.downloadedFile, tmp).printf()
            } else if(it.exception != null) {
                fPrintln("ERROR DOWNLOADING ${it.file.url}")
                it.exception.printStackTrace()
            }
        }

        fPrintln("Making Zip", TERMC.brightGreen)
        ZipOutputStream(FileOutputStream("build/${MPJH.asWrapper!!.json.id}-${MPJH.asWrapper!!.defaultVersion.versionName}-technic.zip"))
            .use {
                tmp.toZip(it)
                it.flush()
            }

        return CommandReturn.success("Built export")
    }
}
