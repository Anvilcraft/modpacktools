package ley.anvil.modpacktools.commands

import j2html.utils.CSSMin.compressCss
import ley.anvil.addonscript.wrapper.MetaData
import ley.anvil.modpacktools.MPJH
import ley.anvil.modpacktools.TERMC
import ley.anvil.modpacktools.command.AbstractCommand
import ley.anvil.modpacktools.command.CommandReturn
import ley.anvil.modpacktools.command.CommandReturn.Companion.success
import ley.anvil.modpacktools.command.LoadCommand
import ley.anvil.modpacktools.util.BuilderContainerTag.Companion.html
import ley.anvil.modpacktools.util.arg
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
        arg("type") {
            type(CaseInsensitiveEnumNameArgumentType(Format::class.java))
            help("What format the mod list should be made in")
        }

        arg("file") {
            type(FileArgumentType().verifyNotExists())
            help("What file the mod list should be written to")
        }

        arg("-s", "--sorting") {
            default = Sorting.NAME
            type(CaseInsensitiveEnumNameArgumentType(Sorting::class.java))
            help("Determines How mods should be sorted")
        }

        arg("-a", "--all") {
            action(storeTrue())
            help("If this is set, all relations and not only be mods will be in the list")
        }

        arg("-n", "--noheader") {
            action(storeTrue())
            help("If this is set, the mod list will not have a header")
        }
    }

    override fun execute(args: Namespace): CommandReturn {
        val outFile = args.get<File>("file")

        val all = args.getBoolean("all")
        val sorting: Comparator<MetaData> = when(args.get<Sorting>("sorting")!!) {
            Sorting.NAME -> comparing {it.name?.toLowerCase() ?: ""}
            Sorting.DESCRIPTION -> comparing {it.description?.getOrNull(0)?.toLowerCase() ?: ""}
            Sorting.AUTHOR -> comparing {it.contributors.keys.first().toLowerCase()}
        }

        return when(args.get<Format>("type")!!) {
            Format.HTML -> doHtml(outFile, all, sorting, args.getBoolean("noheader"))
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

    private fun doHtml(outFile: File, all: Boolean, sorting: Comparator<MetaData>, noHeader: Boolean): CommandReturn {
        fPrintln("Making HTML file $outFile", TERMC.green)
        val writer = FileWriter(outFile)
        val html = html {
            "head" {
                "style"(
                    compressCss(
                        IOUtils.toString(
                            ClassLoader.getSystemResourceAsStream("commands/createmodlist/style.css"),
                            StandardCharsets.UTF_8
                        )
                    )
                )
            }

            "body" {
                if(!noHeader)
                    "div" {
                        val meta = MPJH.asWrapper!!.json.meta
                        withId("header")
                        "div" {
                            withClass("img")
                            meta.icon?.let {
                                "img" {
                                    withSrc(it)
                                }
                            }
                        }
                        "div" {
                            "p" {
                                withHref(meta.website)
                                "b"("Name")
                            }
                            "p"(meta.name ?: "")
                        }
                        "div" {
                            "p" {"b"("Contributors")}
                            "ul" {
                                for(con in meta.contributors)
                                    "li"(con.name) {
                                        //for contributor colors
                                        withClass("contributor_${con.roles.getOrElse(0) {""}}")
                                    }
                            }
                        }
                        "div" {
                            "p" {"b"("Description")}
                            "p"(meta.description?.joinToString("\n") ?: "")
                        }
                    }
                "table" {
                    "tr" {
                        "td"()
                        "td" {"b"("Name")}
                        "td" {"b"("Contributors")}
                        "td" {"b"("Description")}
                    }

                    for(mod in getMods(all, sorting)) {
                        fPrintln("Writing relation ${mod.name}", TERMC.blue)
                        "tr" {
                            "td" {
                                mod.icon?.let {
                                    "a" {
                                        "img" {
                                            withSrc(it)
                                            withClass("img")
                                        }
                                        withHref(mod.website)
                                    }
                                }
                            }
                            "td" {
                                "a"(mod.name!!) {
                                    //Open in new tab
                                    withRel("noopener noreferrer")
                                    withTarget("_blank")
                                    mod.website?.let {withHref(it)}
                                }
                            }
                            "td" {
                                "ul" {
                                    for(con in mod.contributors)
                                        "li"(con.key) {
                                            //for contributor colors
                                            withClass("contributor_${con.value.getOrElse(0) {""}}")
                                        }
                                }
                            }
                            "td" {
                                for(desc in mod.description ?: arrayOf(""))
                                    "p"(desc)
                                withClass("description")
                            }
                        }
                    }
                }
            }
        }.render()

        writer.write(html)
        writer.close()
        return success("Wrote HTML file")
    }

    private fun getMods(all: Boolean, sorting: Comparator<MetaData>): List<MetaData> =
        MPJH.getModMetas {all || "mod" == it.type}.sortedWith(sorting)

    enum class Format {
        HTML, CSV
    }

    enum class Sorting {
        NAME, DESCRIPTION, AUTHOR
    }
}
