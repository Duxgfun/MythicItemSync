package me.imduxg.mythicitemsync.lang;

import org.bukkit.ChatColor;

import java.util.List;
import java.util.Map;

public final class Lang {
    private final Map<String, Object> data;

    public Lang(Map<String, Object> data) {
        this.data = data;
    }

    public String getString(String key, String def) {
        Object v = data.get(key);
        if (v == null) return def;
        return String.valueOf(v);
    }

    @SuppressWarnings("unchecked")
    public List<String> getStringList(String key) {
        Object v = data.get(key);
        if (v instanceof List<?> list) {
            return (List<String>) list;
        }
        return List.of();
    }

    public static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s == null ? "" : s);
    }
}
