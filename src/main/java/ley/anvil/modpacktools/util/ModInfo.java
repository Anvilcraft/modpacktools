package ley.anvil.modpacktools.util;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import ley.anvil.modpacktools.Commands;
import ley.anvil.modpacktools.Main;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class ModInfo {

    private String name;
    private JsonArray authors;
    @SerializedName("websiteUrl")
    private String link;
    @SerializedName("downloadCount")
    private int downloads;
    private int id;

    private ModInfo(String name, JsonArray authors, String link, int downloads, int id) {
        this.name = name;
        this.authors = authors;
        this.link = link;
        this.downloads = downloads;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String[] getAuthors() {
        ArrayList<String> authorArr = new ArrayList<>();
        for(JsonElement author : authors) {
            JsonObject authorObj = (JsonObject) author;
            authorArr.add(authorObj.get("name").getAsString());
        }
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

    public static ArrayList<ModInfo> getModInfo() {
        try {
            System.out.println("Getting Info From Curse API");
            File manifestFile = new File(Main.CONFIG.JAR_LOCATION, Main.CONFIG.CONFIG.get("manifestFile").getAsString());
            //Read manifest
            JsonObject manifest = Util.readJsonFile(manifestFile);
            JsonArray files = manifest.getAsJsonArray("files");

            ArrayList<Integer> fileIds = new ArrayList<>();
            for(JsonElement file : files) {
                fileIds.add(((JsonObject) file).get("projectID").getAsInt());
            }
            String responseStr = Util.httpPostString(new URL("https://addons-ecs.forgesvc.net/api/v2/addon"),
                    fileIds.toString(),
                    "application/json; utf-8",
                    "application/json");
            JsonArray response = (JsonArray) JsonParser.parseString(responseStr);

            ArrayList<ModInfo> modInfos = new ArrayList<>();
            Gson gson = new Gson();
            for(JsonElement mod : response) {
                modInfos.add(gson.fromJson(mod, ModInfo.class));
            }
            return modInfos;
        } catch(MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
