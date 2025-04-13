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

import com.Iluwos.commands.CommandTrack;
import com.Iluwos.ConfigManager;
import com.Iluwos.ModConfig;

import java.util.ArrayList;
import java.util.Map;

@Mod(modid = IluwoS.MODID, version = IluwoS.VERSION, acceptedMinecraftVersions = "[1.8.9]")
public class IluwoS {
    public static final String MODID = "iluwos";
    public static final String VERSION = "0.1";

    public static ArrayList<String> ItemNamesArray = new ArrayList<>();
    public static ArrayList<Integer> ItemCountsArray = new ArrayList<>();

    private static int tickCounter = 0;
    public static String lastMessage = "";

    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        ClientCommandHandler.instance.registerCommand(new CommandTrack());

        ModConfig config = ConfigManager.loadConfig();
        Map<String, Integer> items = config.getItems();

        for (Map.Entry<String, Integer> entry : items.entrySet()) {
            ItemNamesArray.add(entry.getKey());
            ItemCountsArray.add(entry.getValue());
        }
        IluwoSLogger.info("IluwoS has been successfully initialized!");
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (Minecraft.getMinecraft() == null || Minecraft.getMinecraft().thePlayer == null) {
            return;
        }
        ModConfig config = ConfigManager.loadConfig();
        if (!config.get_tracking_status()) {
            return;
        }

        tickCounter++;

        if (tickCounter >= 200) {
            tickCounter = 0;

            if (ItemNamesArray.isEmpty() || ItemCountsArray.isEmpty()) {
                IluwoSLogger.error("Item names or counts are empty. Stop tracking.");
                config.set_tracking_status(false);
                return;
            }
            HypixelApiUtils.checkItems();
        }
    }
}
// команда для вывода списка всех предметов
// поменять таймер
// пустая строка
// убрать постоянный вывод в логи
// гемы