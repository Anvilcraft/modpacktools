package ley.anvil.modpacktools.commands

import ley.anvil.modpacktools.MPJH
import ley.anvil.modpacktools.command.CommandReturn
import ley.anvil.modpacktools.command.CommandReturn.Companion.fail
import ley.anvil.modpacktools.command.CommandReturn.Companion.success
import ley.anvil.modpacktools.command.ICommand
import ley.anvil.modpacktools.command.LoadCommand
import ley.anvil.modpacktools.util.manifest.convertAStoManifest
import ley.anvil.modpacktools.util.zipDir
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
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
        FileUtils.cleanDirectory(tmp)

        val writer = FileWriter(tmp.path + "/manifest.json")
        ml.manifest?.write(writer)
        writer.close()

        //TODO download & install files
        for (uf in ml.links) {
            if (uf.key.isFile) {
                val file = uf.key.getFile("./src/")
                if (file.exists()) {
                    if (uf.value == "internal.override") {
                        if (file.extension == "zip") {
                            //TODO unzip to ./.mpt/twitch/overrides
                        } else if (file.isDirectory) {
                            for (f in file.listFiles()) {
                                val dir = File("./.mpt/twitch/overrides")
                                dir.mkdirs()
                                val target = Paths.get(dir.path + "/" + f.name)
                                Files.copy(f.toPath(), target, StandardCopyOption.REPLACE_EXISTING)
                            }
                        } else {
                            return fail("Only zip files can be used with 'internal.override'")
                        }
                    } else if (uf.value.startsWith("internal.dir")) {
                        val parts = uf.value.split(":")
                        if (parts.size >= 2) {
                            val dir = File("./.mpt/twitch/" + parts[1])
                            dir.mkdirs()
                            val target = Paths.get(dir.path + "/" + file.name)
                            Files.copy(file.toPath(), target, StandardCopyOption.REPLACE_EXISTING)
                        } else {
                            return fail("No directory was given for installer 'internal.dir'")
                        }
                    } else if (uf.value.startsWith("internal.zip")) {
                        if (file.extension == "zip") {
                            val parts = uf.value.split(":")
                            if (parts.size >= 2) {
                                //TODO unzip file to parts[1]
                            } else {
                                return fail("No directory was given for installer 'internal.zip'")
                            }
                        } else {
                            return fail("Only zip files can be used with 'internal.zip'")
                        }
                    } else {
                        return fail("The installer '${uf.value}' is not supportet for Twitch export")
                    }
                }
            } else if (uf.key.isURL) {
                //TODO download and install the file (work for Lord ;) )
            } else {
                return fail("{$uf.key.link} is neither a file nor an URL")
            }
        }

        val zip = ZipOutputStream( FileOutputStream(dir.path + "/$archiveName.zip"))
        zipDir(tmp, "", zip)
        zip.flush()
        zip.close()
        return success("Successfully build twitch zip")
    }

    override val name: String = "buildTwitch"


}