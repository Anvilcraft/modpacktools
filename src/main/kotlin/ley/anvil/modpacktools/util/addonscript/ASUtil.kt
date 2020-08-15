package ley.anvil.modpacktools.util.addonscript

import ley.anvil.modpacktools.TERMC
import ley.anvil.modpacktools.util.fPrintln
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.PrintStream

/**
 * This will be returned by [installFile]
 *
 * @param success if the file was installer successfully
 * @param msg an optional message returned by [installFile]
 */
data class InstallFileSuccess(
    val success: Boolean,
    val msg: String? = null
) {
    /**
     * Prints a message of this to the [out] [PrintStream]
     *
     * @param out the [PrintStream] to print the message to or [System.out] if not specified
     */
    @JvmOverloads
    fun printf(out: PrintStream = System.out) {
        if(msg != null)
            out.fPrintln(msg, if(success) TERMC.green else TERMC.red)
    }
}

/**
 * This will install a file given an addonscript installer
 *
 * @param installer the installer to use
 * @param file the file to install
 * @param outDir the directory to install the [file] to
 */
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
            FileUtils.copyFile(file, File(outDir, dir).resolve(file))
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
