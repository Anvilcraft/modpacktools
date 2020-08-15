package ley.anvil.modpacktools.commands

import ley.anvil.addonscript.wrapper.FileOrLink
import ley.anvil.modpacktools.MPJH
import ley.anvil.modpacktools.command.AbstractCommand
import ley.anvil.modpacktools.command.CommandReturn
import ley.anvil.modpacktools.command.CommandReturn.Companion.success
import ley.anvil.modpacktools.command.LoadCommand
import ley.anvil.modpacktools.util.FileToDownload
import ley.anvil.modpacktools.util.arg
import ley.anvil.modpacktools.util.downloadFiles
import ley.anvil.modpacktools.util.fPrintln
import net.sourceforge.argparse4j.impl.Arguments.storeTrue
import net.sourceforge.argparse4j.impl.type.FileArgumentType
import net.sourceforge.argparse4j.inf.ArgumentParser
import net.sourceforge.argparse4j.inf.Namespace
import java.io.File

@LoadCommand
object DownloadMods : AbstractCommand("DownloadMods") {
    override val helpMessage: String = "Downloads all mods."

    override fun ArgumentParser.addArgs() {
        arg("dir") {
            type(FileArgumentType().verifyCanCreate())
            help("the directory to download the mods to")
        }

        arg("-f", "--force") {
            action(storeTrue())
            help("if true, mods that are already in the download folder will be downloaded again")
        }

        arg("-a", "--all") {
            action(storeTrue())
            help("Downloads not only mods but everything with a dir installer")
        }
    }

    override fun execute(args: Namespace): CommandReturn {
        val json = MPJH.asWrapper
        val fileList = mutableListOf<FileOrLink>()
        for(rel in json!!.defaultVersion!!.getRelations {"client" in it.options && (args.getBoolean("all") || it.type == "mod")})
            if(rel.hasFile())
                fileList.add(rel.file.get())

        downloadFiles(
            fileList
                .filter {it.isURL}
                .filter {
                    val (installer, dir) = it.installer.split(':')

                    installer == "internal.dir" && (args.getBoolean("all") || dir == "mods")
                }
                .map {
                    FileToDownload(
                        if(args.getBoolean("all"))
                            File(args.get<File>("dir"), it.installer.split(':')[1])
                        else
                            args.get("dir"),
                        it.url,
                        true,
                        !args.getBoolean("force")
                    )
                }
        ) {
            println("${it.responseCode} ${it.responseMessage} ${it.file.url} ${it.downloadedFile}")
            if(it.exception != null) {
                fPrintln("ERROR DOWNLOADING ${it.file.url}")
                it.exception.printStackTrace()
            }
        }
        return success()
    }
}
