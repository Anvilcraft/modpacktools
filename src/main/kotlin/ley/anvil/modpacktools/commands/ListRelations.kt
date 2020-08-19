package ley.anvil.modpacktools.commands

import com.jakewharton.fliptables.FlipTable
import ley.anvil.modpacktools.MPJH
import ley.anvil.modpacktools.command.AbstractCommand
import ley.anvil.modpacktools.command.CommandReturn
import ley.anvil.modpacktools.command.CommandReturn.Companion.success
import ley.anvil.modpacktools.command.LoadCommand
import ley.anvil.modpacktools.util.arg
import net.sourceforge.argparse4j.impl.Arguments.storeTrue
import net.sourceforge.argparse4j.inf.ArgumentParser
import net.sourceforge.argparse4j.inf.Namespace

@LoadCommand
object ListRelations : AbstractCommand("ListRelations") {
    override val helpMessage: String = "Lists the relations of this mod pack"

    override val parser: ArgumentParser by argParser {
        arg("-c", "--csv") {
            help("Doesn't format as a table but instead as csv (separated by ;)")
            action(storeTrue())
        }

        arg("-n", "--nolimit") {
            help("does not limit the size of the authors list")
            action(storeTrue())
        }

        arg("-d", "--description") {
            help("adds the description of relations to the list")
            action(storeTrue())
        }
    }

    override fun execute(args: Namespace): CommandReturn {
        val metas = MPJH.getModMetas()
            .sortedWith {a, b -> b.name?.let {a.name?.compareTo(it, true)} ?: 0}

        if(args.getBoolean("csv")) {
            metas.forEach {
                println(
                    "${it.name};${it.contributors.keys.joinToString()};${it.website}".let {s ->
                        if(args.getBoolean("description"))
                            s + ";${it.description?.joinToString() ?: ""}"
                        else s
                    }
                )
            }
        } else {
            val data = mutableListOf<Array<out String>>()

            metas.forEach {
                data.add(
                    run {
                        val row = mutableListOf(
                            it.name ?: "",
                            it.contributors.keys.joinToString().let {s ->
                                //Limit size
                                if(args.getBoolean("nolimit") || s.length < 50)
                                    s
                                else
                                    s.substring(0..47) + "..."
                            },
                            it.website ?: ""
                        )

                        if(args.getBoolean("description"))
                            row.add(it.description?.joinToString(" ") ?: "")

                        row.toTypedArray()
                    }
                )
            }

            println(
                FlipTable.of(
                    run {
                        val header = mutableListOf(
                            "Name",
                            "Contributors",
                            "Website"
                        )

                        if(args.getBoolean("description"))
                            header.add("Description")

                        header.toTypedArray()
                    },
                    data.toTypedArray()
                )
            )
        }
        return success()
    }
}
