package com.Iluwos;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.client.Minecraft;

public class IluwoSLogger {
    private static final String PREFIX = "[Ilup] ";
    private static final String INGAME_PREFIX = "\u00A7d[IluP]\u00A7e "; 
    private static final String RED_PREFIX = "\u00A7c[IluP] "; 
    private static final String DEBUG = "[DEBUG] ";
    private static final String INFO = "[INFO] ";
    private static final String ERROR = "[ERROR] ";
    private static final String TAG = "iluwosproject"; 

    public static void debug(String message) {
        System.out.println(PREFIX + DEBUG + message + " - " + TAG);
    }

    public static void info(String message) {
        System.out.println(PREFIX + INFO + message + " - " + TAG);
    }

    public static void error(String message) {
        System.err.println(PREFIX + ERROR + message + " - " + TAG);
    }

    public static void error(String message, Throwable throwable) {
        System.err.println(PREFIX + ERROR + message + " - " + TAG);
        throwable.printStackTrace();
    }

    public static void send(String message) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(INGAME_PREFIX + message));
    }
    public static void sendError(String message) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(RED_PREFIX + message));
    }
}
