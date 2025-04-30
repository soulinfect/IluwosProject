package com.Iluwos.discord;

import com.Iluwos.IluwoSLogger;

import java.io.*;
import java.nio.file.*;

public class DiscordHandler {

    public static void executePythonScript() {
        IluwoSLogger.info("Attempting to execute Python script...");

        try {
            // Попытка загрузить скрипт
            InputStream scriptStream = DiscordHandler.class.getResourceAsStream("/scripts/discord.py");
            if (scriptStream == null) {
                IluwoSLogger.error("Script not found in resources!");
                return;  // Скрипт не найден, выходим
            }

            // Создание временного файла для скрипта
            File tempScript = File.createTempFile("discord_activity", ".py");
            tempScript.deleteOnExit();
            IluwoSLogger.info("Temporary script file created: " + tempScript.getAbsolutePath());

            // Копирование содержимого скрипта в файл
            try (OutputStream os = new FileOutputStream(tempScript)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = scriptStream.read(buffer)) != -1) {
                    os.write(buffer, 0, length);
                }
                IluwoSLogger.info("Script content copied to temporary file successfully.");
            }

            // Запуск Python скрипта
            ProcessBuilder pb = new ProcessBuilder("python", tempScript.getAbsolutePath());
            pb.inheritIO(); 
            IluwoSLogger.info("Executing Python script...");
            Process process = pb.start();
            int exitCode = process.waitFor(); 
            IluwoSLogger.info("Python script executed with exit code: " + exitCode);

        } catch (Exception e) {
            IluwoSLogger.error("Error executing Python script: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void init() {
        IluwoSLogger.info("Initializing DiscordHandler...");
        executePythonScript();
    }
}
