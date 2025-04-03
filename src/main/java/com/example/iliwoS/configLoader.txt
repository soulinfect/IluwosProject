package com.example.libvoS;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigLoader {
    private static String API_KEY;
    private static final Path CONFIG_PATH = Paths.get(
        "src", "main", "java", "com", "example", "libvoS", "config.json"
    );

    public static void loadConfig() {
        try {
            File configFile = CONFIG_PATH.toFile();
            // Читаем ключ из файла
            JsonObject json = new JsonParser().parse(new FileReader(configFile)).getAsJsonObject();
            API_KEY = json.get("hypixel_api_key").getAsString();
            
        } catch (Exception e) {
            System.err.println("[ConfigLoader] Ошибка загрузки конфига: " + e.getMessage());
            API_KEY = "";
        }
    }
}