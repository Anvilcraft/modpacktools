package ley.anvil.modpacktools.commands

import j2html.TagCreator.a
import j2html.TagCreator.b
import j2html.TagCreator.body
import j2html.TagCreator.each
import j2html.TagCreator.head
import j2html.TagCreator.html
import j2html.TagCreator.img
import j2html.TagCreator.li
import j2html.TagCreator.p
import j2html.TagCreator.style
import j2html.TagCreator.table
import j2html.TagCreator.td
import j2html.TagCreator.tr
import j2html.TagCreator.ul
import j2html.utils.CSSMin.compressCss
import ley.anvil.addonscript.wrapper.MetaData
import ley.anvil.modpacktools.MPJH
import ley.anvil.modpacktools.TERMC
import ley.anvil.modpacktools.command.AbstractCommand
import ley.anvil.modpacktools.command.CommandReturn
import ley.anvil.modpacktools.command.CommandReturn.Companion.success
import ley.anvil.modpacktools.command.LoadCommand
import ley.anvil.modpacktools.util.fPrintln
import net.sourceforge.argparse4j.impl.Arguments.storeTrue
import net.sourceforge.argparse4j.impl.type.CaseInsensitiveEnumNameArgumentType
import net.sourceforge.argparse4j.impl.type.FileArgumentType
import net.sourceforge.argparse4j.inf.ArgumentParser
import net.sourceforge.argparse4j.inf.Namespace
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileWriter
import java.nio.charset.StandardCharsets
import java.util.Comparator.comparing

@LoadCommand
object CreateModlist : AbstractCommand("CreateModlist") {
    override val helpMessage: String = "This creates a modlist either as html or csv file."

    override fun ArgumentParser.addArgs() {
        addArgument("-s", "--sorting")
            .type(CaseInsensitiveEnumNameArgumentType(Sorting::class.java))
            .setDefault(Sorting.NAME)
            .help("Determines How mods should be sorted")

        addArgument("-a", "--all")
            .action(storeTrue())
            .help("If this is set, all relations and not only be mods will be in the list")

        addArgument("type")
            .type(CaseInsensitiveEnumNameArgumentType(Format::class.java))
            .help("What format the mod list should be made in")

        addArgument("file")
            .type(FileArgumentType().verifyNotExists())
            .help("What file the mod list should be written to")
    }

    override fun execute(args: Namespace): CommandReturn {
        val outFile = args.get<File>("file")

        val all = args.getBoolean("all")
        val sorting: Comparator<MetaData> = when(args.get<Sorting>("sorting")!!) {
            Sorting.NAME -> comparing<MetaData, String> {it.name}
            Sorting.DESCRIPTION -> comparing<MetaData, String> {it.description?.getOrNull(0) ?: ""}
            Sorting.AUTHOR -> comparing<MetaData, String> {it.contributors.keys.first()}
        }

        return when(args.get<Format>("type")!!) {
            Format.HTML -> doHtml(outFile, all, sorting)
            Format.CSV -> doCsv(outFile, all, sorting)
        }
    }

    private fun doCsv(outFile: File, all: Boolean, sorting: Comparator<MetaData>): CommandReturn {
        println("Making CSV file $outFile")
        val printer = CSVPrinter(FileWriter(outFile), CSVFormat.EXCEL.withDelimiter(';'))

        printer.printRecord("Name", "Contributors", "Link")
        printer.println()

        for(mod in getMods(all, sorting)) {
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

    private fun doHtml(outFile: File, all: Boolean, sorting: Comparator<MetaData>): CommandReturn {
        fPrintln("Making HTML file $outFile", TERMC.green)
        val writer = FileWriter(outFile)
        val html = html(
            head(
                style(
                    //Fancy css!
                    compressCss(
                        IOUtils.toString(
                            ClassLoader.getSystemResourceAsStream("commands/createmodlist/style.css"),
                            StandardCharsets.UTF_8
                        )
                    )
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
                    each(getMods(all, sorting)) {
                        fPrintln("Writing relation ${it.name}", TERMC.blue)
                        tr(
                            td(
                                if(it.icon != null) a(
                                    img().withSrc(it.icon)
                                        .withClass("img")
                                ).withHref(it.website) else null
                            ),
                            td(
                                run {
                                    val a = a(it.name)
                                        //Open in new tab
                                        .withRel("noopener noreferrer")
                                        .withTarget("_blank")
                                    if(it.website != null)
                                        a.withHref(it.website)
                                    a
                                }
                            ),
                            td(
                                ul(
                                    each(it.contributors) {contr ->
                                        li(contr.key)
                                            //for contributor colors
                                            .withClass("contributor_${contr.value.getOrElse(0) {""}}")
                                    }
                                )
                            ),
                            td(
                                each(it.description?.asList() ?: listOf()) {d: String ->
                                    p(d)
                                }
                            )
                                .withClass("description")
                        )
                    }
                )
            )
        ).render()

        writer.write(html)
        writer.close()
        return success("Wrote HTML file")
    }

    private fun getMods(all: Boolean, sorting: Comparator<MetaData>): List<MetaData> =
        MPJH.getModMetas(if(all) null else arrayOf("mod")).sortedWith(sorting)

    enum class Format {
        HTML, CSV
    }

    enum class Sorting {
        NAME, DESCRIPTION, AUTHOR
    }
}
