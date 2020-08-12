package ley.anvil.modpacktools.util.config

import org.apache.commons.io.FileUtils
import java.io.File

class Config(val configName: String) {
    val configLocation = File(configName)
    val config = readConfig()

    /**
     * reads the config it it exists and the default config otherwise
     *
     * @return the Toml object of the config file
     */
    private fun readConfig(): ConfigToml {
        return if(exists) {
            //parse file to toml
            ConfigToml().read(configLocation) as ConfigToml
            //reads config from resources if no config file exists as a default value. commands that require the config still won't run without it
        } else ConfigToml().read(ClassLoader.getSystemResourceAsStream(configName)) as ConfigToml
    }

    /**
     * Checks if the config file exists
     *
     * @return true if the config file exists
     */
    val exists: Boolean get() = configLocation.exists()

    /**
     * Copies the Config file from the resources into the tool's folder
     */
    fun copyConfig() {
        //copy from resources
        val conf = ClassLoader.getSystemResourceAsStream(configName)
        FileUtils.copyInputStreamToFile(conf, configLocation)
        conf!!.close()
    }
}
