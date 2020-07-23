package ley.anvil.modpacktools.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import static ley.anvil.modpacktools.Main.HTTP_CLIENT;

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
     * @param contentType what content type should be used. Example: {@code MediaType.parse("application/json; utf-8")}
     * @param payload the payload to send
     * @param additionalHeaders additional headers that should be added to the request
     * @return the response as string
     */
    public static String httpPostString(URL url, String payload, MediaType contentType, Map<String, String> additionalHeaders) throws IOException {
        Request.Builder builder = new Request.Builder().url(url)
                .post(RequestBody.create(payload, contentType));

        additionalHeaders.forEach(builder::addHeader);

        Response resp = HTTP_CLIENT.newCall(builder.build()).execute();
        String rString = resp.body().string();
        resp.close();
        return rString;
    }

    /**
     * sends a http post request
     *
     * @param url the url to send the request to
     * @param contentType what content type should be used. Example: {@code "application/json; utf-8"}
     * @param payload the payload to send
     * @param additionalHeaders additional headers that should be added to the request
     * @return the response as string
     */
    public static String httpPostString(URL url, String payload, String contentType, Map<String, String> additionalHeaders) throws IOException {
        return httpPostString(
                url,
                payload,
                MediaType.get(contentType),
                additionalHeaders
        );
    }

    /**
     * Sanitizes a URL to be valid by encoding illegal chars like spaces
     *
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
