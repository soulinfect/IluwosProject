package com.example.examplemod;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = SowlAddon.MODID, version = SowlAddon.VERSION) 
public class SowlAddon {  
    public static final String MODID = "sowladdon";  
    public static final String VERSION = "1.0";

    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);  // Регистрация событий
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (event.message.getUnformattedText().contains("ping")) {
            Minecraft.getMinecraft().thePlayer.sendChatMessage("pong");
        }
    }
}