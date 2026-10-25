# MC Survival Plugin

A custom survival plugin for Minecraft Paper 26.2 servers.

## Features

- **Custom Mobs**: Shadow Zombie with custom health, attack damage, and loot drops.
- **Custom Items & Abilities**:
  - `Void Slayer` (Diamond Sword) with Void Strike ability.
  - `Shadow Stone` (Netherite Ingot) rare material drop.
- **Custom Traders**: `Void Trader` with configurable custom item trades.
- **Rank & Progression**: Leveling system with XP gained from mining and defeating vanilla/custom mobs.

## Requirements

- **Java**: JDK 25 LTS
- **Paper**: Version 26.2+
- **Gradle**: 8.14+ (Foojay toolchain resolver configured)

## Getting Started

### Build Plugin
```bash
gradle jar
```

### Server Scripts
- `start_server.bat`: Launch the local Paper 26.2 server.
- `update-plugin.bat`: Rebuild the plugin and update `server/plugins/mc-server.jar`.
