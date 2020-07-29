package ley.anvil.modpacktools.commands

import j2html.TagCreator.*
import j2html.utils.CSSMin.compressCss
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
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileWriter
import java.nio.charset.StandardCharsets

@LoadCommand
object CreateModlist : ICommand {
    override val name: String = "createmodlist"
    override val helpMessage: String = "This creates a modlist either as html or csv file. if the \'all\' option is supplied, not only mods will be included Syntax: <html/csv> <outFile> [all]"

    override fun execute(args: Array<out String>): CommandReturn {
        if(!args.checkArgs())
            return fail("invalid args")

        val outFile = File(args[2])

        if(outFile.exists())
            return fail("File already exists!")

        val all = args.getOrNull(3) == "all"
        return if(args[1] == "html")
            doHtml(outFile, all)
        else
            doCsv(outFile, all)
    }

    private fun doCsv(outFile: File, all: Boolean): CommandReturn {
        println("Making CSV file $outFile")
        val printer = CSVPrinter(FileWriter(outFile), CSVFormat.EXCEL.withDelimiter(';'))

        printer.printRecord("Name", "Contributors", "Link")
        printer.println()

        for(mod in getMods(all)) {
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

    private fun doHtml(outFile: File, all: Boolean): CommandReturn {
        println("Making HTML file $outFile")
        val writer = FileWriter(outFile)
        val html = html(
            head(
                style(
                    //Fancy css!
                    compressCss(IOUtils.toString(ClassLoader.getSystemResourceAsStream("commands/createmodlist/style.css"), StandardCharsets.UTF_8))
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
                    each(getMods(all)) {
                        tr(
                            td(if(it.icon != null) a(
                                img().withSrc(it.icon)
                                    .withClass("img")
                            ).withHref(it.website) else null
                            ),
                            td(run {
                                val a = a(it.name)
                                    //Open in new tab
                                    .withRel("noopener noreferrer")
                                    .withTarget("_blank")
                                if(it.website != null)
                                    a.withHref(it.website)
                                a
                            }),
                            td(ul(
                                each(it.contributors) {contr ->
                                    li(contr.key)
                                }
                            )),
                            td(each(it.description?.asList() ?: listOf()) {d: String ->
                                p(d)
                            })
                                .withClass("description")
                        )
                    }
                )
            )).render()

        writer.write(html)
        writer.close()
        return success("Wrote HTML file")
    }

    private fun getMods(all: Boolean): List<MetaData> {
        println("Getting mods...")
        val asJson = MPJH.asWrapper
        val mods = mutableListOf<MetaData>()
        val toGet = mutableListOf<ArtifactDestination>()

        for(rel in asJson!!.defaultVersion.getRelations(arrayOf("included"), /*null means all*/ if(all) null else arrayOf("mod"))) {
            if(rel.hasLocalMeta())
                mods.add(rel.localMeta)
            else if(rel.hasFile() && rel.file.isArtifact)
                toGet.add(rel.file.artifact)
        }
        mods.addAll(ASWrapper.getMetaData(toGet.toTypedArray()).values)
        return mods.sortedBy {m -> m.name?.toLowerCase()}
    }

    private fun Array<out String>.checkArgs(): Boolean =
        //must be right length
        this.size >= 3 &&
            //second option must be html or csv
            this[1] in arrayOf("html", "csv") &&
            //3rd option must either be "all" or nothing
            this.getOrElse(3) {"all"} == "all"
}