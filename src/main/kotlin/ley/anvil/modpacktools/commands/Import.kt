package ley.anvil.modpacktools.commands

import com.google.gson.stream.JsonReader
import ley.anvil.addonscript.curse.ManifestJSON
import ley.anvil.modpacktools.Main.GSON
import ley.anvil.modpacktools.Main.MPJH
import ley.anvil.modpacktools.command.CommandReturn
import ley.anvil.modpacktools.command.CommandReturn.Companion.fail
import ley.anvil.modpacktools.command.CommandReturn.Companion.success
import ley.anvil.modpacktools.command.ICommand
import ley.anvil.modpacktools.command.LoadCommand
import java.io.File
import java.io.FileReader
import java.io.FileWriter

@LoadCommand
object Import : ICommand {
    override val name: String = "import"
    override val helpMessage: String = "Converts a given manifest file to a modpackjson file"
    override val needsModpackjson: Boolean = false
    override val needsConfig: Boolean = false

    override fun execute(args: Array<out String>): CommandReturn {
        if(!args.checkArgs())
            return fail("Invalid Args")

        val outFile = MPJH.modpackJsonFile
        val manifest = File(args[1])

        if(!manifest.exists() || outFile.exists())
            return fail("$manifest not found or $outFile already exists.")

        println("Converting...")
        MPJH.modpackJsonFile.parentFile.mkdirs()
        val mpjWriter = FileWriter(MPJH.modpackJsonFile)
        GSON.fromJson<ManifestJSON>(JsonReader(FileReader(manifest)), ManifestJSON::class.java).toAS().write(mpjWriter)
        mpjWriter.close()
        return success("Converted sucessfully")
    }

    private fun Array<out String>.checkArgs(): Boolean = this.size == 2
}