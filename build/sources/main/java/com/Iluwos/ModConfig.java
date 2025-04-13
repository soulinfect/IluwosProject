package com.Iluwos;

import com.google.gson.annotations.SerializedName;
import java.util.HashMap;
import java.util.Map;

public class ModConfig {
    private Map<String, Integer> items = new HashMap<>();
    private String track_timer_ticks;
    private boolean tracking_status;

    public ModConfig() {
        track_timer_ticks = "100";
        tracking_status = true;
    }

    public Map<String, Integer> getItems() { return items; }

    public String get_track_timer_ticks() { return track_timer_ticks; }
    public void set_track_timer_ticks(String key) { this.track_timer_ticks = key; }

    public boolean get_tracking_status() { return tracking_status; }
    public void set_tracking_status(boolean key) { this.tracking_status = key; }
}

