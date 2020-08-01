package ley.anvil.modpacktools.commands

import ley.anvil.addonscript.v1.AddonscriptJSON
import ley.anvil.modpacktools.CONFIG
import ley.anvil.modpacktools.command.CommandReturn
import ley.anvil.modpacktools.command.CommandReturn.Companion.success
import ley.anvil.modpacktools.command.ICommand
import ley.anvil.modpacktools.command.LoadCommand
import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.inf.ArgumentParser
import net.sourceforge.argparse4j.inf.Namespace
import java.io.File
import java.io.FileWriter

@LoadCommand
object Init : ICommand {
    override val name: String = "init"
    override val helpMessage: String = "initializes the MPT dev environment (currently only creates config file)"
    override val parser: ArgumentParser = ArgumentParsers.newFor("Init")
        .build()
        .description(helpMessage)

    override val needsConfig: Boolean = false
    override val needsModpackjson: Boolean = false

    override fun execute(args: Namespace): CommandReturn {
        if(!CONFIG.exists)
            CONFIG.copyConfig()

        val srcDir = File(CONFIG.config.pathOrException<String>("Locations/src"))
        val overrides = File(srcDir, "overrides")

        if(!overrides.exists())
            overrides.mkdirs()

        val asJson = File(srcDir, "modpack.json")

        if (!asJson.exists()) {
            //create new file
            val writer = FileWriter(asJson)
            val addsc = AddonscriptJSON.create()

            //set type and add version
            addsc.type = "modpack"
            val ver = AddonscriptJSON.Version()
            addsc.versions = mutableListOf(ver)
            ver.versionid = -1

            //create overrides
            val file = AddonscriptJSON.File()
            ver.files = mutableListOf(file)

            file.id = "overrides"
            file.link = "file://overrides"
            file.installer = "internal.overrides"
            file.options = mutableListOf("client", "server", "required", "included")

            //write file
            addsc.write(writer)
            writer.close()
        }
        return success("MPT dev environment created. Use the created modpack.json or use the import command to import a curse manifest (recommended)")
    }
}