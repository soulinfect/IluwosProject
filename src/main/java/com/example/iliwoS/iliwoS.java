package com.example.examplemod;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = iliwoS.MODID, version = iliwoS.VERSION, acceptedMinecraftVersions = "[1.8.9]")
public class iliwoS {
    public static final String MODID = "iliwoS";
    public static final String VERSION = "1.0";

    // String apiKey = configLoader.getApiKey();
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        String msg = event.message.getUnformattedText();
        if (msg.equalsIgnoreCase("/uuid")) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(
                    new ChatComponentText(Minecraft.getMinecraft().thePlayer.getName()));
        }
    }
}