package me.imduxg.mythicitemsync.command;

import me.imduxg.mythicitemsync.MythicItemSyncPlugin;
import me.imduxg.mythicitemsync.config.ConfigManager;
import me.imduxg.mythicitemsync.lang.LangManager;
import me.imduxg.mythicitemsync.mythic.MythicAdapter;
import me.imduxg.mythicitemsync.sync.ItemSyncEngine;
import me.imduxg.mythicitemsync.util.Perm;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class MISCommand implements CommandExecutor, TabCompleter {

    private final MythicItemSyncPlugin plugin;
    private final ConfigManager config;
    private final LangManager lang;
    private final MythicAdapter mythic;
    private final ItemSyncEngine engine;

    public MISCommand(MythicItemSyncPlugin plugin, ConfigManager config, LangManager lang, MythicAdapter mythic, ItemSyncEngine engine) {
        this.plugin = plugin;
        this.config = config;
        this.lang = lang;
        this.mythic = mythic;
        this.engine = engine;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            for (String line : lang.trList("help")) sender.sendMessage(line);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        switch (sub) {
            case "reload" -> {
                if (!sender.hasPermission(Perm.ADMIN)) {
                    sender.sendMessage(lang.tr("no-permission"));
                    return true;
                }
                plugin.reloadAll();
                sender.sendMessage(lang.tr("reloaded"));
                return true;
            }
            case "status" -> {
                if (!sender.hasPermission(Perm.SYNC) && !sender.hasPermission(Perm.ADMIN)) {
                    sender.sendMessage(lang.tr("no-permission"));
                    return true;
                }
                String mythicStatus = mythic.isAvailable() ? "&aON" : "&cOFF";
                for (String line : lang.trList("status")) {
                    sender.sendMessage(line
                            .replace("{mythic}", org.bukkit.ChatColor.translateAlternateColorCodes('&', mythicStatus))
                            .replace("{queue}", String.valueOf(engine.getQueueSize()))
                            .replace("{running}", String.valueOf(engine.getRunningCount()))
                    );
                }
                return true;
            }
            case "sync" -> {
                if (!sender.hasPermission(Perm.SYNC) && !sender.hasPermission(Perm.ADMIN)) {
                    sender.sendMessage(lang.tr("no-permission"));
                    return true;
                }

                boolean deep = false;
                for (String a : args) {
                    if (a.equalsIgnoreCase("--deep")) deep = true;
                }

                if (args.length >= 2 && args[1].equalsIgnoreCase("all")) {
                    int count = Bukkit.getOnlinePlayers().size();
                    sender.sendMessage(lang.format(lang.tr("sync-start-all"), "count", count));
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        engine.syncNow(p, deep);
                    }
                    return true;
                }

                Player target;
                if (args.length >= 2) {
                    target = Bukkit.getPlayerExact(args[1]);
                    if (target == null) {
                        sender.sendMessage(lang.format(lang.tr("player-not-found"), "player", args[1]));
                        return true;
                    }
                } else {
                    if (!(sender instanceof Player p)) {
                        sender.sendMessage("Usage: /" + label + " sync <player|all> [--deep]");
                        return true;
                    }
                    target = p;
                }

                sender.sendMessage(lang.format(lang.tr("sync-start-one"), "player", target.getName()));
                engine.syncNow(target, deep);
                return true;
            }
        }

        for (String line : lang.trList("help")) sender.sendMessage(line);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            out.add("help");
            out.add("reload");
            out.add("sync");
            out.add("status");
            return filter(out, args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("sync")) {
            out.add("all");
            for (Player p : Bukkit.getOnlinePlayers()) out.add(p.getName());
            return filter(out, args[1]);
        }
        if (args.length >= 2 && args[0].equalsIgnoreCase("sync")) {
            out.add("--deep");
            return filter(out, args[args.length - 1]);
        }
        return List.of();
    }

    private static List<String> filter(List<String> list, String token) {
        if (token == null || token.isBlank()) return list;
        String t = token.toLowerCase(Locale.ROOT);
        return list.stream().filter(s -> s.toLowerCase(Locale.ROOT).startsWith(t)).toList();
    }
}
