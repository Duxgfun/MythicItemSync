package me.imduxg.mythicitemsync.sync;

import me.imduxg.mythicitemsync.config.ConfigManager;
import me.imduxg.mythicitemsync.lang.LangManager;
import me.imduxg.mythicitemsync.log.SyncLogger;
import me.imduxg.mythicitemsync.mythic.MythicAdapter;
import me.imduxg.mythicitemsync.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public final class ItemSyncEngine {

    private final org.bukkit.plugin.Plugin plugin;
    private final ConfigManager config;
    private final LangManager lang;
    private final MythicAdapter mythic;

    private final Queue<UUID> queue = new ConcurrentLinkedQueue<>();
    private final Set<UUID> running = Collections.synchronizedSet(new HashSet<>());

    private int taskId = -1;

    public ItemSyncEngine(org.bukkit.plugin.Plugin plugin, ConfigManager config, LangManager lang, MythicAdapter mythic) {
        this.plugin = plugin;
        this.config = config;
        this.lang = lang;
        this.mythic = mythic;

        startTicker();
    }

    public org.bukkit.plugin.Plugin getPlugin() {
        return plugin;
    }

    public void shutdown() {
        if (taskId != -1) Bukkit.getScheduler().cancelTask(taskId);
        taskId = -1;
        queue.clear();
        running.clear();
    }

    public int getQueueSize() { return queue.size(); }
    public int getRunningCount() { return running.size(); }

    public void queueSync(Player player, boolean deepScan) {
        if (player == null) return;
        UUID id = player.getUniqueId();
        if (queue.contains(id) || running.contains(id)) return;
        queue.add(id);
        // no player message, no console log
    }

    public void syncNow(Player player, boolean deepScan) {
        if (player == null) return;
        if (running.size() < config.maxConcurrentPlayers()) {
            runFor(player, new SyncPlan(deepScan));
        } else {
            queueSync(player, deepScan);
        }
    }

    private void startTicker() {
        this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            while (running.size() < config.maxConcurrentPlayers()) {
                UUID next = queue.poll();
                if (next == null) break;

                Player p = Bukkit.getPlayer(next);
                if (p == null || !p.isOnline()) continue;

                runFor(p, new SyncPlan(false));
            }
        }, 1L, 1L);
    }

    private void runFor(Player player, SyncPlan plan) {
        UUID pid = player.getUniqueId();
        if (running.contains(pid)) return;
        running.add(pid);

        final long startTime = System.currentTimeMillis();

        final int perTick = config.itemsPerTickPerPlayer();
        final int maxDepth = plan.deepScan() ? Math.max(config.maxShulkerDepth(), 4) : config.maxShulkerDepth();
        final boolean scanShulkers = config.scanShulkers();

        final List<ItemRef> refs = new ArrayList<>(256);

        PlayerInventory inv = player.getInventory();

        if (config.scanInventory()) {
            ItemStack[] contents = inv.getContents();
            for (int i = 0; i < contents.length; i++) refs.add(ItemRef.playerInv(player, i));
        }

        if (config.scanArmor()) {
            refs.add(ItemRef.armor(player, ArmorSlot.HELMET));
            refs.add(ItemRef.armor(player, ArmorSlot.CHESTPLATE));
            refs.add(ItemRef.armor(player, ArmorSlot.LEGGINGS));
            refs.add(ItemRef.armor(player, ArmorSlot.BOOTS));
        }

        if (config.scanOffhand()) refs.add(ItemRef.offhand(player));

        if (config.scanEnderChest()) {
            Inventory ec = player.getEnderChest();
            for (int i = 0; i < ec.getSize(); i++) refs.add(ItemRef.ender(player, i));
        }

        final int totalRefs = refs.size();
        final int[] idx = {0};
        final int[] scanned = {0};
        final int[] updated = {0};

        final AtomicInteger localTaskId = new AtomicInteger(-1);

        int id = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            int processed = 0;

            while (processed < perTick && idx[0] < totalRefs) {
                ItemRef ref = refs.get(idx[0]++);
                ItemStack stack = ref.get();
                processed++;

                if (stack == null || stack.getType().isAir()) continue;

                scanned[0]++;

                boolean changed = tryUpdateMythicItem(stack);
                if (changed) {
                    updated[0]++;
                    ref.set(stack);
                } else {
                    ref.set(stack);
                }

                if (scanShulkers && ItemUtil.isShulker(stack) && maxDepth > 0) {
                    int changedInside = scanShulker(stack, 1, maxDepth);
                    if (changedInside > 0) {
                        updated[0] += changedInside;
                        ref.set(stack);
                    }
                }
            }

            if (idx[0] >= totalRefs) {
                int tid = localTaskId.get();
                if (tid != -1) Bukkit.getScheduler().cancelTask(tid);

                player.updateInventory();
                running.remove(pid);

                if (config.logEnabled() && config.logPlayerSyncFinish()) {
                    long duration = System.currentTimeMillis() - startTime;
                    SyncLogger.log("PLAYER_SYNC",
                            "player=" + player.getName()
                                    + " scanned=" + scanned[0]
                                    + " updated=" + updated[0]
                                    + " time=" + duration + "ms"
                    );
                }
            }
        }, 1L, 1L);

        localTaskId.set(id);
    }

    /**
     * Update MythicItem base, while preserving:
     * - amount
     * - enchants (unsafe included)
     * - durability/damage
     * - PDC / custom NBT (most plugins store here)
     */
    private boolean tryUpdateMythicItem(ItemStack stack) {
        if (!mythic.isAvailable()) return false;

        var idOpt = ItemUtil.getMythicTypeId(stack);
        if (idOpt.isEmpty()) return false;

        String mythicId = idOpt.get();
        var freshOpt = mythic.buildMythicItem(mythicId);
        if (freshOpt.isEmpty()) return false;

        ItemStack fresh = freshOpt.get();
        if (fresh == null || fresh.getType().isAir()) return false;

        // ---- Preserve from original ----
        int amount = stack.getAmount();

        // Preserve enchants (including unsafe)
        Map<org.bukkit.enchantments.Enchantment, Integer> originalEnchants =
                new HashMap<>(stack.getEnchantments());

        // Preserve damage (durability)
        org.bukkit.inventory.meta.ItemMeta originalMeta = stack.getItemMeta();
        Integer originalDamage = null;
        if (originalMeta instanceof org.bukkit.inventory.meta.Damageable dmg) {
            originalDamage = dmg.getDamage();
        }

        // Preserve PDC
        org.bukkit.persistence.PersistentDataContainer originalPdc =
                (originalMeta != null) ? originalMeta.getPersistentDataContainer() : null;

        // ---- Apply Mythic base (type + meta) ----
        stack.setType(fresh.getType());
        stack.setItemMeta(fresh.getItemMeta());
        stack.setAmount(amount);

        // ---- Restore damage ----
        if (originalDamage != null) {
            org.bukkit.inventory.meta.ItemMeta newMeta = stack.getItemMeta();
            if (newMeta instanceof org.bukkit.inventory.meta.Damageable dmg2) {
                dmg2.setDamage(originalDamage);
                stack.setItemMeta(newMeta);
            }
        }

        // ---- Restore enchants ----
        if (!originalEnchants.isEmpty()) {
            // Clear enchants from Mythic base first (avoid merge conflicts)
            for (org.bukkit.enchantments.Enchantment e : new HashMap<>(stack.getEnchantments()).keySet()) {
                stack.removeEnchantment(e);
            }
            stack.addUnsafeEnchantments(originalEnchants);
        }

        // ---- Restore PDC / custom NBT ----
        if (originalPdc != null) {
            org.bukkit.inventory.meta.ItemMeta newMeta = stack.getItemMeta();
            if (newMeta != null) {
                copyPdcAll(originalPdc, newMeta.getPersistentDataContainer());
                stack.setItemMeta(newMeta);
            }
        }

        return true;
    }

    private int scanShulker(ItemStack shulkerItem, int depth, int maxDepth) {
        if (depth > maxDepth) return 0;

        var shulker = ItemUtil.getShulkerState(shulkerItem);
        if (shulker == null) return 0;

        Inventory inv = shulker.getInventory();
        int updated = 0;

        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack inner = inv.getItem(i);
            if (inner == null || inner.getType().isAir()) continue;

            boolean changed = tryUpdateMythicItem(inner);
            if (changed) {
                updated++;
                inv.setItem(i, inner);
            }

            if (config.scanShulkers() && ItemUtil.isShulker(inner)) {
                updated += scanShulker(inner, depth + 1, maxDepth);
                inv.setItem(i, inner);
            }
        }

        ItemUtil.saveShulkerState(shulkerItem, shulker);
        return updated;
    }

    /**
     * Copy full PersistentDataContainer data.
     * - Preferred: serializeToBytes/readFromBytes (Paper and many forks)
     * - Fallback: copy STRING keys only (best-effort)
     */
    private static void copyPdcAll(org.bukkit.persistence.PersistentDataContainer from,
                                   org.bukkit.persistence.PersistentDataContainer to) {
        if (from == null || to == null) return;

        // Best: copy everything by bytes (via reflection for compatibility)
        try {
            java.lang.reflect.Method ser = from.getClass().getMethod("serializeToBytes");
            byte[] bytes = (byte[]) ser.invoke(from);

            java.lang.reflect.Method read = to.getClass().getMethod("readFromBytes", byte[].class);
            read.invoke(to, bytes);
            return;
        } catch (Throwable ignored) {
            // fallback below
        }

        // Fallback: copy STRING keys only (still preserves many common plugin tags)
        try {
            for (org.bukkit.NamespacedKey key : from.getKeys()) {
                String v = from.get(key, org.bukkit.persistence.PersistentDataType.STRING);
                if (v != null) {
                    to.set(key, org.bukkit.persistence.PersistentDataType.STRING, v);
                }
            }
        } catch (Throwable ignored) {
            // swallow (no console log)
        }
    }

    private enum ArmorSlot { HELMET, CHESTPLATE, LEGGINGS, BOOTS }

    private static final class ItemRef {
        private final Player player;
        private final Kind kind;
        private final int index;
        private final ArmorSlot armorSlot;

        private enum Kind { PLAYER_INV, ENDER, OFFHAND, ARMOR }

        private ItemRef(Player player, Kind kind, int index, ArmorSlot armorSlot) {
            this.player = player;
            this.kind = kind;
            this.index = index;
            this.armorSlot = armorSlot;
        }

        static ItemRef playerInv(Player p, int slot) { return new ItemRef(p, Kind.PLAYER_INV, slot, null); }
        static ItemRef ender(Player p, int slot) { return new ItemRef(p, Kind.ENDER, slot, null); }
        static ItemRef offhand(Player p) { return new ItemRef(p, Kind.OFFHAND, -1, null); }
        static ItemRef armor(Player p, ArmorSlot slot) { return new ItemRef(p, Kind.ARMOR, -1, slot); }

        ItemStack get() {
            PlayerInventory pi = player.getInventory();
            return switch (kind) {
                case PLAYER_INV -> pi.getItem(index);
                case ENDER -> player.getEnderChest().getItem(index);
                case OFFHAND -> pi.getItemInOffHand();
                case ARMOR -> switch (armorSlot) {
                    case HELMET -> pi.getHelmet();
                    case CHESTPLATE -> pi.getChestplate();
                    case LEGGINGS -> pi.getLeggings();
                    case BOOTS -> pi.getBoots();
                };
            };
        }

        void set(ItemStack stack) {
            PlayerInventory pi = player.getInventory();
            switch (kind) {
                case PLAYER_INV -> pi.setItem(index, stack);
                case ENDER -> player.getEnderChest().setItem(index, stack);
                case OFFHAND -> pi.setItemInOffHand(stack);
                case ARMOR -> {
                    switch (armorSlot) {
                        case HELMET -> pi.setHelmet(stack);
                        case CHESTPLATE -> pi.setChestplate(stack);
                        case LEGGINGS -> pi.setLeggings(stack);
                        case BOOTS -> pi.setBoots(stack);
                    }
                }
            }
        }
    }
}
