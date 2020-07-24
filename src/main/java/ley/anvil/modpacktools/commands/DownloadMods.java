package ley.anvil.modpacktools.commands;

import ley.anvil.modpacktools.Main;
import ley.anvil.modpacktools.command.CommandReturn;
import ley.anvil.modpacktools.command.ICommand;
import ley.anvil.modpacktools.command.LoadCommand;
import ley.anvil.modpacktools.util.FileDownloader;
import ley.anvil.modpacktools.util.Util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

//TODO due to tilera still not having a proper implementation to get mod info in the addonscript library, this always downloads the most recent version of mods!
@LoadCommand
public class DownloadMods implements ICommand {
    @Override
    public CommandReturn execute(String[] args) {
        if(args.length < 2)
            return CommandReturn.fail("Invalid args");

        Function<String, URL> toURL = spec -> {
            try {
                return new URL(spec);
            }catch(MalformedURLException ignored) {
            }
            return null;
        };

        FileDownloader.downloadAsync(
                Main.MPJH.getJson().getDefaultVersion().getRelLinks(Main.MPJH.getJson().indexes, "client", false, "internal.dir:mods", null).stream()
                        .collect(Collectors.toMap(
                                i -> Util.sanitizeURL(toURL.apply(i.getKey())),
                                i -> new File(args[1], Paths.get(toURL.apply(i.getKey()).getPath()).getFileName().toString())
                        )),
                r -> {
                    //Synced to prevent the exception being printed too late
                    synchronized(this) {
                        System.out.println(r.getResponseCode() + " " + r.getResponseMessage() + " " + r.getUrl() + " " + r.getFile());
                        if(r.getException() != null)
                            System.out.println(r.getException().getMessage());
                    }
                },
                Arrays.asList(args).contains("force") ? FileDownloader.AsyncDownloader.ExistingFileBehaviour.OVERWRITE : FileDownloader.AsyncDownloader.ExistingFileBehaviour.SKIP
        );
        return CommandReturn.success();
    }

    @Override
    public String getName() {
        return "downloadmods";
    }

    @Override
    public boolean needsConfig() {
        return true;
    }

    @Override
    public String getHelpMessage() {
        return "Downloads all mods. force always downloads files even if they are already present Syntax: <OutDir> [force]";
    }
}
