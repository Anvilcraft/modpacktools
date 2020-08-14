package ley.anvil.modpacktools.commands

import ley.anvil.addonscript.wrapper.FileOrLink
import ley.anvil.modpacktools.CONFIG
import ley.anvil.modpacktools.MPJH
import ley.anvil.modpacktools.TERMC
import ley.anvil.modpacktools.command.AbstractCommand
import ley.anvil.modpacktools.command.CommandReturn
import ley.anvil.modpacktools.command.CommandReturn.Companion.fail
import ley.anvil.modpacktools.command.LoadCommand
import ley.anvil.modpacktools.util.addonscript.installFile
import ley.anvil.modpacktools.util.downloadFiles
import ley.anvil.modpacktools.util.fPrintln
import ley.anvil.modpacktools.util.toZip
import net.sourceforge.argparse4j.inf.Namespace
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileOutputStream
import java.net.URL
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
        val toDownload = mutableMapOf<URL, Pair<String, File>>()
        MPJH.asWrapper!!.defaultVersion.getRelations {"client" in it.options}.forEach {
            if(it.hasFile())
                fileList.add(it.file.get())
        }

        fileList.forEach {
            when {
                it.isFile -> {
                    if(!it.isASDirSet)
                        it.setASDir(srcDir)

                    if(it.file.exists())
                        installFile(it.installer, it.file, tmp).printf()
                }
                it.isURL -> toDownload[it.url] = it.installer to download
                else -> return fail("${it.link} is neither a file nor an URL")
            }
        }

        downloadFiles(
            toDownload.mapValues {it.value.second},
            {
                if(it.exception == null) {
                    fPrintln("downloaded file ${it.file}", TERMC.brightBlue)
                    installFile(toDownload[it.url]!!.first, it.file, tmp).printf()
                } else {
                    fPrintln("ERROR DOWNLOADING ${it.url}")
                    it.exception.printStackTrace()
                }
            },
            resolveFileName = true
        )

        fPrintln("Making Zip", TERMC.brightGreen)
        ZipOutputStream(FileOutputStream("build/${MPJH.asWrapper!!.json.id}-${MPJH.asWrapper!!.defaultVersion.versionName}-technic.zip"))
            .use {
                tmp.toZip(it)
                it.flush()
            }

        return CommandReturn.success("Built export")
    }
}
