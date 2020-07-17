package ley.anvil.modpacktools.commands;

import ley.anvil.modpacktools.Main;
import ley.anvil.modpacktools.command.CommandReturn;
import ley.anvil.modpacktools.command.ICommand;
import ley.anvil.modpacktools.command.LoadCommand;
import ley.anvil.modpacktools.commandhelper.ModInfo;
import ley.anvil.modpacktools.util.FileDownloader;
import ley.anvil.modpacktools.util.Util;

import javax.annotation.Nonnull;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
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
                ModInfo.getModInfo()
                        .stream()
                        .collect(Collectors.toMap(
                                i -> Util.sanitizeURL(toURL.apply(i.getDownload())),
                                i -> new File(args[1], Paths.get(toURL.apply(i.getDownload()).getPath()).getFileName().toString())
                        )),
                Main.CONFIG.CONFIG.getPath(Long.class, "Downloads/maxThreads").intValue(),
                r -> {
                    System.out.println(r.getResponseCode() + " " + r.getResponseMessage() + " " + r.getUrl() + " " + r.getFile());
                    if(r.getException() != null)
                        System.out.println(r.getException().getMessage());
                },
                1,
                2,
                TimeUnit.MINUTES,
                Main.CONFIG.CONFIG.getPath(Long.class, "Downloads/httpTimeout").intValue(),
                Arrays.stream(args).anyMatch("force"::equals) ? FileDownloader.AsyncDownloader.ExistingFileBehaviour.OVERWRITE : FileDownloader.AsyncDownloader.ExistingFileBehaviour.SKIP
        );
        return CommandReturn.success();
    }

    @Nonnull
    @Override
    public String getName() {
        return "downloadmods";
    }

    @Override
    public boolean needsConfig() {
        return true;
    }

    @Nonnull
    @Override
    public String getHelpMessage() {
        return "Downloads all mods. force always downloads files even if they are already presentSyntax: <OutDir> [force]";
    }
}
