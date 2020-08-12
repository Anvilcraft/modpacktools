package ley.anvil.modpacktools.util.addonscript

import ley.anvil.modpacktools.TERMC
import ley.anvil.modpacktools.util.fPrintln
import ley.anvil.modpacktools.util.mergeTo
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.PrintStream

data class InstallFileSuccess(
    val success: Boolean,
    val msg: String? = null
) {
    @JvmOverloads
    fun printf(out: PrintStream = System.out) {
        if(msg != null)
            out.fPrintln(msg, if(success) TERMC.green else TERMC.red)
    }
}

fun installFile(installer: String, file: File, outDir: File): InstallFileSuccess {
    when {
        installer == "internal.override" -> {
            when {
                file.extension == "zip" -> {
                    TODO("unzip to ./.mpt/twitch/overrides")
                }

                file.isDirectory -> {
                    FileUtils.copyDirectory(file, outDir)
                }

                else -> {
                    InstallFileSuccess(false, "Only zip files can be used with \'internal.override\'")
                }
            }
        }

        installer.startsWith("internal.dir") -> {
            val (_, dir) = installer.split(":")
            FileUtils.copyFile(file, File(outDir, dir) mergeTo file)
        }

        installer.startsWith("internal.zip") -> {
            TODO()
        }

        else -> {
            return InstallFileSuccess(false, "The installer \'$installer\' is not supported")
        }
    }
    return InstallFileSuccess(true, "installed $file")
}
