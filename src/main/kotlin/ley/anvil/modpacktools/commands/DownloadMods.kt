package ley.anvil.modpacktools.commands

import ley.anvil.addonscript.wrapper.ASWrapper
import ley.anvil.addonscript.wrapper.LinkInstPair
import ley.anvil.modpacktools.Main.MPJH
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


        val json = MPJH.json
        var filelist = ArrayList<ASWrapper.FileWrapper>()
        for (rel in json?.defaultVersion?.getRelations(arrayOf("client"), "mod")!!) {
            if (rel.hasFile())
                filelist.add(rel.file)
        }

        FileDownloader(
            filelist.stream()
                .filter {it.isURL}
                .collect(toMap<ASWrapper.FileWrapper, URL, File>(
                    {URL(it.link)}, //TODO Get the link using Multithreadding
                    {File(args[1], Paths.get(URL(it.link).path).fileName.toString())},
                    {_: File, f: File -> f}
                )),
            {r: FileDownloader.DownloadFileTask.Return ->
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