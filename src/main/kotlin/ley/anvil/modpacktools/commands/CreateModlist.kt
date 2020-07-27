package ley.anvil.modpacktools.commands

import j2html.TagCreator.*
import ley.anvil.addonscript.wrapper.ASWrapper
import ley.anvil.addonscript.wrapper.ArtifactDestination
import ley.anvil.addonscript.wrapper.MetaData
import ley.anvil.modpacktools.MPJH
import ley.anvil.modpacktools.command.CommandReturn
import ley.anvil.modpacktools.command.CommandReturn.Companion.fail
import ley.anvil.modpacktools.command.CommandReturn.Companion.success
import ley.anvil.modpacktools.command.ICommand
import ley.anvil.modpacktools.command.LoadCommand
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.File
import java.io.FileWriter

@LoadCommand
object CreateModlist : ICommand {
    override val name: String = "createmodlist"
    override val helpMessage: String = "This creates a modlist either as html or csv file. Syntax: <html/csv> outFile"

    override fun execute(args: Array<out String>): CommandReturn {
        if(!args.checkArgs())
            return fail("invalid args")

        val outFile = File(args[2])

        if(outFile.exists())
            return fail("File already exists!")

        return if(args[1] == "html")
            doHtml(outFile)
        else
            doCsv(outFile)
    }

    private fun doCsv(outFile: File): CommandReturn {
        println("Making CSV file $outFile")
        val printer = CSVPrinter(FileWriter(outFile), CSVFormat.EXCEL.withDelimiter(';'))

        printer.printRecord("Name", "Contributors", "Link")
        printer.println()

        for(mod in getMods()) {
            printer.printRecord(
                mod.name,
                mod.contributors.keys.joinToString(),
                mod.website,
                mod.description?.joinToString(" ")
            )
        }
        printer.close()
        return success("Wrote CSV file")
    }

    private fun doHtml(outFile: File): CommandReturn {
        println("Making HTML file $outFile")
        val writer = FileWriter(outFile)
        val html = html(
            head(
                style(
                    //Fancy css!
                    """
a:link {
 color: #ff5555;
}
a:visited {
 color: #cc55cc;
}
body {
 background-color: #333333;
 color: #ffffff
}
.img {
 width:100px;
}
td {
 border: #999999 3px;
 border-style: solid;
}
.description {
  width: 100%
}
"""
                        //trim unnecessary chars
                        .trimIndent().filter {it != '\n'}
                )
            ),
            body(
                table(
                    tr(
                        td(),
                        td(b("Name")),
                        td(b("Contributors")),
                        td(b("Description"))
                    ),
                    each(getMods()) {
                        tr(
                            td(a(
                                img().withSrc(it.icon)
                                    .withClass("img")
                            ).withHref(it.website)
                            ),
                            td(a(it.name)
                                .withHref(it.website)
                                //Open in new tab
                                .withRel("noopener noreferrer")
                                .withTarget("_blank")),
                            td(ul(
                                each(it.contributors) {contr ->
                                    li(contr.key)
                                }
                            )),
                            td(it.description?.joinToString("<br />") ?: "")
                                .withClass("description")
                        )
                    }
                )
            )).render()

        writer.write(html)
        writer.close()
        return success("Wrote HTML file")
    }

    private fun getMods(): List<MetaData> {
        println("Getting mods...")
        val asJson = MPJH.asWrapper
        val mods = mutableListOf<MetaData>()
        val toGet = mutableListOf<ArtifactDestination>()

        for(rel in asJson!!.defaultVersion.getRelations(arrayOf("client"), null)) {
            if(rel.hasLocalMeta())
                mods.add(rel.localMeta)
            else if(rel.hasFile() && rel.file.isArtifact)
                toGet.add(rel.file.artifact)
        }
        mods.addAll(ASWrapper.getMetaData(toGet.toTypedArray()).values)
        return mods.sortedBy {m -> m.name?.toLowerCase()}
    }

    private fun Array<out String>.checkArgs(): Boolean = this.size >= 3 && this[1] in arrayOf("html", "csv")

}