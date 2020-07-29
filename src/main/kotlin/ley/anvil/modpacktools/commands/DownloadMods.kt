package ley.anvil.modpacktools.commands

import ley.anvil.addonscript.wrapper.FileOrLink
import ley.anvil.modpacktools.MPJH
import ley.anvil.modpacktools.command.CommandReturn
import ley.anvil.modpacktools.command.CommandReturn.Companion.fail
import ley.anvil.modpacktools.command.CommandReturn.Companion.success
import ley.anvil.modpacktools.command.ICommand
import ley.anvil.modpacktools.command.LoadCommand
import ley.anvil.modpacktools.util.FileDownloader
import ley.anvil.modpacktools.util.FileDownloader.ExistingFileBehaviour.OVERWRITE
import ley.anvil.modpacktools.util.FileDownloader.ExistingFileBehaviour.SKIP
import java.io.File
import java.net.URL
import java.nio.file.Paths
import java.util.stream.Collectors.toMap

@LoadCommand
object DownloadMods : ICommand {
    override val name: String = "downloadmods"
    override val helpMessage: String = "Downloads all mods. force always downloads files even if they are already present Syntax: <OutDir> [force]"

    override fun execute(args: Array<out String>): CommandReturn {
        if(!args.checkArgs())
            return fail("Invalid Args")


        val json = MPJH.asWrapper
        val fileList = mutableListOf<FileOrLink>()
        for (rel in json!!.defaultVersion!!.getRelations(arrayOf("client"), arrayOf("mod"))!!)
            if (rel.hasFile())
                fileList.add(rel.file.get())

        FileDownloader(
            fileList.stream()
                .filter {it.isURL}
                .filter {it.installer == "internal.dir:mods"}
                .collect(toMap<FileOrLink, URL, File>(
                    {URL(it.link)},
                    {File(args[1], Paths.get(URL(it.link).path).fileName.toString())},
                    {_: File, f: File -> f}
                )),
            {r: FileDownloader.DownloadFileTask.Return ->
                //synced so error message gets printed under response
                synchronized(this) {
                    println("${r.responseCode} ${r.responseMessage} ${r.url} ${r.file}")
                    if(r.exception != null)
                        println(r.exception.message)
                }
            },
            if("force" in args) OVERWRITE else SKIP
        )
        return success()
    }

    private fun Array<out String>.checkArgs(): Boolean = this.size >= 2 && this.elementAtOrNull(2)?.equals("force") ?: true
}
