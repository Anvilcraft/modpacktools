package ley.anvil.modpacktools.util;

import java.io.*;
import java.net.URISyntaxException;

//TODO add config integrity check to make sure each key is present (and maybe of correct type?)
public class Config {
    public final File JAR_LOCATION;
    public final File CONFIG_LOCATION;
    private final String CONFIG_NAME = "modpacktoolsconfig.toml";
    public CustomToml CONFIG;

    public Config() {
        //Get the Location of the jarfile
        File JAR_LOCATION1;
        try {
            JAR_LOCATION1 = new File(this
                    .getClass()
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI());
            //Ensure That JAR_LOCATION is the jarfiles directory and not the file itself
            if(JAR_LOCATION1.isFile()) {
                JAR_LOCATION1 = JAR_LOCATION1.getParentFile();
            }
        }catch(URISyntaxException e) {
            JAR_LOCATION1 = null;
            e.printStackTrace();
        }
        JAR_LOCATION = JAR_LOCATION1;
        CONFIG_LOCATION = new File(JAR_LOCATION, CONFIG_NAME);
        CONFIG = readConfig();
    }

    /**
     * reads the config it it exists
     *
     * @return the Toml object of the config file
     */
    private CustomToml readConfig() {
        if(configExists()) {
            //parse file to toml
            return (CustomToml)new CustomToml().read(CONFIG_LOCATION);
        }
        return null;
    }

    /**
     * Checks if the config file exists
     *
     * @return true if the config file exists
     */
    public boolean configExists() {
        return CONFIG_LOCATION.exists();
    }

    /**
     * Copes the Config file from the resources into the tool's folder
     */
    public void copyConfig() {
        //copy from resources
        try {
            InputStream in = ClassLoader.getSystemResourceAsStream(CONFIG_NAME);
            byte[] buff = new byte[in.available()];
            in.read(buff);
            OutputStream out = new FileOutputStream(CONFIG_LOCATION);
            out.write(buff);
            in.close();
            out.close();
            CONFIG = readConfig();
        }catch(IOException e) {
            e.printStackTrace();
        }
    }
}
