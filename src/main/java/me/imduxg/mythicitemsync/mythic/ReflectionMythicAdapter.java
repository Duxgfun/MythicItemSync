package me.imduxg.mythicitemsync.mythic;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.Optional;

public final class ReflectionMythicAdapter implements MythicAdapter {

    private final JavaPlugin plugin;

    public ReflectionMythicAdapter(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isAvailable() {
        Plugin mm = Bukkit.getPluginManager().getPlugin("MythicMobs");
        return mm != null && mm.isEnabled();
    }

    @Override
    public Optional<ItemStack> buildMythicItem(String mythicTypeId) {
        if (mythicTypeId == null || mythicTypeId.isBlank()) return Optional.empty();
        if (!isAvailable()) return Optional.empty();

        try {
            // MythicBukkit.inst()
            Class<?> mythicBukkitClz = Class.forName("io.lumine.mythic.bukkit.MythicBukkit");
            Method inst = mythicBukkitClz.getMethod("inst");
            Object mythicBukkit = inst.invoke(null);

            // getItemManager()
            Method getItemManager = mythicBukkitClz.getMethod("getItemManager");
            Object itemManager = getItemManager.invoke(mythicBukkit);

            // Try common methods:
            // - getItemStack(String)
            // - getItemStack(String, int)
            // - getItem(String) ... then .toItemStack()
            try {
                Method m = itemManager.getClass().getMethod("getItemStack", String.class);
                Object res = m.invoke(itemManager, mythicTypeId);
                if (res instanceof ItemStack is) return Optional.of(is);
            } catch (NoSuchMethodException ignored) {}

            try {
                Method m = itemManager.getClass().getMethod("getItemStack", String.class, int.class);
                Object res = m.invoke(itemManager, mythicTypeId, 1);
                if (res instanceof ItemStack is) return Optional.of(is);
            } catch (NoSuchMethodException ignored) {}

            // Fallback: getItem(String) -> Optional-like, or MythicItem object
            try {
                Method m = itemManager.getClass().getMethod("getItem", String.class);
                Object res = m.invoke(itemManager, mythicTypeId);
                if (res != null) {
                    // try toItemStack()
                    try {
                        Method toItemStack = res.getClass().getMethod("toItemStack");
                        Object is = toItemStack.invoke(res);
                        if (is instanceof ItemStack itemStack) return Optional.of(itemStack);
                    } catch (NoSuchMethodException ignored2) {}
                }
            } catch (NoSuchMethodException ignored) {}

        } catch (Throwable t) {
            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                plugin.getLogger().warning("Mythic reflection failed: " + t.getClass().getSimpleName() + ": " + t.getMessage());
            }
        }
        return Optional.empty();
    }
}
