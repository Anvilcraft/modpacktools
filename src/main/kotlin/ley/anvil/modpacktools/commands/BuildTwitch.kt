package ley.anvil.modpacktools.commands

import ley.anvil.modpacktools.CONFIG
import ley.anvil.modpacktools.MPJH
import ley.anvil.modpacktools.command.CommandReturn
import ley.anvil.modpacktools.command.CommandReturn.Companion.fail
import ley.anvil.modpacktools.command.CommandReturn.Companion.success
import ley.anvil.modpacktools.command.ICommand
import ley.anvil.modpacktools.command.LoadCommand
import ley.anvil.modpacktools.util.manifest.convertAStoManifest
import ley.anvil.modpacktools.util.mergeTo
import ley.anvil.modpacktools.util.toZip
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.util.zip.ZipOutputStream

@LoadCommand
object BuildTwitch : ICommand {
    override val name: String = "buildtwitch"
    override val helpMessage: String = "builds a twitch export"
    private val tmp: File by lazy {File(CONFIG.config.getPath<String>("Locations/tempDir"), "twitch")}

    override fun execute(args: Array<out String>): CommandReturn {
        val wr = MPJH.asWrapper!!
        val ml = convertAStoManifest(wr)
        val archiveName = "${wr.json.id}-${wr.defaultVersion.versionName}-twitch"
        val dir = File("./build")
        dir.mkdirs()
        tmp.mkdirs()
        FileUtils.cleanDirectory(tmp)

        val writer = FileWriter(tmp.path + "/manifest.json")
        ml.manifest?.write(writer)
        writer.close()

        //TODO download & install files
        for(uf in ml.links) {
            if(uf.key.isFile) {
                val file = uf.key.getFile(CONFIG.config.getPath<String>("Locations/src")!!)
                if(file.exists()) {
                    when {
                        uf.value == "internal.override" -> {
                            when {
                                file.extension == "zip" -> {
                                    TODO("unzip to ./.mpt/twitch/overrides")
                                }

                                file.isDirectory -> {
                                    FileUtils.copyDirectory(file, File(tmp, "overrides"))
                                    //file.listFiles()!!.forEach {it.copyTo("overrides")}
                                }

                                else -> {
                                    return fail("Only zip files can be used with 'internal.override'")
                                }
                            }
                        }

                        uf.value.startsWith("internal.dir") -> {
                            val parts = uf.value.split(":")
                            if(parts.size >= 2) {
                                file.copyTo(File(tmp, parts[1]))
                            } else {
                                return fail("No directory was given for installer 'internal.dir'")
                            }
                        }

                        uf.value.startsWith("internal.zip") -> {
                            if(file.extension == "zip") {
                                val parts = uf.value.split(":")
                                if(parts.size >= 2) {
                                    TODO("unzip file to parts[1]")
                                } else {
                                    return fail("No directory was given for installer 'internal.zip'")
                                }
                            } else {
                                return fail("Only zip files can be used with 'internal.zip'")
                            }
                        }

                        else -> {
                            return fail("The installer '${uf.value}' is not supported for Twitch export")
                        }
                    }
                }
            } else if(uf.key.isURL) {
                TODO("download and install the file (work for Lord ;) )")
            } else {
                return fail("{$uf.key.link} is neither a file nor an URL")
            }
        }

        val zip = ZipOutputStream(FileOutputStream(dir.path + "/$archiveName.zip"))
        tmp.toZip(zip)
        zip.flush()
        zip.close()
        return success("Successfully build twitch zip")
    }

    private fun File.copyTo(dir: File) {
        FileUtils.copyFile(this, tmp.mergeTo(dir).apply {mkdirs()}.mergeTo(this))
    }
}