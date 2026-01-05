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
```
/mythicitemsync
/msync
/mis
```

### Command List

| Command | Description |
|------|------------|
| `/msync sync <player>` | Sync Mythic items for a specific player |
| `/msync sync all` | Sync Mythic items for all online players |
| `/msync reload` | Reload configuration and language files |
| `/msync status` | View sync queue and running tasks |

---

## 🔐 Permissions

| Permission | Description |
|-----------|------------|
| `mythicitemsync.use` | Access basic commands |
| `mythicitemsync.sync` | Allows manual synchronization |
| `mythicitemsync.reload` | Allows plugin reload |
| `mythicitemsync.admin` | Full access (includes all permissions) |

**Default:**
- OPs: Full access
- Non-OP players: No access

---

## ⚙ Configuration Highlights

- Sync on join / reload / enable
- Container-take sync with cooldown protection
- Shulker scanning with depth limits
- Performance tuning (items per tick, concurrent players)
- File-only logging with timestamps

See `config.yml` for full documentation (EN / VI).

---

## 📄 Logging

All sync actions are logged to:
```
plugins/MythicItemSync/logs/sync.log
```

Example:
```
[2026-01-05 21:14:33] [PLAYER_SYNC] player=Steve scanned=128 updated=12 time=87ms
```

---

## 🧠 Why MythicItemSync?

Unlike basic resync plugins, MythicItemSync focuses on:
- **Data safety** (no enchant/NBT loss)
- **Performance** (no full inventory spam)
- **Clean output** (no console or chat spam)
- **Real gameplay integration** (sync only when needed)

Perfect for **SMP**, **RPG**, and **MythicMobs-heavy servers**.

---

## 📜 License

This project is licensed under the **MIT License**.

---

## 👤 Author

**ImDuxg**

---

## 💬 Support & Feedback

If you encounter bugs, performance issues, or have feature requests,
please open an **Issue** or **Pull Request** on GitHub.
