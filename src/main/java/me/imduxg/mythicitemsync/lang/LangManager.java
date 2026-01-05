package me.imduxg.mythicitemsync.lang;

import me.imduxg.mythicitemsync.config.ConfigManager;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class LangManager {
    private final JavaPlugin plugin;
    private final ConfigManager config;
    private Lang lang;

    public LangManager(JavaPlugin plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void reload() {
        // Ensure defaults exist
        saveIfMissing("lang/vi_VN.yml");
        saveIfMissing("lang/en_US.yml");

        String code = config.language();
        File file = new File(plugin.getDataFolder(), "lang/" + code + ".yml");
        if (!file.exists()) file = new File(plugin.getDataFolder(), "lang/vi_VN.yml");

        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        Map<String, Object> map = new HashMap<>();
        for (String k : yml.getKeys(true)) {
            map.put(k, yml.get(k));
        }
        this.lang = new Lang(map);
    }

    private void saveIfMissing(String path) {
        File out = new File(plugin.getDataFolder(), path);
        if (out.exists()) return;
        out.getParentFile().mkdirs();
        plugin.saveResource(path, false);
    }

    public String tr(String key) {
        String prefix = Lang.color(lang.getString("prefix", "&b[&fMythicItemSync&b]&r "));
        String s = lang.getString(key, key);
        return prefix + Lang.color(s);
    }

    public String trRaw(String key) {
        return Lang.color(lang.getString(key, key));
    }

    public java.util.List<String> trList(String key) {
        return lang.getStringList(key).stream().map(Lang::color).toList();
    }

    public String format(String msg, Object... kv) {
        String out = msg;
        for (int i = 0; i + 1 < kv.length; i += 2) {
            out = out.replace("{" + kv[i] + "}", String.valueOf(kv[i + 1]));
        }
        return out;
    }
}
