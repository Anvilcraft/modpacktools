package ley.anvil.modpacktools.util

import org.apache.commons.io.FileUtils
import java.io.File

class Config(val configName: String) {
    val jarLocation by lazy {
        //Get the Location of the jarfile
        var file = File(
            this::class.java
                .protectionDomain
                .codeSource
                .location
                .toURI()
        )
        //Ensure That JAR_LOCATION is the jarfile's directory and not the file itself
        if(file.isFile)
            file = file.parentFile
        file
    }
    val configLocation by lazy {File(jarLocation, configName)}
    val config by lazy {readConfig()}

    /**
     * reads the config it it exists and the default config otherwise
     *
     * @return the Toml object of the config file
     */
    private fun readConfig(): CustomToml {
        return if(configExists()) {
            //parse file to toml
            CustomToml().read(configLocation) as CustomToml
            //reads config from resources if no config file exists as a default value. commands that require the config still won't run without it
        } else CustomToml().read(ClassLoader.getSystemResourceAsStream(configName)) as CustomToml
    }

    /**
     * Checks if the config file exists
     *
     * @return true if the config file exists
     */
    fun configExists(): Boolean = configLocation.exists()

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