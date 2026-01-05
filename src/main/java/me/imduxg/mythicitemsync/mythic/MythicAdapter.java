package me.imduxg.mythicitemsync.mythic;

import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public interface MythicAdapter {
    boolean isAvailable();

    /**
     * @param mythicTypeId value from key mythicmobs:type (e.g. "SOME_ITEM_ID")
     * @return fresh Mythic item stack if obtainable
     */
    Optional<ItemStack> buildMythicItem(String mythicTypeId);
}
