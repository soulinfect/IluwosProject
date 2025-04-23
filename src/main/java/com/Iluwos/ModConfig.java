package com.Iluwos;

import com.google.gson.annotations.SerializedName;
import java.util.HashMap;
import java.util.Map;

public class ModConfig {
    private Map<String, PresetsData> presets = new HashMap<>();
    private Integer track_timer_ticks;
    private boolean tracking_status;
    private String activePreset;

    public ModConfig() {
        track_timer_ticks = 200;
        tracking_status = true;
        activePreset = "Custom";

        PresetsData defaultPreset = new PresetsData();
        presets.put("AllItems", defaultPreset);
        presets.put("Custom", defaultPreset);
    }

    public Map<String, PresetsData> getPresets() { return presets; }

    public Integer get_track_timer_ticks() { return track_timer_ticks; }
    public void set_track_timer_ticks(Integer key) { this.track_timer_ticks = key; }

    public boolean get_tracking_status() { return tracking_status; }
    public void set_tracking_status(boolean key) { this.tracking_status = key; }

    public String get_activePreset() { return activePreset; }
    public void set_activePreset(String key) { this.activePreset = key; }

    public static class PresetsData {
        private Map<String, Integer> items = new HashMap<>();
        public Map<String, Integer> getItems() { return items; }
    }
}


