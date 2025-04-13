package com.Iluwos;

import net.minecraftforge.fml.common.Loader;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE_NAME = "iluwos_config.json";  
    
    public static Path getConfigPath() {
        return Loader.instance().getConfigDir().toPath();
    }

    public static ModConfig loadConfig() {
        Path configPath = getConfigPath().resolve(CONFIG_FILE_NAME);

        try {
            if (Files.exists(configPath)) {
                byte[] fileBytes = Files.readAllBytes(configPath);
                String json = new String(fileBytes, StandardCharsets.UTF_8);
                return GSON.fromJson(json, ModConfig.class);
            } else {
                ModConfig defaultConfig = new ModConfig();
                saveConfig(defaultConfig);
                return defaultConfig;
            }
        } catch (Exception e) {
            IluwoSLogger.error("Error loading config in /track", e);
            return new ModConfig(); 
        }
    }

    public static void saveConfig(ModConfig config) {
        try {
            Path configPath = getConfigPath().resolve(CONFIG_FILE_NAME);
            Files.createDirectories(configPath.getParent());
            String json = GSON.toJson(config);
            
            Files.write(configPath, json.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            IluwoSLogger.error("Error saving config in /track", e);
        }
    }
}
