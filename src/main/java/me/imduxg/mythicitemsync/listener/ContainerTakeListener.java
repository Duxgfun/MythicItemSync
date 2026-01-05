package me.imduxg.mythicitemsync.listener;

import me.imduxg.mythicitemsync.config.ConfigManager;
import me.imduxg.mythicitemsync.log.SyncLogger;
import me.imduxg.mythicitemsync.sync.ItemSyncEngine;
import me.imduxg.mythicitemsync.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ContainerTakeListener implements Listener {

    private final ConfigManager config;
    private final ItemSyncEngine syncEngine;

    private final Map<UUID, Long> takeCooldown = new HashMap<>();

    public ContainerTakeListener(ConfigManager config, ItemSyncEngine syncEngine) {
        this.config = config;
        this.syncEngine = syncEngine;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!config.onContainerTake()) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Inventory clicked = event.getClickedInventory();
        if (clicked == null) return;

        InventoryView view = event.getView();
        Inventory top = view.getTopInventory();

        // Only care clicks in TOP inventory (container side)
        if (!clicked.equals(top)) return;

        InventoryAction action = event.getAction();

        // Actions that can take/move an item out from container
        if (!isTakeAction(action)) return;

        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType().isAir()) return;

        // Only Mythic items
        var mythicId = ItemUtil.getMythicTypeId(item);
        if (mythicId.isEmpty()) return;

        // Cooldown
        if (isOnCooldown(player)) {
            if (!config.containerTakeSkipWhenCooldown()) event.setCancelled(true);
            return;
        }

        // Run 1 tick later so the moved item state is applied
        long t0 = System.currentTimeMillis();
        Bukkit.getScheduler().runTaskLater(syncEngine.getPlugin(), () -> {
            syncEngine.syncNow(player, false);

            long dt = System.currentTimeMillis() - t0;
            if (config.logEnabled() && config.logContainerTake()) {
                SyncLogger.log("CONTAINER_TAKE",
                        "player=" + player.getName()
                                + " mythic=" + mythicId.get()
                                + " action=" + action.name()
                                + " time=" + dt + "ms"
                );
            }
        }, 1L);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!config.onContainerTake()) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        InventoryView view = event.getView();
        int topSize = view.getTopInventory().getSize();

        // If any dragged slot is in player inventory area (raw slot >= topSize)
        boolean intoPlayerInv = event.getRawSlots().stream().anyMatch(raw -> raw >= topSize);
        if (!intoPlayerInv) return;

        ItemStack item = event.getOldCursor();
        if (item == null || item.getType().isAir()) return;

        var mythicId = ItemUtil.getMythicTypeId(item);
        if (mythicId.isEmpty()) return;

        if (isOnCooldown(player)) {
            if (!config.containerTakeSkipWhenCooldown()) event.setCancelled(true);
            return;
        }

        long t0 = System.currentTimeMillis();
        Bukkit.getScheduler().runTaskLater(syncEngine.getPlugin(), () -> {
            syncEngine.syncNow(player, false);

            long dt = System.currentTimeMillis() - t0;
            if (config.logEnabled() && config.logContainerTake()) {
                SyncLogger.log("CONTAINER_DRAG",
                        "player=" + player.getName()
                                + " mythic=" + mythicId.get()
                                + " time=" + dt + "ms"
                );
            }
        }, 1L);
    }

    private boolean isTakeAction(InventoryAction action) {
        return switch (action) {
            case MOVE_TO_OTHER_INVENTORY,    // shift-click
                 PICKUP_ALL, PICKUP_HALF, PICKUP_ONE, PICKUP_SOME, // pickup to cursor
                 HOTBAR_SWAP, HOTBAR_MOVE_AND_READD,               // number key
                 SWAP_WITH_CURSOR                                 // swap cursor
                    -> true;
            default -> false;
        };
    }

    private boolean isOnCooldown(Player player) {
        if (!config.containerTakeCooldownEnabled()) return false;

        long now = System.currentTimeMillis();
        long cooldown = config.containerTakeCooldownMs();

        UUID uuid = player.getUniqueId();
        Long last = takeCooldown.get(uuid);

        if (last != null && (now - last) < cooldown) return true;

        takeCooldown.put(uuid, now);
        return false;
    }
}
