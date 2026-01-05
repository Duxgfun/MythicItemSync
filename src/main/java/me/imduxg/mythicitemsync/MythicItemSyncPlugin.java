package me.imduxg.mythicitemsync;

import me.imduxg.mythicitemsync.command.MISCommand;
import me.imduxg.mythicitemsync.config.ConfigManager;
import me.imduxg.mythicitemsync.lang.LangManager;
import me.imduxg.mythicitemsync.listener.ContainerTakeListener;
import me.imduxg.mythicitemsync.log.SyncLogger;
import me.imduxg.mythicitemsync.mythic.MythicAdapter;
import me.imduxg.mythicitemsync.mythic.ReflectionMythicAdapter;
import me.imduxg.mythicitemsync.sync.ItemSyncEngine;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;

public final class MythicItemSyncPlugin extends JavaPlugin implements Listener {

    private ConfigManager configManager;
    private LangManager lang;
    private MythicAdapter mythic;
    private ItemSyncEngine syncEngine;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        configManager.reload();

        this.lang = new LangManager(this, configManager);
        lang.reload();

        // init file logger (NO console logging)
        SyncLogger.init(getDataFolder(), configManager.logEnabled());

        this.mythic = new ReflectionMythicAdapter(this);
        this.syncEngine = new ItemSyncEngine(this, configManager, lang, mythic);

        Bukkit.getPluginManager().registerEvents(this, this);

        // register container take listener
        Bukkit.getPluginManager().registerEvents(new ContainerTakeListener(configManager, syncEngine), this);

        PluginCommand cmd = getCommand("mythicitemsync");
        if (cmd != null) {
            MISCommand executor = new MISCommand(this, configManager, lang, mythic, syncEngine);
            cmd.setExecutor(executor);
            cmd.setTabCompleter(executor);
        }

        if (configManager.onEnableSync()) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                syncEngine.queueSync(p, false);
            }
        }
    }

    @Override
    public void onDisable() {
        if (syncEngine != null) syncEngine.shutdown();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (!configManager.onJoinSync()) return;
        Player p = e.getPlayer();
        long delay = configManager.joinDelayTicks();
        Bukkit.getScheduler().runTaskLater(this, () -> syncEngine.queueSync(p, false), delay);
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
        if (!configManager.detectMythicReload()) return;
        if (e.getMessage() == null) return;

        String msg = e.getMessage().trim().toLowerCase(Locale.ROOT);
        if (msg.startsWith("/mythicmobs reload") || msg.startsWith("/mm reload") || msg.startsWith("/mythicmob reload")) {
            long delay = configManager.reloadDetectDelayTicks();

            me.imduxg.mythicitemsync.log.SyncLogger.log("MYTHIC_RELOAD",
                    "Detected player command, syncing online players after " + delay + " ticks");

            Bukkit.getScheduler().runTaskLater(this, () -> {
                for (Player p : Bukkit.getOnlinePlayers()) syncEngine.queueSync(p, false);
            }, delay);
        }
    }

    @EventHandler
    public void onServerCommand(ServerCommandEvent e) {
        if (!configManager.detectMythicReload()) return;
        if (e.getCommand() == null) return;

        String cmd = e.getCommand().trim().toLowerCase(Locale.ROOT);
        if (cmd.startsWith("mythicmobs reload") || cmd.startsWith("mm reload") || cmd.startsWith("mythicmob reload")) {
            long delay = configManager.reloadDetectDelayTicks();

            me.imduxg.mythicitemsync.log.SyncLogger.log("MYTHIC_RELOAD",
                    "Detected console command, syncing online players after " + delay + " ticks");

            Bukkit.getScheduler().runTaskLater(this, () -> {
                for (Player p : Bukkit.getOnlinePlayers()) syncEngine.queueSync(p, false);
            }, delay);
        }
    }

    public void reloadAll() {
        configManager.reload();
        lang.reload();
        SyncLogger.init(getDataFolder(), configManager.logEnabled()); // re-init logger with new config
    }
}
