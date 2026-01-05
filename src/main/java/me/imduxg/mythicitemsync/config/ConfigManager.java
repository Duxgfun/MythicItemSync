package me.imduxg.mythicitemsync.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class ConfigManager {
    private final JavaPlugin plugin;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
    }

    private FileConfiguration cfg() {
        return plugin.getConfig();
    }

    public String language() {
        return cfg().getString("settings.language", "vi_VN");
    }

    public boolean debug() {
        return cfg().getBoolean("settings.debug", false);
    }

    public boolean onJoinSync() {
        return cfg().getBoolean("sync.on-join", true);
    }

    public long joinDelayTicks() {
        return cfg().getLong("sync.join-delay-ticks", 40L);
    }

    public boolean detectMythicReload() {
        return cfg().getBoolean("sync.detect-mythic-reload-command", true);
    }

    public long reloadDetectDelayTicks() {
        return cfg().getLong("sync.reload-detect-delay-ticks", 40L);
    }

    public boolean onEnableSync() {
        return cfg().getBoolean("sync.on-enable", true);
    }

    // --- container-take ---
    public boolean onContainerTake() {
        return cfg().getBoolean("sync.on-container-take", true);
    }

    public boolean containerTakeScanShulkers() {
        return cfg().getBoolean("sync.container-take-scan-shulkers", true);
    }

    public int containerTakeMaxShulkerDepth() {
        return Math.max(0, cfg().getInt("sync.container-take-max-shulker-depth", 2));
    }

    // --- cooldown ---
    public boolean containerTakeCooldownEnabled() {
        return cfg().getBoolean("sync.container-take-cooldown.enabled", true);
    }

    public long containerTakeCooldownMs() {
        return Math.max(0L, cfg().getLong("sync.container-take-cooldown.time-ms", 800L));
    }

    public boolean containerTakeSkipWhenCooldown() {
        return cfg().getBoolean("sync.container-take-cooldown.skip-when-cooldown", true);
    }

    // --- scan scope ---
    public boolean scanInventory() {
        return cfg().getBoolean("scan.include-inventory", true);
    }

    public boolean scanEnderChest() {
        return cfg().getBoolean("scan.include-enderchest", true);
    }

    public boolean scanArmor() {
        return cfg().getBoolean("scan.include-armor", true);
    }

    public boolean scanOffhand() {
        return cfg().getBoolean("scan.include-offhand", true);
    }

    public boolean scanShulkers() {
        return cfg().getBoolean("scan.scan-shulkers", true);
    }

    public int maxShulkerDepth() {
        return Math.max(0, cfg().getInt("scan.max-shulker-depth", 2));
    }

    // --- performance ---
    public int itemsPerTickPerPlayer() {
        return Math.max(10, cfg().getInt("performance.items-per-tick-per-player", 200));
    }

    public int maxConcurrentPlayers() {
        return Math.max(1, cfg().getInt("performance.max-concurrent-players", 6));
    }

    // --- logging ---
    public boolean logEnabled() {
        return cfg().getBoolean("logging.enabled", true);
    }

    public boolean logContainerTake() {
        return cfg().getBoolean("logging.log-container-take", true);
    }

    public boolean logPlayerSyncFinish() {
        return cfg().getBoolean("logging.log-player-sync-finish", true);
    }
}
