package ley.anvil.modpacktools.commands

import ley.anvil.modpacktools.CONFIG
import ley.anvil.modpacktools.MPJH
import ley.anvil.modpacktools.TERMC
import ley.anvil.modpacktools.command.CommandReturn
import ley.anvil.modpacktools.command.CommandReturn.Companion.fail
import ley.anvil.modpacktools.command.CommandReturn.Companion.success
import ley.anvil.modpacktools.command.ICommand
import ley.anvil.modpacktools.command.LoadCommand
import ley.anvil.modpacktools.util.downloadFiles
import ley.anvil.modpacktools.util.fPrintln
import ley.anvil.modpacktools.util.manifest.convertAStoManifest
import ley.anvil.modpacktools.util.mergeTo
import ley.anvil.modpacktools.util.toZip
import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.impl.Arguments.storeTrue
import net.sourceforge.argparse4j.inf.ArgumentParser
import net.sourceforge.argparse4j.inf.Namespace
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.net.URL
import java.util.zip.ZipOutputStream

@LoadCommand
object BuildTwitch : ICommand {
    override val name: String = "buildtwitch"
    override val helpMessage: String = "builds a twitch export"
    override val parser: ArgumentParser = ArgumentParsers.newFor("BuildTwitch")
        .build()
        .description(helpMessage)
        .apply {
            addArgument("-a", "--all")
                .help("Downloads all relations instead of just required ones")
                .action(storeTrue())
        }

    private val tempDir by lazy {File(CONFIG.config.pathOrException<String>("Locations/tempDir"))}
    private val tmp: File by lazy {File(tempDir, "twitch")}
    private val downloadDir by lazy {File(tempDir, "download")}

    override fun execute(args: Namespace): CommandReturn {
        val wr = MPJH.asWrapper!!
        val ml = convertAStoManifest(wr) {args.getBoolean("all") || "required" in it.options}
        val archiveName = "${wr.json.id}-${wr.defaultVersion.versionName}-twitch"
        val dir = File("./build")
        val toDownload = mutableMapOf<URL, Pair<File, String>>()
        val srcDir by lazy {File(CONFIG.config.pathOrException<String>("Locations/src"))}
        dir.mkdirs()
        tmp.mkdirs()
        downloadDir.mkdirs()
        FileUtils.cleanDirectory(tmp)
        FileUtils.cleanDirectory(downloadDir)

        val writer = FileWriter(tmp.path + "/manifest.json")
        ml.manifest?.write(writer)
        writer.close()

        for(uf in ml.links) {
            if(uf.key.isFile) {
                if(!uf.key.isASDirSet)
                    uf.key.setASDir(srcDir)
                val file = uf.key.file
                if(file.exists()) {
                    installFile(uf.value, file).apply {println(this)}
                }
            } else if(uf.key.isURL) {
                val filePath = URL(uf.key.link)
                toDownload[filePath] = Pair(File(downloadDir, FilenameUtils.getName(filePath.toString())), uf.value)
            } else {
                return fail("{$uf.key.link} is neither a file nor an URL")
            }
        }

        downloadFiles(
            toDownload.mapValues {it.value.first},
            {
                fPrintln("downloaded file ${it.file}", TERMC.brightBlue)
                fPrintln(installFile(toDownload[it.url]!!.second, it.file), TERMC.green)
            },
            false
        )

        fPrintln("Creating zip", TERMC.brightGreen)
        val zip = ZipOutputStream(FileOutputStream("${dir.path}/$archiveName.zip"))
        tmp.toZip(zip)
        zip.flush()
        zip.close()
        return success("Successfully built twitch zip")
    }

    private fun installFile(installer: String, file: File): String {
        when {
            installer == "internal.override" -> {
                when {
                    file.extension == "zip" -> {
                        TODO("unzip to ./.mpt/twitch/overrides")
                    }

                    file.isDirectory -> {
                        FileUtils.copyDirectory(file, File(tmp, "overrides"))
                    }

                    else -> {
                        return "Only zip files can be used with 'internal.override'"
                    }
                }
            }

            installer.startsWith("internal.dir") -> {
                val parts = installer.split(":")
                if(parts.size >= 2) {
                    FileUtils.copyFile(file, File(File(tmp, "overrides"), parts[1]) mergeTo file)
                } else {
                    return "No directory was given for installer 'internal.dir'"
                }
            }

            installer.startsWith("internal.zip") -> {
                TODO()
            }

            else -> {
                return "The installer '$installer' is not supported for Twitch export"
            }
        }
        return "installed $file"
    }
}
