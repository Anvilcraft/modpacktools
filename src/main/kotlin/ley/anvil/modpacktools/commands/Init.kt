package ley.anvil.modpacktools.commands

import ley.anvil.addonscript.v1.AddonscriptJSON
import ley.anvil.modpacktools.CONFIG
import ley.anvil.modpacktools.command.CommandReturn
import ley.anvil.modpacktools.command.CommandReturn.Companion.success
import ley.anvil.modpacktools.command.ICommand
import ley.anvil.modpacktools.command.LoadCommand
import ley.anvil.modpacktools.util.mergeTo
import java.io.File
import java.io.FileWriter

@LoadCommand
object Init : ICommand {
    override val name: String = "init"
    override val helpMessage: String = "initializes the MPT dev environment (currently only creates config file)"
    override val needsConfig: Boolean = false
    override val needsModpackjson: Boolean = false

    override fun execute(args: Array<out String>): CommandReturn {
        if(!CONFIG.exists)
            CONFIG.copyConfig()
        val srcDir = File(CONFIG.config.getPath<String>("Locations/src")!!)
        val overrides = srcDir.mergeTo(File("overrides"))
        if(!overrides.exists())
            overrides.mkdirs()
        val asjson = srcDir.mergeTo(File("modpack.json"))
        if (!asjson.exists()) {
            val writer = FileWriter(asjson)
            val addsc = AddonscriptJSON.create()
            addsc.type = "modpack"
            val ver = AddonscriptJSON.Version()
            addsc.versions = mutableListOf(ver)
            ver.versionid = -1
            val file = AddonscriptJSON.File()
            ver.files = mutableListOf(file)
            file.id = "overrides"
            file.link = "file://overrides"
            file.installer = "internal.overrides"
            file.options = mutableListOf("client", "server", "required", "included")
            addsc.write(writer)
            writer.close()
        }
        return success("MPT dev environment created. Use the created modpack.json or use the import command to import a curse manifest (recommended)")
    }
}