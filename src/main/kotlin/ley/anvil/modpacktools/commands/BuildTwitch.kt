package ley.anvil.modpacktools.commands

import ley.anvil.modpacktools.CONFIG
import ley.anvil.modpacktools.MPJH
import ley.anvil.modpacktools.TERMC
import ley.anvil.modpacktools.command.AbstractCommand
import ley.anvil.modpacktools.command.CommandReturn
import ley.anvil.modpacktools.command.CommandReturn.Companion.fail
import ley.anvil.modpacktools.command.CommandReturn.Companion.success
import ley.anvil.modpacktools.command.LoadCommand
import ley.anvil.modpacktools.util.FileToDownload
import ley.anvil.modpacktools.util.addonscript.convertAStoManifest
import ley.anvil.modpacktools.util.addonscript.installFile
import ley.anvil.modpacktools.util.arg
import ley.anvil.modpacktools.util.downloadFiles
import ley.anvil.modpacktools.util.fPrintln
import ley.anvil.modpacktools.util.toZip
import net.sourceforge.argparse4j.impl.Arguments.storeTrue
import net.sourceforge.argparse4j.inf.Namespace
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.util.zip.ZipOutputStream

@LoadCommand
object BuildTwitch : AbstractCommand("BuildTwitch") {
    override val helpMessage: String = "builds a twitch export"

    private val tempDir by lazy {File(CONFIG.config.pathOrException<String>("Locations/tempDir"))}
    private val tmp: File by lazy {File(tempDir, "twitch")}
    private val downloadDir by lazy {File(tempDir, "download")}

    override val parser by argParser {
        arg("-a", "--all") {
            help("Downloads all relations instead of just required ones")
            action(storeTrue())
        }
    }

    override fun execute(args: Namespace): CommandReturn {
        val wr = MPJH.asWrapper!!
        val ml = convertAStoManifest(wr) {args.getBoolean("all") || "required" in it.options}
        val archiveName = "${wr.json.id}-${wr.defaultVersion.versionName}-twitch"
        val dir = File("./build")
        val toDownload = mutableMapOf<FileToDownload, String>()
        val srcDir by lazy {File(CONFIG.config.pathOrException<String>("Locations/src"))}
        val overrides by lazy {File(tmp, "overrides")}
        dir.mkdirs()
        tmp.mkdirs()
        downloadDir.mkdirs()
        FileUtils.cleanDirectory(tmp)
        FileUtils.cleanDirectory(downloadDir)

        val writer = FileWriter(tmp.path + "/manifest.json")
        ml.manifest?.write(writer)
        writer.close()

        for(uf in ml.links) {
            if(uf.key.isFile) {
                if(!uf.key.isASDirSet)
                    uf.key.setASDir(srcDir)
                val file = uf.key.file
                if(file.exists()) {
                    installFile(uf.value, file, overrides).printf()
                }
            } else if(uf.key.isURL) {
                toDownload[FileToDownload(downloadDir, uf.key.url, true)] = uf.value
            } else {
                return fail("{$uf.key.link} is neither a file nor an URL")
            }
        }

        downloadFiles(
            toDownload.keys.toList()
        ) {
            if(it.downloadedFile != null) {
                fPrintln("downloaded file ${it.file.url}", TERMC.brightBlue)
                installFile(toDownload[it.file]!!, it.downloadedFile, overrides).printf()
            } else if(it.exception != null) {
                fPrintln("ERROR DOWNLOADING ${it.file}")
                it.exception.printStackTrace()
            }
        }

        fPrintln("Creating zip", TERMC.brightGreen)
        val zip = ZipOutputStream(FileOutputStream("${dir.path}/$archiveName.zip"))
        tmp.toZip(zip)
        zip.flush()
        zip.close()
        return success("Successfully built twitch zip")
    }
}
