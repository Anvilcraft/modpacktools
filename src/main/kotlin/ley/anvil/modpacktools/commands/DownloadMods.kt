package ley.anvil.modpacktools.commands

import ley.anvil.addonscript.wrapper.FileOrLink
import ley.anvil.modpacktools.MPJH
import ley.anvil.modpacktools.command.CommandReturn
import ley.anvil.modpacktools.command.CommandReturn.Companion.success
import ley.anvil.modpacktools.command.ICommand
import ley.anvil.modpacktools.command.LoadCommand
import ley.anvil.modpacktools.util.DownloadFileTask
import ley.anvil.modpacktools.util.downloadFiles
import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.impl.Arguments.storeTrue
import net.sourceforge.argparse4j.impl.type.FileArgumentType
import net.sourceforge.argparse4j.inf.ArgumentParser
import net.sourceforge.argparse4j.inf.Namespace
import java.io.File
import java.net.URL
import java.util.stream.Collectors.toMap

@LoadCommand
object DownloadMods : ICommand {
    override val name: String = "downloadmods"
    override val helpMessage: String = "Downloads all mods."
    override val parser: ArgumentParser = run {
        val parser = ArgumentParsers.newFor("DownloadMods")
            .build()
            .description(helpMessage)

        parser.addArgument("dir")
            .type(FileArgumentType().verifyCanCreate())
            .help("the directory to download the mods to")

        parser.addArgument("-f", "--force")
            .action(storeTrue())
            .help("if true, mods that are already in the download folder will be downloaded again")

        parser.addArgument("-a", "--all")
            .action(storeTrue())
            .help("Downloads not only mods but everything with a dir installer")

        parser
    }

    override fun execute(args: Namespace): CommandReturn {
        val json = MPJH.asWrapper
        val fileList = mutableListOf<FileOrLink>()
        for(rel in json!!.defaultVersion!!.getRelations(arrayOf("client"), if(args.getBoolean("all")) null else arrayOf("mod"))!!) //TODO only client? what if someone wants a server?
            if(rel.hasFile())
                fileList.add(rel.file.get())

        downloadFiles(
            fileList.stream()
                .filter {it.isURL}
                .filter {
                    val (installer, dir) = it.installer.split(':')

                    installer == "internal.dir" && (args.getBoolean("all") || dir == "mods")
                }
                .collect(toMap<FileOrLink, URL, File>(
                    {URL(it.link)},
                    {
                        val dir = it.installer.split(':').last()

                        if(args.getBoolean("all"))
                            File(args.get<File>("dir"), dir)
                        else
                            args.get<File>("dir")
                    },
                    {_: File, f: File -> f}
                )),
            {r: DownloadFileTask.Return ->
                //synced so error message gets printed under response
                synchronized(this) {
                    println("${r.responseCode} ${r.responseMessage} ${r.url} ${r.file}")
                    if(r.exception != null)
                        println(r.exception.message)
                }
            },
            !args.getBoolean("force"),
            true
        )
        return success()
    }
}
