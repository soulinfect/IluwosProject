package com.Iluwos.commands;

import com.Iluwos.HypixelApiUtils;
import com.Iluwos.IluwoSLogger;
import com.Iluwos.ModConfig;
import com.Iluwos.ConfigManager;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.lang.Thread;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import java.util.UUID;


public class CommandTrack extends CommandBase {
    @Override
    public String getCommandName() {
        return "track";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/track [item_name]\n/track stop\n/track start";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            sender.addChatMessage(new ChatComponentText("[IluP] Please enter the item name"));
            return;
        }
        ModConfig config = ConfigManager.loadConfig();
        if (args[0].equals("stop")) {
            if (!config.get_tracking_status()) {
                sender.addChatMessage(new ChatComponentText("[IluP] Tracking has already been stopped!"));
                return;
            }
            sender.addChatMessage(new ChatComponentText("[IluP] Tracking stopped!"));
            config.set_tracking_status(false);
            ConfigManager.saveConfig(config);
            return;
        } else if (args[0].equals("start")) {
            if (config.get_tracking_status()) {
                sender.addChatMessage(new ChatComponentText("[IluP] Tracking is already underway!"));
                return;
            }
            sender.addChatMessage(new ChatComponentText("[IluP] Tracking started!"));
            config.set_tracking_status(true);
            ConfigManager.saveConfig(config);
            return;
        } else if (sender instanceof EntityPlayer) {
            UUID raw_uuid = ((EntityPlayer) sender).getUniqueID();
            String uuid = raw_uuid.toString().replace("-", "");

            String item = args[0].toUpperCase();
            if (config.getItems().containsKey(item)) {
                sender.addChatMessage(new ChatComponentText("[IluP] This item is already being tracked."));
                return;
            } else {
                new Thread(() -> {
                    String jsonResponse = HypixelApiUtils.getProfilesData(uuid);
                    int activeProfileIndex = HypixelApiUtils.findActiveProfileIndex(jsonResponse);
            
                    if (activeProfileIndex == -1) {
                        sender.addChatMessage(new ChatComponentText("[IluP] No active profile found!"));
                        return;
                    }
            
                    try {
                        JsonObject response = new JsonParser().parse(jsonResponse).getAsJsonObject();
                        JsonObject profile = response.getAsJsonArray("profiles").get(activeProfileIndex).getAsJsonObject();
                        JsonObject members = profile.getAsJsonObject("members");
                        JsonObject userData = members.getAsJsonObject(uuid);
                        JsonObject inventory = userData.getAsJsonObject("inventory");
                        JsonObject sacksCounts = inventory.getAsJsonObject("sacks_counts");
            
                        if (sacksCounts == null || !sacksCounts.has(item)) {
                            sender.addChatMessage(new ChatComponentText("[IluP] Item " + item + " not found in your sacks!"));
                            return;
                        }
            
                        String itemCount = sacksCounts.get(item).getAsString();
                        sender.addChatMessage(new ChatComponentText("[IluP] Item succesfully added! " + item + " count: " + itemCount));
                        config.getItems().put(item, Integer.parseInt(itemCount));
                        ConfigManager.saveConfig(config);
                    } catch (Exception e) {
                        sender.addChatMessage(new ChatComponentText("[IluP] An error occurred: " + e.getMessage()));
                        IluwoSLogger.error("Error in /track", e);
                    }
                }).start();
            }
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
