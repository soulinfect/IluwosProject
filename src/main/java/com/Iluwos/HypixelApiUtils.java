package com.Iluwos;

import com.Iluwos.IluwoSLogger;
import com.Iluwos.IluwoS;
import com.Iluwos.ModConfig;
import com.Iluwos.ConfigManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;

import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.ByteArrayOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.UUID;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.lang.Thread;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.SocketTimeoutException;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.inventory.Slot;
import net.minecraftforge.client.event.GuiScreenEvent;

public class HypixelApiUtils {
    public static String getProfilesData(String uuid) {
    try {
        String apiUrl = "http://127.0.0.1:5000/skyblock/profiles?uuid=" + uuid;

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
            //IluwoSLogger.debug("Profiles found: " + profiles.size());
            for (int i = 0; i < profiles.size(); i++) {
                JsonObject profile = profiles.get(i).getAsJsonObject();
                if (profile.has("selected") && profile.get("selected").getAsBoolean()) {
                    //IluwoSLogger.info("Active profile found! Index: " + i);
                    return i;
                }
            }
            ModConfig config = ConfigManager.loadConfig();
            IluwoSLogger.error("Active profile not found, Tracking is disabled!");
            IluwoSLogger.sendError("Active profile not found, Tracking is disabled!");
            config.set_tracking_status(false);
            ConfigManager.saveConfig(config);
            return -1;
        } catch (Exception e) {
            IluwoSLogger.error("An error occurred while parsing the JSON response", e);
        }

        return -1;
    }
    public static void checkItems() {
        ModConfig config = ConfigManager.loadConfig();
        int[] newItemCountArray = new int[IluwoS.ItemNamesArray.size()];
    
        new Thread(() -> {
            try {
                JsonObject sacksCounts = getSacksCount();
                if (sacksCounts == null) {
                    IluwoSLogger.sendError("SacksCount is null!");
                    return;
                }

                if (IluwoS.ItemNamesArray.size() != IluwoS.ItemCountsArray.size()) {
                    IluwoSLogger.error("ItemNamesArray and ItemCountsArray are out of sync!");
                    return;
                }
    
                for (int i = 0; i < IluwoS.ItemNamesArray.size(); i++) {
                    String itemName = IluwoS.ItemNamesArray.get(i);
                    int count = sacksCounts.has(itemName) ? sacksCounts.get(itemName).getAsInt() : 0;
                    newItemCountArray[i] = count;
                }
    
                StringBuilder msg = new StringBuilder();
                boolean msgUpdate = false;
                Minecraft mc = Minecraft.getMinecraft();
                boolean isAllItemsEmpty = config.getPresets()
                        .get("AllItems")
                        .getItems()
                        .isEmpty();
                if (isAllItemsEmpty) {
                    for (Map.Entry<String, JsonElement> entry : sacksCounts.entrySet()) {
                        String itemName = entry.getKey();
                        int itemCount = entry.getValue().getAsInt();
                        config.getPresets()
                            .get("AllItems")
                            .getItems()
                            .put(itemName, itemCount);
                    }
                    ConfigManager.saveConfig(config);
                }
                for (int i = 0; i < newItemCountArray.length; i++) {
                    int newCount = newItemCountArray[i];
                    int oldCount = IluwoS.ItemCountsArray.get(i);
    
                    if (newCount != oldCount) {
                        msgUpdate = true;
                        msg.append("\u00A7dIluP \u00BB\u00A7e ")
                           .append(IluwoS.ItemNamesArray.get(i).toLowerCase())
                           .append(" count: ")
                           .append(newCount);
                        int diff = newCount - oldCount;
                        msg.append(diff > 0 ? " \u00A7r\u00A7a[+" + diff + "]" : " \u00A7r\u00A7c[" + diff + "]");
                        IluwoS.ItemCountsArray.set(i, newCount);
                        msg.append("\n");
                        config.getPresets()
                            .get(IluwoS.activePreset)
                            .getItems()
                            .put(IluwoS.ItemNamesArray.get(i), newCount);
                    }
                }
                if (msgUpdate) {
                    ConfigManager.saveConfig(config);
                    String finalMsg = msg.toString().trim();
                    mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new ChatComponentText(finalMsg), 2376);
                    IluwoS.lastMessage = finalMsg;
                }
            } catch (Exception e) {
                IluwoSLogger.sendError("An error occurred: " + e.getMessage());
                IluwoSLogger.error("Error in checkItems", e);
            }
        }).start();
    }
    public static JsonObject getSacksCount() {
        try { 
            ModConfig config = ConfigManager.loadConfig();
            Minecraft mc = Minecraft.getMinecraft();
            EntityPlayer player = mc.thePlayer;
            UUID raw_uuid = player.getUniqueID();
            String uuid = raw_uuid.toString().replace("-", "");
            String jsonResponse = HypixelApiUtils.getProfilesData(uuid);
            int activeProfileIndex = HypixelApiUtils.findActiveProfileIndex(jsonResponse);

            if (activeProfileIndex == -1) {
                config.set_tracking_status(false);
                ConfigManager.saveConfig(config);
                IluwoSLogger.sendError("An error occurred with api! Tracking has been stopped.");
                return null; 
            }
            JsonObject response = new JsonParser().parse(jsonResponse).getAsJsonObject();
            JsonObject profile = response.getAsJsonArray("profiles").get(activeProfileIndex).getAsJsonObject();
            JsonObject members = profile.getAsJsonObject("members");
            JsonObject userData = members.getAsJsonObject(uuid);
            JsonObject inventory = userData.getAsJsonObject("inventory");
            JsonObject sacksCounts = inventory.getAsJsonObject("sacks_counts");
            return sacksCounts;
        } catch (Exception e) {
            IluwoSLogger.sendError("An error occurred: " + e.getMessage());
            IluwoSLogger.error("Error in /track", e);
            return null;
        }
    }
    public static JsonObject gemstoneProcessing(JsonObject sacksCounts) {
        return sacksCounts;
    }
    public static void addItem(String item) {
        ModConfig config = ConfigManager.loadConfig();
        if (config.getPresets().get("Custom").getItems().containsKey(item)) {
            IluwoSLogger.sendError("This item is already being tracked.");
        } else {
            new Thread(() -> {
                try {
                    JsonObject sacksCounts = getSacksCount();
                    if (sacksCounts == null || !sacksCounts.has(item)) {
                        IluwoSLogger.sendError("Item " + item + " not found in your sacks!");
                        return;
                    }
                    String itemCount = sacksCounts.get(item).getAsString();
                    config.getPresets().get("Custom").getItems().put(item, Integer.parseInt(itemCount));
                    IluwoSLogger.send("Item successfully added! " + item + " count: " + itemCount);
                    ConfigManager.saveConfig(config);
                } catch (Exception e) {
                    IluwoSLogger.sendError("An error occurred: " + e.getMessage());
                    IluwoSLogger.error("Error in /track", e);
                }
            }).start();
        }
    }
    public static void itemScanning() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null) {
            IluwoSLogger.error("itemScanning: Minecraft instance is null");
            return;
        }
    
        if (mc.currentScreen instanceof GuiContainer) {
            IluwoSLogger.debug("itemScanning: current screen is a container GUI");
            Slot hoveredSlot = ((GuiContainer) mc.currentScreen).getSlotUnderMouse();
            if (hoveredSlot != null) {
                if (hoveredSlot.getHasStack()) {
                    ItemStack stack = hoveredSlot.getStack();
                    String displayName = stack.getDisplayName();
                    String registryName = Item.itemRegistry.getNameForObject(stack.getItem()).toString();
    
                    IluwoSLogger.debug("itemScanning: displayName = " + displayName);
                    IluwoSLogger.debug("itemScanning: registryName = " + registryName);
    
                    String customId = "N/A";
                    NBTTagCompound tag = stack.getTagCompound();
                    if (tag != null) {
                        if (tag.hasKey("ExtraAttributes")) {
                            NBTTagCompound extra = tag.getCompoundTag("ExtraAttributes");
                            if (extra.hasKey("id")) {
                                customId = extra.getString("id");
                                IluwoSLogger.debug("itemScanning: found ExtraAttributes id = " + customId);
                            }
                        }
                    } 
                    IluwoSLogger.send("Item detected - " + customId);
                    addItem(customId);
                } else {
                    IluwoSLogger.debug("itemScanning: hoveredSlot does not have a stack");
                    IluwoSLogger.sendError("No item detected!");
                }
            } else {
                IluwoSLogger.debug("itemScanning: hoveredSlot is null");
                IluwoSLogger.sendError("No item detected!");
            }
        } else {
            IluwoSLogger.debug("itemScanning: current screen is not a container GUI");
        }
    }
}