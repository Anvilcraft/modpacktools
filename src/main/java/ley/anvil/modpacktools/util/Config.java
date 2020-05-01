package ley.anvil.modpacktools.util;

import com.google.gson.JsonObject;

import java.io.*;
import java.net.URISyntaxException;

public class Config {
	private final String CONFIG_NAME = "modpacktoolsconfig.json";
	public final File JAR_LOCATION;
	public final File CONFIG_LOCATION;
	public final JsonObject CONFIG;
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
		} catch(URISyntaxException e) {
			JAR_LOCATION1 = null;
			e.printStackTrace();
		}
		JAR_LOCATION = JAR_LOCATION1;
		CONFIG_LOCATION  = new File(JAR_LOCATION, CONFIG_NAME);
		CONFIG = readConfig();
	}

	/**
	 * reads the config it it exists and otherwise copies it
	 * @return the json object of the config file
	 */
	private JsonObject readConfig() {
		if(CONFIG_LOCATION.exists()) {
			//parse file to json
			return Util.readJsonFile(CONFIG_LOCATION);
		}else {
			//copy from resources
			try {
				InputStream in = ClassLoader.getSystemResourceAsStream(CONFIG_NAME);
				byte[] buff = new byte[in.available()];
				in.read(buff);
				OutputStream out = new FileOutputStream(CONFIG_LOCATION);
				out.write(buff);
				in.close();
				out.close();
				return readConfig();
			}catch(IOException e) {
				e.printStackTrace();
			}
		}return null;
	}
}
