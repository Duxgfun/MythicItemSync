# MythicItemSync

**MythicItemSync** is a lightweight, performance-focused utility plugin that automatically keeps **MythicMobs items synchronized** across player inventories on Paper / Spigot servers.

It is designed to safely update Mythic items after reloads and during gameplay actions **without data loss, lag, console spam, or player chat spam**.

---

## ✨ Features

- 🔄 **Automatic Mythic Item Synchronization**
  - Syncs Mythic items when:
    - Players join the server
    - MythicMobs is reloaded (player or console)
    - The plugin is enabled
    - Players take Mythic items from containers

- 📦 **Container Take Sync**
  - Syncs items when taken from:
    - Chests
    - Shulker boxes (including nested shulkers)
    - Other container inventories
  - Supports shift-click, drag, hotbar swap, and normal pickup

- ⏱ **Anti-Spam Cooldown**
  - Per-player cooldown prevents shift-click spam
  - Skips sync silently (no cancelled clicks, no conflicts)

- 🛡 **Safe Item Preservation**
  - Preserves all important item data during sync:
    - Enchantments (including unsafe)
    - Durability / damage
    - Custom NBT / PersistentDataContainer data
  - Only the Mythic base item is refreshed

- 🚀 **High Performance Sync Engine**
  - Queue-based processing
  - Tick-safe item scanning
  - Configurable items-per-tick and concurrent sync limits

- 📦 **Advanced Shulker Support**
  - Scans shulker boxes and nested shulkers
  - Configurable maximum depth to prevent abuse and lag

- 📄 **Dedicated File Logging**
  - All sync activity is written to `plugins/MythicItemSync/logs/sync.log`
  - Includes timestamps and execution time
  - No console logging, no player messages

- 🌐 **Fully Configurable**
  - All behavior controlled via `config.yml`
  - Bilingual EN / VI comments
  - Reloadable without restarting the server

---

## 🧩 Requirements

- **Minecraft:** 1.21+
- **Server:** Paper or Spigot
- **Dependency:** MythicMobs

---

## 📥 Installation

1. Download the latest release from **Releases**
2. Place `MythicItemSync.jar` into your `plugins/` folder
3. Start the server
4. Configure settings in `plugins/MythicItemSync/config.yml`
5. (Optional) Reload with `/mis reload`

---

## 🧪 Commands

