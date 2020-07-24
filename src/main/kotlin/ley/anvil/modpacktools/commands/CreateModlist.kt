package ley.anvil.modpacktools.commands

import j2html.TagCreator.*
import ley.anvil.addonscript.v1.AddonscriptJSON
import ley.anvil.modpacktools.Main
import ley.anvil.modpacktools.command.CommandReturn
import ley.anvil.modpacktools.command.ICommand
import ley.anvil.modpacktools.command.LoadCommand
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.File
import java.io.FileWriter

@LoadCommand
class CreateModlist : ICommand {
    override fun getName(): String = "createmodlist"
    override fun getHelpMessage(): String = "This creates a modlist either as html or csv file. Syntax: <html/csv> outFile"

    override fun execute(args: Array<out String>): CommandReturn {
        if(!args.checkArgs())
            return CommandReturn.fail("invalid args")

        val outFile = File(args[2])

        if(outFile.exists())
            return CommandReturn.fail("File already exists!")

        when(args[1]) {
            "csv" -> {
                println("Making CSV file $outFile")
                val printer = CSVPrinter(FileWriter(outFile), CSVFormat.EXCEL.withDelimiter(';'))

                printer.printRecord("Name", "Contributors", "Link")
                printer.println()

                for(mod in getMods()) {
                    printer.printRecord(
                        mod.name,
                        mod.contributors.joinToString {c -> c.name},
                        mod.website
                    )
                }
                printer.close()
                return CommandReturn.success("Wrote CSV file")
            }

            "html" -> {
                println("Making HTML file $outFile")
                val writer = FileWriter(outFile)
                val html = body(
                    table(
                        tr(
                            td(b("Name")),
                            td(b("Contributors"))
                        ),
                        each(getMods()) {
                            tr(s(it.name),
                                a(it.name)
                                    .withHref(it.website)
                                    //Open in new tab
                                    .withRel("noopener noreferrer")
                                    .withTarget("_blank"),
                                ul(
                                    each(it.contributors) {contr ->
                                        li(contr.name)
                                    }
                                )
                            )
                        }
                    )
                ).render()

                writer.write(html)
                writer.close()
                return CommandReturn.success("Wrote HTML file")
            }

            else -> throw IllegalStateException("Unreachable")
        }
    }

    private fun getMods(): List<AddonscriptJSON.Meta> {
        val asJson = Main.MPJH.json
        val mods = ArrayList<AddonscriptJSON.Meta>()

        asJson.load()

        for(rel in asJson.defaultVersion.getRelations("client", false, null)) {
            val meta = rel.getMeta(asJson.indexes)
            if(meta.name != null) mods.add(meta) else println("meta name == null")
        }
        return mods.sortedBy {m -> m.name.toLowerCase()}
    }

    private fun Array<out String>.checkArgs(): Boolean = this.size >= 3 && this[1] in arrayOf("html", "csv")

}