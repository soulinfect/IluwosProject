package com.IluProject.IluwosProject;

import com.IluProject.IluwosProject.IluwoSLogger;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;


public class HypixelApiUtils {
    public static String getProfilesData(String uuid) {
        String apiKey = "9eb1afa9-6b7a-40b1-8e3f-85cf89425dcd";
        String apiUrl = "https://api.hypixel.net/v2/skyblock/profiles?key=" + apiKey + "&uuid=" + uuid;

        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(apiUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);

            int responseCode = connection.getResponseCode();
            IluwoSLogger.debug("HTTP Response Code: " + responseCode);

            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP error code: " + responseCode);
            }

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();

        } catch (Exception e) {
            IluwoSLogger.error("An error occurred while getting profile data", e);
        } finally {
            try {
                if (reader != null) reader.close();
            } catch (IOException e) {
                IluwoSLogger.error("An error occurred while closing the reader", e);
            }
            if (connection != null) connection.disconnect();
        }
        return null;
    }
    public static int findActiveProfileIndex(String jsonResponse) {
        if (jsonResponse == null || jsonResponse.isEmpty()) {
            IluwoSLogger.error("The JSON response is empty or null");
            return -1;
        }

        try {
            JsonObject response = new JsonParser().parse(jsonResponse).getAsJsonObject();
            JsonArray profiles = response.getAsJsonArray("profiles");
            IluwoSLogger.debug("Profiles found: " + profiles.size());
            for (int i = 0; i < profiles.size(); i++) {
                JsonObject profile = profiles.get(i).getAsJsonObject();
                if (profile.has("selected") && profile.get("selected").getAsBoolean()) {
                    IluwoSLogger.info("Active profile found! Index: " + i);
                    return i;
                }
            }

            IluwoSLogger.error("Active profile not found");

        } catch (Exception e) {
            IluwoSLogger.error("An error occurred while parsing the JSON response", e);
        }

        return -1;
    }
}
