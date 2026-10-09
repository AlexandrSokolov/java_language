package com.savdev.io.inputStream;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class Url2InputStream {

    public static InputStream inputStream(String url){
        try {
            return new URL(url).openStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static InputStream inputStreamViaURLConnection(String url){
        try {
            URL urlObj = new URL(url);
            URLConnection connection = urlObj.openConnection();
            connection.setRequestProperty("Accept-Charset",
                    StandardCharsets.UTF_8.name());
            HttpURLConnection httpURLConnection = (HttpURLConnection) connection;
            httpURLConnection.setRequestMethod("GET");
            int status = httpURLConnection.getResponseCode();
            for (Map.Entry<String, List<String>> header
                    : connection.getHeaderFields().entrySet()) {
                System.out.println(header.getKey() + "=" + header.getValue());
            }

            return connection.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
