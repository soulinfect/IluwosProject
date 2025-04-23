package com.Iluwos;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.inventory.Slot;
import net.minecraftforge.client.event.GuiScreenEvent;

import org.lwjgl.input.Keyboard;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.gameevent.InputEvent;

import com.Iluwos.commands.CommandTrack;
import com.Iluwos.ConfigManager;
import com.Iluwos.ModConfig;
//import com.Iluwos.DiscordRichPresenceManager;

import java.util.ArrayList;
import java.util.Map;

@Mod(modid = IluwoS.MODID, version = IluwoS.VERSION, acceptedMinecraftVersions = "[1.8.9]")
public class IluwoS {
    public static final String MODID = "iluwos";
    public static final String VERSION = "0.1";

    public static ArrayList<String> ItemNamesArray = new ArrayList<>();
    public static ArrayList<Integer> ItemCountsArray = new ArrayList<>();
    public static String activePreset;

    private static int tickCounter = 0;
    public static String lastMessage = "";
    private static Integer timerticks;
    public static KeyBinding itemScanningKey;
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        ClientCommandHandler.instance.registerCommand(new CommandTrack());
        ModConfig config = ConfigManager.loadConfig();

        activePreset = config.get_activePreset();
        timerticks = config.get_track_timer_ticks();
        Map<String, Integer> items = config.getPresets().get(activePreset).getItems();
        for (Map.Entry<String, Integer> entry : items.entrySet()) {
            ItemNamesArray.add(entry.getKey());
            ItemCountsArray.add(entry.getValue());
        }
        itemScanningKey = new KeyBinding("Add Item", Keyboard.KEY_I, "IluwoS");
        ClientRegistry.registerKeyBinding(itemScanningKey);
        //DiscordRichPresenceManager.start();
        IluwoSLogger.info("Iluwos has been successfully initialized!");
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (Minecraft.getMinecraft() == null || Minecraft.getMinecraft().thePlayer == null) {
            return;
        }

        // if (event.phase == TickEvent.Phase.END) {
        //     DiscordRichPresenceManager.runCallbacks();
        // }
        ModConfig config = ConfigManager.loadConfig();
        if (!config.get_tracking_status()) {
            return;
        }
        
        tickCounter++;
        if (tickCounter >= timerticks) {
            tickCounter = 0;
            if ((ItemNamesArray.isEmpty() || ItemCountsArray.isEmpty()) && !"AllItems".equals(activePreset)) {
                IluwoSLogger.error("Item names or counts are empty. Tracking is disabled!");
                IluwoSLogger.sendError("Item names or counts are empty. Tracking is disabled!");
                config.set_tracking_status(false);
                ConfigManager.saveConfig(config);
                return;
            }
            if (config.get_activePreset().equals(activePreset)) {
                HypixelApiUtils.checkItems();
            } else {
                activePreset = config.get_activePreset();
                ItemCountsArray.clear();
                ItemCountsArray.clear();
                Map<String, Integer> items = config.getPresets().get(activePreset).getItems();
                for (Map.Entry<String, Integer> entry : items.entrySet()) {
                    ItemNamesArray.add(entry.getKey());
                    ItemCountsArray.add(entry.getValue());
                }
                if ((ItemNamesArray.isEmpty() || ItemCountsArray.isEmpty()) && !"AllItems".equals(activePreset)) {
                    IluwoSLogger.error("Item names or counts are empty. Tracking is disabled!");
                    IluwoSLogger.sendError("Item names or counts are empty. Tracking is disabled!");
                    config.set_tracking_status(false);
                    ConfigManager.saveConfig(config);
                    return;
                }
                HypixelApiUtils.checkItems();
            }
        }
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (itemScanningKey.isPressed()) {
            if (Minecraft.getMinecraft().currentScreen == null) {
                HypixelApiUtils.itemScanning();
            }
        }
    }

    @SubscribeEvent
    public void onGuiKeyInput(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        if (Keyboard.isKeyDown(itemScanningKey.getKeyCode())) {
            HypixelApiUtils.itemScanning();
        }
    }
}
// не меняется пресет
// изменить вывод (цвет + маленькие буквы)