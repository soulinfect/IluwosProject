package com.Iluwos.commands;

import com.Iluwos.IluwoS;
import com.Iluwos.HypixelApiUtils;
import com.Iluwos.IluwoSLogger;
import com.Iluwos.ModConfig;
import com.Iluwos.ConfigManager;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import java.lang.Thread;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import java.util.UUID;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.lang.*;

public class CommandTrack extends CommandBase {
    @Override
    public String getCommandName() {
        return "track";
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("t", "tr");
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/t [item_name]\n/t stop/start\n/t remove [item]\n/t info\n/t timerset [ticks]";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            IluwoSLogger.send("Incorrect command use:\n" + getCommandUsage(sender));
            return;
        }
        ModConfig config = ConfigManager.loadConfig();
        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "stop":
                if (!config.get_tracking_status()) {
                    IluwoSLogger.sendError("Tracking has already been stopped!");
                } else {
                    IluwoSLogger.send("\u00A7cTracking stopped!");
                    config.set_tracking_status(false);
                    ConfigManager.saveConfig(config);
                }
                break;

            case "start":
                if (config.get_tracking_status()) {
                    IluwoSLogger.sendError("Tracking is already underway!");
                } else {
                    IluwoSLogger.send("\u00A7aTracking started!");
                    config.set_tracking_status(true);
                    ConfigManager.saveConfig(config);
                }
                break;

            case "timerset":
                if (args.length < 2 || args[1] == null || args[1].isEmpty()) {
                    IluwoSLogger.send("Write the time in ticks!");
                    break;
                }
                try {
                    int timerticks = Integer.parseInt(args[1]);
                    if (timerticks < 1200) {
                        IluwoSLogger.sendError("Time in ticks must be more than 1200.");
                    } else if (timerticks > 72000) {
                        IluwoSLogger.sendError("That's too high a number.");
                    } else {
                        config.set_track_timer_ticks(timerticks);
                        IluwoS.timerticks = timerticks;
                        ConfigManager.saveConfig(config);
                        IluwoSLogger.send("The timer time is set to " + timerticks + " ticks.");
                    }
                } catch (NumberFormatException e) {
                    IluwoSLogger.sendError("Write the time in ticks!");
                }
                break;

            case "preset":
                if (args.length < 2 || args[1] == null || args[1].isEmpty()) {
                    IluwoSLogger.sendError("Write the preset name: Custom or AllItems");
                    break;
                }
                String preset = args[1].toLowerCase();
                switch (preset) {
                    case "custom":
                        config.set_activePreset("Custom");
                        IluwoS.activePreset = "Custom";
                        ConfigManager.saveConfig(config);
                        IluwoSLogger.send("Custom preset is set!");
                        break;
                    case "allitems":
                        config.set_activePreset("AllItems");
                        IluwoS.activePreset = "AllItems";
                        ConfigManager.saveConfig(config);
                        IluwoSLogger.send("AllItems preset is set!");
                        break;
                    default:
                        IluwoSLogger.sendError("Write the preset name: Custom or AllItems");
                }
                break;

            case "remove":
                if (args.length < 2 || args[1] == null || args[1].isEmpty()) {
                    IluwoSLogger.sendError("Write the item name to remove.");
                    break;
                }
                String itemToRemove = args[1].toUpperCase();
                new Thread(() -> {
                    try {
                        JsonObject sacksCounts = HypixelApiUtils.getSacksCount();
                        if (sacksCounts == null || !sacksCounts.has(itemToRemove)) {
                            IluwoSLogger.sendError("Item " + itemToRemove + " not found in your sacks!");
                            return;
                        }

                        if (!config.getPresets().get("Custom").getItems().containsKey(itemToRemove)) {
                            IluwoSLogger.sendError("This item (" + itemToRemove + ") is not tracked.");
                            return;
                        }

                        Integer removed = config.getPresets().get("Custom").getItems().remove(itemToRemove);
                        if (removed != null) {
                            IluwoSLogger.send(itemToRemove + " successfully removed!");
                            ConfigManager.saveConfig(config);
                        } else {
                            IluwoSLogger.sendError("Unexpected error: item not removed.");
                        }
                    } catch (Exception e) {
                        IluwoSLogger.sendError("An error occurred: " + e.getMessage());
                        IluwoSLogger.error("Error in /track", e);
                    }
                }).start();
                break;
            case "info": 
                StringBuilder msg = new StringBuilder();
                if (config.get_tracking_status()) {
                    msg.append("Status: \u00A7aactive.\u00A7r\u00A7e Time between requests: ");
                } else {
                    msg.append("Status: \u00A7cstopped.\u00A7r\u00A7e Time between requests: ");
                }
                msg.append(IluwoS.timerticks / 20);
                msg.append("s. \nItems info: ");
                for (Map.Entry<String, Integer> entry : config.getPresets().get("Custom").getItems().entrySet()) {
                    msg.append(entry.getKey().toLowerCase()).append(", ");
                }
                if (msg.length() >= 2) {
                    msg.delete(msg.length() - 2, msg.length());
                }
                IluwoSLogger.send(msg.toString());
                break;
                // обработка пустого пресета
            default:
                String item = args[0].toUpperCase();
                HypixelApiUtils.addItem(item);
                break;
            }
        }
    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
