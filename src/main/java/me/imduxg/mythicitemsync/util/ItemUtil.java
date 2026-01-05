package me.imduxg.mythicitemsync.util;

import org.bukkit.NamespacedKey;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;
import java.util.Optional;

public final class ItemUtil {
    private ItemUtil() {}

    public static final NamespacedKey MYTHIC_TYPE_KEY = NamespacedKey.fromString("mythicmobs:type");

    public static Optional<String> getMythicTypeId(ItemStack stack) {
        if (stack == null || stack.getType().isAir()) return Optional.empty();
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return Optional.empty();
        if (MYTHIC_TYPE_KEY == null) return Optional.empty();

        String id = meta.getPersistentDataContainer().get(MYTHIC_TYPE_KEY, PersistentDataType.STRING);
        if (id == null || id.isBlank()) return Optional.empty();
        return Optional.of(id);
    }

    public static boolean isShulker(ItemStack stack) {
        if (stack == null || stack.getType().isAir()) return false;
        ItemMeta meta = stack.getItemMeta();
        return meta instanceof BlockStateMeta bsm && bsm.getBlockState() instanceof ShulkerBox;
    }

    public static ShulkerBox getShulkerState(ItemStack stack) {
        if (!isShulker(stack)) return null;
        BlockStateMeta bsm = (BlockStateMeta) Objects.requireNonNull(stack.getItemMeta());
        return (ShulkerBox) bsm.getBlockState();
    }

    public static void saveShulkerState(ItemStack stack, ShulkerBox shulker) {
        if (!isShulker(stack) || shulker == null) return;
        BlockStateMeta bsm = (BlockStateMeta) Objects.requireNonNull(stack.getItemMeta());
        bsm.setBlockState(shulker);
        stack.setItemMeta(bsm);
    }
}
