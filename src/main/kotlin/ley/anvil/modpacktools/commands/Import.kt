package ley.anvil.modpacktools.commands

import com.google.gson.stream.JsonReader
import ley.anvil.addonscript.curse.ManifestJSON
import ley.anvil.modpacktools.GSON
import ley.anvil.modpacktools.MPJH
import ley.anvil.modpacktools.command.CommandReturn
import ley.anvil.modpacktools.command.CommandReturn.Companion.fail
import ley.anvil.modpacktools.command.CommandReturn.Companion.success
import ley.anvil.modpacktools.command.ICommand
import ley.anvil.modpacktools.command.LoadCommand
import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.impl.type.FileArgumentType
import net.sourceforge.argparse4j.inf.ArgumentParser
import net.sourceforge.argparse4j.inf.Namespace
import java.io.File
import java.io.FileReader
import java.io.FileWriter

@LoadCommand
object Import : ICommand {
    override val name: String = "import"
    override val helpMessage: String = "Converts a given manifest file to a modpackjson file"
    override val parser: ArgumentParser = ArgumentParsers.newFor("Import")
        .build()
        .apply {
            addArgument("manifest")
                .help("the manifest file to import")
                .type(FileArgumentType().verifyIsFile())
        }

    override val needsModpackjson: Boolean = false
    override val needsConfig: Boolean = false

    override fun execute(args: Namespace): CommandReturn {
        val outFile = MPJH.modpackJsonFile
        val manifest = args.get<File>("manifest")

        if(!manifest.exists() || outFile.exists())
            return fail("$manifest not found or $outFile already exists.")

        println("Converting...")
        MPJH.modpackJsonFile.parentFile.mkdirs()
        val mpjWriter = FileWriter(MPJH.modpackJsonFile)
        GSON.fromJson<ManifestJSON>(JsonReader(FileReader(manifest)), ManifestJSON::class.java).toAS().write(mpjWriter)
        mpjWriter.close()
        return success("Converted sucessfully")
    }
}
