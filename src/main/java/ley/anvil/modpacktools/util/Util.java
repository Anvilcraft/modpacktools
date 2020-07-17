package ley.anvil.modpacktools.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ley.anvil.modpacktools.Main;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class Util {
    /**
     * Reads a Json File
     *
     * @param file the file to read
     * @return the file content as JsonObject
     */
    public static JsonObject readJsonFile(File file) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String inputLine;
            StringBuffer sb = new StringBuffer();
            while((inputLine = br.readLine()) != null) {
                sb.append(inputLine);
            }
            br.close();
            return (JsonObject)JsonParser.parseString(sb.toString());
        }catch(IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * sends a http post request
     *
     * @param url the url to send the request to
     * @param contentType what content type should be used. Example: "application/json; utf-8"
     * @param accept what content is accepted. Example: "application/json"
     * @param payload the payload to send
     * @return the response as string
     */
    public static String httpPostString(URL url, String payload, String contentType, String accept) {
        try {
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", contentType);
            con.setRequestProperty("Accept", accept);
            con.setDoOutput(true);
            con.setConnectTimeout(Main.CONFIG.CONFIG.getPath(Long.class,"Downloads/httpTimeout").intValue());

            OutputStream outs = con.getOutputStream();
            byte[] input = payload.getBytes(StandardCharsets.UTF_8);
            outs.write(input);

            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
            StringBuffer sb = new StringBuffer();
            String responseLine;
            while((responseLine = br.readLine()) != null) {
                sb.append(responseLine.trim());
            }
            br.close();
            return sb.toString();
        }catch(IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Sanitizes a URL to be valid by encoding illegal chars like spaces
     * @param url the URL to sanitize
     * @return the sanitized URL
     */
    public static URL sanitizeURL(URL url) {
        try {
            URI uri = new URI(url.getProtocol(),
                    url.getUserInfo(),
                    url.getHost(),
                    url.getPort(),
                    url.getPath(),
                    url.getQuery(),
                    url.getRef());
            return uri.toURL();
        }catch(MalformedURLException | URISyntaxException ignored) {
        }
        return null;
    }
}
