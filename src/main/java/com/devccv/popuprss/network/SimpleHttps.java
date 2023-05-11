package com.devccv.popuprss.network;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class SimpleHttps {
    public static RequestResult GET(URL url, Proxy proxy) {
        return send(url, proxy);
    }

    private static RequestResult send(URL url, Proxy proxy) {
        Map<String, List<String>> headerFields = null;
        try {
            HttpsURLConnection httpsURLConnection = getHttpsURLConnection(url, proxy);

            headerFields = httpsURLConnection.getHeaderFields();

            StringBuilder rawData;
            rawData = new StringBuilder();
            try (InputStream inputStream = httpsURLConnection.getInputStream();
                 BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String data;
                while ((data = bufferedReader.readLine()) != null) {
                    rawData.append(data).append('\n');
                }
            }
            String rawDataString = rawData.toString();

            return new RequestResult(rawDataString, headerFields);
        } catch (IOException e) {
            return new RequestResult(e, headerFields);
        }
    }

    private static HttpsURLConnection getHttpsURLConnection(URL url, Proxy proxy) throws IOException {
        HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection(proxy);
        httpsURLConnection.setConnectTimeout(5000);
        httpsURLConnection.setReadTimeout(5000);
        httpsURLConnection.setRequestProperty("Connection", "Keep-Alive");
        httpsURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) PopupRSS/1.0");
        return httpsURLConnection;
    }
}
