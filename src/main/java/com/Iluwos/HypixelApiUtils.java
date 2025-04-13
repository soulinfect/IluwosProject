package com.Iluwos;

import com.Iluwos.IluwoSLogger;
import com.Iluwos.IluwoS;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.lang.Thread;
import java.util.concurrent.CountDownLatch;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.util.UUID;
import java.io.ByteArrayOutputStream;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.client.Minecraft;

public class HypixelApiUtils {
    public static String loadApiKey() throws IOException {
        try {
            InputStream inputStream = HypixelApiUtils.class.getClassLoader().getResourceAsStream("config.json");

            if (inputStream == null) {
                throw new FileNotFoundException("Resource 'config.json' not found");
            }

            String configContent = readFromInputStream(inputStream);
            JsonParser parser = new JsonParser();
            JsonObject config = parser.parse(configContent).getAsJsonObject();
            return config.get("api_key").getAsString();

        } catch (Exception e) {
            IluwoSLogger.error("loadapikey error: ", e);
            return null;
        }
    }
    private static String readFromInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString(StandardCharsets.UTF_8.name());
    }
    public static String getProfilesData(String uuid) {
        try {
            String apiKey = loadApiKey();
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
                //IluwoSLogger.debug("HTTP Response Code: " + responseCode);

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
        } catch (IOException e) {
            IluwoSLogger.error("Error with config reader: ", e);
            return null;
        } catch (Exception e) {
            IluwoSLogger.error("Error with JSON parsing: ", e);
            return null;
        }
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
                    //IluwoSLogger.info("Active profile found! Index: " + i);
                    return i;
                }
            }

            IluwoSLogger.error("Active profile not found");

        } catch (Exception e) {
            IluwoSLogger.error("An error occurred while parsing the JSON response", e);
        }

        return -1;
    }
    public static void checkItems() {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.thePlayer;
        CountDownLatch latch = new CountDownLatch(1);
        try {
            String apiKey = loadApiKey();
            int[] newItemCountArray = new int[IluwoS.ItemNamesArray.size()];
            UUID raw_uuid = player.getUniqueID();
            String uuid = raw_uuid.toString().replace("-", "");
    
            new Thread(() -> {
                String jsonResponse = HypixelApiUtils.getProfilesData(uuid);
                int activeProfileIndex = HypixelApiUtils.findActiveProfileIndex(jsonResponse);
    
                if (activeProfileIndex == -1) {
                    Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("[IluP] No active profile found!"));
                    return; 
                }
                try {
                    JsonObject response = new JsonParser().parse(jsonResponse).getAsJsonObject();
                    JsonObject profile = response.getAsJsonArray("profiles").get(activeProfileIndex).getAsJsonObject();
                    JsonObject members = profile.getAsJsonObject("members");
                    JsonObject userData = members.getAsJsonObject(uuid);
                    JsonObject inventory = userData.getAsJsonObject("inventory");
                    JsonObject sacksCounts = inventory.getAsJsonObject("sacks_counts");
                    int itemCount;
                    for (int i = 0; i < IluwoS.ItemNamesArray.size(); i++) {
                        if (sacksCounts == null || !sacksCounts.has(IluwoS.ItemNamesArray.get(i))) {
                            return;
                        }
    
                        itemCount = sacksCounts.get(IluwoS.ItemNamesArray.get(i)).getAsInt();
                        newItemCountArray[i] = itemCount;
                    }
                } catch (Exception e) {
                    Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("[IluP] An error occurred: " + e.getMessage()));
                    IluwoSLogger.error("Error in /track", e);
                    return;
                }
    
                StringBuilder msg = new StringBuilder();
                boolean msgUpdate = false;
    
                for (int i = 0; i < newItemCountArray.length; i++) {
                    mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(IluwoS.ItemNamesArray.get(i) + ": " + IluwoS.ItemCountsArray.get(i) + " -> " + newItemCountArray[i]));
                    if (newItemCountArray[i] != IluwoS.ItemCountsArray.get(i)) {
                        msgUpdate = true;
                        msg.append("[IluP] ");
                        msg.append(IluwoS.ItemNamesArray.get(i));
    
                        int difference = newItemCountArray[i] - IluwoS.ItemCountsArray.get(i);
                        if (difference > 0) {
                            msg.append(" \u00A7a[+");
                            msg.append(difference);
                            msg.append("]");
                        } else {
                            msg.append(" \u00A7c[");
                            msg.append(difference);
                            msg.append("]");
                        }
                        IluwoS.ItemCountsArray.set(i, newItemCountArray[i]);
                        msg.append("\n");  
                    }
                }
                if (msgUpdate) {
                    if (!IluwoS.lastMessage.isEmpty()) {
                        mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new ChatComponentText(""), 2376);
                    }
                    mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new ChatComponentText(msg.toString()), 2376);
                    IluwoS.lastMessage = msg.toString();
                }
            }).start(); 
        } catch (IOException e) {
            IluwoSLogger.error("Error with config reader: ", e);
            return;
        } catch (Exception e) {
            IluwoSLogger.error("Error with JSON parsing: ", e);
            return;
        }
    }
    
}

