package com.IluProject.IluwosProject;

import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import com.IluProject.IluwosProject.commands.CommandTrack;


@Mod(modid = IluwoS.MODID, version = IluwoS.VERSION, acceptedMinecraftVersions = "[1.8.9]")
public class IluwoS {
    public static final String MODID = "iluwos";
    public static final String VERSION = "1.0";

    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        ClientCommandHandler.instance.registerCommand(new CommandTrack());
        IluwoSLogger.info("IluwoS has been successfully initialized!");
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        // обработка чата 
    }
    
}


