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
import java.nio.file.Paths
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
        parser
    }

    override fun execute(args: Namespace): CommandReturn {
        val json = MPJH.asWrapper
        val fileList = mutableListOf<FileOrLink>()
        for (rel in json!!.defaultVersion!!.getRelations(arrayOf("client"), arrayOf("mod"))!!)
            if (rel.hasFile())
                fileList.add(rel.file.get())

        downloadFiles(
            fileList.stream()
                .filter {it.isURL}
                .filter {it.installer == "internal.dir:mods"}
                .collect(toMap<FileOrLink, URL, File>(
                    {URL(it.link)},
                    {File(args.get<File>("dir"), Paths.get(URL(it.link).path).fileName.toString())},
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
            args.get<Boolean>("force") == null
        )
        return success()
    }
}
