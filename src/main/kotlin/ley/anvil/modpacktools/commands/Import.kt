package ley.anvil.modpacktools.commands

import com.google.gson.stream.JsonReader
import ley.anvil.addonscript.curse.ManifestJSON
import ley.anvil.modpacktools.Main.GSON
import ley.anvil.modpacktools.Main.MPJH
import ley.anvil.modpacktools.command.CommandReturn
import ley.anvil.modpacktools.command.CommandReturn.fail
import ley.anvil.modpacktools.command.CommandReturn.success
import ley.anvil.modpacktools.command.ICommand
import ley.anvil.modpacktools.command.LoadCommand
import java.io.File
import java.io.FileReader
import java.io.FileWriter

@LoadCommand
class Import : ICommand {
    override fun getName(): String = "import"
    override fun getHelpMessage(): String = "Converts a given manifest file to a modpackjson file"

    override fun execute(args: Array<out String>): CommandReturn {
        if(!args.checkArgs())
            return fail("Invalid Args")

        val outFile = MPJH.file
        val manifest = File(args[1])

        if(!manifest.exists() || outFile.exists())
            return fail("$manifest not found or $outFile already exists.")

        println("Converting...")
        val mpjWriter = FileWriter(MPJH.file)
        GSON.fromJson<ManifestJSON>(JsonReader(FileReader(manifest)), ManifestJSON::class.java).toAS().write(mpjWriter)
        mpjWriter.close()
        return success("Converted sucessfully")
    }

    private fun Array<out String>.checkArgs(): Boolean = this.size == 2
}