package ley.anvil.modpacktools.commandhelper;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import ley.anvil.modpacktools.Main;
import ley.anvil.modpacktools.util.Util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

public class ModInfo {

    private final String name;
    private final JsonArray authors;
    @SerializedName("websiteUrl")
    private final String link;
    @SerializedName("downloadCount")
    private final int downloads;
    private final int id;
    private final JsonArray latestFiles;

    private ModInfo(String name, JsonArray authors, String link, int downloads, int id, JsonArray latestFiles) {
        this.name = name;
        this.authors = authors;
        this.link = link;
        this.downloads = downloads;
        this.id = id;
        this.latestFiles = latestFiles;
    }

    public static ArrayList<ModInfo> getModInfo() {
        try {
            System.out.println("Getting Info From Curse API");
            File manifestFile = new File(Main.getCONFIG().getJarLocation(), Main.getCONFIG().getConfig()
                .getPath(
                    "Locations",
                    "manifestFile"
                ));
            //Read manifest
            JsonObject manifest = Util.readAsJson(manifestFile);
            JsonArray files = manifest.getAsJsonArray("files");

            ArrayList<Integer> fileIds = new ArrayList<>();
            files.forEach(file -> fileIds.add(((JsonObject)file).get("projectID").getAsInt()));
            String responseStr = Util.httpPostStr(new URL("https://addons-ecs.forgesvc.net/api/v2/addon"),
                fileIds.toString(),
                "application/json; charset=utf-8",
                Collections.singletonMap("Accept", "application/json")
            );
            JsonArray response = (JsonArray)JsonParser.parseString(responseStr);

            ArrayList<ModInfo> modInfos = new ArrayList<>();
            Gson gson = new Gson();
            response.forEach(mod -> modInfos.add(gson.fromJson(mod, ModInfo.class)));
            return modInfos;
        }catch(IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public String[] getAuthors() {
        ArrayList<String> authorArr = new ArrayList<>();
        authors.forEach(author -> {
            authorArr.add(((JsonObject)author).get("name").getAsString());
        });
        return authorArr.toArray(new String[authorArr.size()]);
    }

    public String getLink() {
        return link;
    }

    public int getDownloads() {
        return downloads;
    }

    public int getId() {
        return id;
    }

    public String getDownload() {
        return latestFiles.get(0).getAsJsonObject().get("downloadUrl").getAsString();
    }

}
