package ley.anvil.modpacktools;

import ley.anvil.modpacktools.util.Config;

public class Main {
	public static final Config CONFIG = new Config();

	public static void main(String[] args) {
	    if(args.length == 0) {
	        Commands.help();
	        return;
        }
		switch(args[0].toLowerCase() /* ignores case of commands */) {
			case "help":
			default:
				Commands.help();
				break;
            case "init":
                Commands.init();
                break;
            case "addmod":
                Commands.addMod(args);
                break;
            case "buildtwitch":
                Commands.buildTwitch();
                break;
            case "buildmodpackjson":
                Commands.buildModpackJSON();
                break;
            case "buildraw":
                Commands.buildRaw();
                break;
            case "buildserver":
                Commands.buildServer(args);
                break;
            case "downloadmods":
                Commands.downloadMods(args);
                break;
            case "createmodlist":
                Commands.createModlist(args);
                break;
            case "makeserver":
                Commands.makeServer(args);
                break;
		}
	}

}
