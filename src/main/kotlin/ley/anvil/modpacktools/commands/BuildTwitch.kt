package ley.anvil.modpacktools.commands

import ley.anvil.modpacktools.MPJH
import ley.anvil.modpacktools.command.CommandReturn
import ley.anvil.modpacktools.command.CommandReturn.Companion.success
import ley.anvil.modpacktools.command.ICommand
import ley.anvil.modpacktools.command.LoadCommand
import ley.anvil.modpacktools.util.convertAStoManifest
import ley.anvil.modpacktools.util.zipDir
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.util.zip.ZipOutputStream

@LoadCommand
object BuildTwitch : ICommand {
    override fun execute(args: Array<out String>): CommandReturn {
        val wr = MPJH.asWrapper!!
        val ml = convertAStoManifest(wr)
        val archiveName = wr.json.id + "-" + wr.defaultVersion.versionName + "-twitch"
        val dir = File("./build")
        val tmp = File("./.mpt/twitch")
        dir.mkdirs()
        tmp.mkdirs()

        val writer = FileWriter(tmp.path + "/manifest.json")
        ml.manifest?.write(writer)
        writer.close()

        //TODO download & install files


        val zip = ZipOutputStream( FileOutputStream(dir.path + "/$archiveName.zip"))
        zipDir(tmp, "", zip)
        zip.flush()
        zip.close()
        return success()
    }

    override val name: String = "buildTwitch"


}