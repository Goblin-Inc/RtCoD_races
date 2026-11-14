<div align="center">

🌐 **Language:** **English** | [Русский (README_Ru.md)](README_Ru.md)

# ⚔️ RtCoD Races (`rtcod_races`)

**Full-scale RPG mod adding 20 playable races, true 3D player model & hitbox scaling, passive auras, and active combat abilities for Minecraft Forge 1.20.1**

[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-62B47A?style=for-the-badge&logo=minecraft&logoColor=white)](https://files.minecraftforge.net/)
[![Forge](https://img.shields.io/badge/Forge-47.4.13-DFA86A?style=for-the-badge)](https://files.minecraftforge.net/)
[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://adoptium.net/)
[![Release](https://img.shields.io/github/v/release/Goblin-Inc/RtCoD_races?style=for-the-badge&color=a855f7)](https://github.com/Goblin-Inc/RtCoD_races/releases)

<br/>

![RtCoD Races GUI Preview](assets/screenshots/rtcod_races_preview.png)

</div>

---

## 📖 Overview

**RtCoD Races** is a custom client-server RPG mod developed for **RtCoD (Return to Chronicles of Darkness)** on **Minecraft Forge 1.20.1**, overhauling character creation and progression.

When joining a server for the first time, players are greeted by an interactive graphical menu (`RaceSelectionScreen`) featuring custom 16×16 pixel-art race icons, stat breakdowns, passive trait lists, and active skill descriptions.

### ✨ Key Features
- **20 Playable Fantasy Races**: ranging from classic Humans, Elves, and Dwarves to Vampires, Demons, Celestials, Dragonborn, Automatons, and Beastfolk (Bastet, Lycanthrope, Kitsune, Minotaur, Arachnid).
- **True 3D Character Scaling (`0.75x` – `1.30x`)**:
  - Visual player model scaling via `RenderPlayerEvent.Pre` (`PoseStack.scale`).
  - Physical hitbox and eye-height (`EyeHeight`) adjustment via `EntityEvent.Size` (Dwarves, Goblins, and Halflings can walk through 1.5-block gaps, while Goliaths, Orcs, and Minotaurs tower over the battlefield).
- **Active Racial Abilities on `[R]`**: dashes, shadow blinks, dragon breath, vampiric bite, spider leap, berserker rage, illusory decoy, stone bastion, and more.
- **`Iron's Spells 'n Spellbooks` Integration**: soft-dependency attribute bonuses for `max_mana`, `mana_regen`, and `spell_power` on spellcasting races.
- **Persistent Forge Capabilities**: chosen race data is stored in player NBT, automatically preserved across deaths (`PlayerEvent.Clone`), and synced to all nearby clients (`PacketHandler`).

---

## 🧬 All 20 Playable Races

| # | Race (`ID`) | ❤️ Health | ⚡ Speed | 🗡️ Attack | 🛡️ Armor | 📏 Scale | 🌟 Passive Traits | 🔥 Active Ability `[R]` |
|:-:|:---|:---:|:---:|:---:|:---:|:---:|:---|:---|
| 1 | **Human** (`human`) | `20 HP` | `100%` | `100%` | `0` | `1.00x` | +25% XP gain, +2 Luck | **Inspiration** *(60s CD)* — Grants Regeneration II and Speed I to allies within 8 blocks |
| 2 | **High Elf** (`high_elf`) | `18 HP` | `108%` | `95%` | `0` | `1.06x` | +40 Max Mana, +20% mana regen, Night Vision | **Astral Step** *(25s CD)* — Instant 10-block blink along look vector |
| 3 | **Wood Elf** (`wood_elf`) | `18 HP` | `115%` | `100%` | `0` | `1.03x` | +25% bow damage, silent step in forests | **Gale Volley** *(30s CD)* — Backflip leap + Invisibility and Speed II for 6s |
| 4 | **Drow / Dark Elf** (`drow`) | `18 HP` | `110%` | `105%` | `0` | `1.02x` | Night Vision, Blindness immunity, weakened in direct sunlight | **Shroud of Darkness** *(40s CD)* — Blinds enemies within 6 blocks and grants Invisibility |
| 5 | **Dwarf** (`dwarf`) | `26 HP` | `90%` | `110%` | `+4` | `0.82x` | Haste II underground, +30% Knockback Resistance | **Stone Bastion** *(75s CD)* — Resistance III and Fire Resistance for 10s |
| 6 | **Orc** (`orc`) | `28 HP` | `98%` | `125%` | `+2` | `1.18x` | Heavy melee damage bonus, faster hunger depletion | **Blood Roar** *(60s CD)* — Strength II and Speed II for 12s, knocks back nearby foes |
| 7 | **Goblin** (`goblin`) | `16 HP` | `122%` | `90%` | `0` | `0.75x` | Fits through 1.5-block gaps, +20% attack speed | **Dirty Trick** *(25s CD)* — Forward dash + inflicts Poison and Slowness on target |
| 8 | **Demon** (`demon`) | `24 HP` | `105%` | `115%` | `+2` | `1.12x` | Complete Fire & Lava immunity, takes damage from water/rain | **Hellfire Ring** *(45s CD)* — Fiery blast around the player, igniting enemies for 8s |
| 9 | **Vampire** (`vampire`) | `22 HP` | `112%` | `110%` | `0` | `1.02x` | Lifesteal (15% of melee damage at night), burns in sunlight without helmet | **Blood Bite** *(30s CD)* — Drains 6 HP from a nearby target and inflicts Weakness |
| 10 | **Undead** (`undead`) | `24 HP` | `95%` | `105%` | `+2` | `1.00x` | Cannot drown, immune to Poison & Hunger, burns in daylight | **Grave Chill** *(45s CD)* — Freezes and withers enemies within 5 blocks |
| 11 | **Lycanthrope** (`lycanthrope`) | `26 HP` | `115%` | `120%` | `+2` | `1.15x` | Gains Strength I & Regeneration I at night, jump boost | **Feral Pounce** *(20s CD)* — Long predator leap inflicting AoE bleed |
| 12 | **Dragonborn** (`dragonborn`) | `26 HP` | `100%` | `115%` | `+4` | `1.16x` | Natural scales (+4 Armor), Fire Resistance, +10% spell power | **Dragon Breath** *(40s CD)* — Cone of flame in front of the player (true + fire damage) |
| 13 | **Bastet Beastfolk** (`bastet`) | `18 HP` | `120%` | `105%` | `0` | `0.95x` | Complete fall damage immunity, Night Vision | **Feline Reflexes** *(30s CD)* — Evasion dash + Speed III and Jump Boost III for 8s |
| 14 | **Kitsune** (`kitsune`) | `18 HP` | `114%` | `95%` | `0` | `0.96x` | +35 Max Mana, +15% spell power, foxfire aura | **Foxfire Orbs** *(35s CD)* — Illusory dash + homing magical wisp projectiles |
| 15 | **Goliath** (`goliath`) | `32 HP` | `92%` | `120%` | `+4` | `1.28x` | Freeze immunity, +50% Knockback Resistance | **Titan Slam** *(50s CD)* — Ground smash launching all surrounding enemies into the air |
| 16 | **Automaton** (`automaton`) | `28 HP` | `90%` | `110%` | `+6` | `1.08x` | Does not require air, immune to Poison and Wither | **Core Overdrive** *(60s CD)* — Electric shock shield + Speed II and Haste II |
| 17 | **Celestial** (`celestial`) | `20 HP` | `105%` | `100%` | `+2` | `1.08x` | Slow falling while sneaking, holy magic bonus | **Blessing of Light** *(60s CD)* — Heals 8 HP and grants Absorption hearts to self and allies |
| 18 | **Arachnid** (`arachnid`) | `20 HP` | `110%` | `105%` | `+1` | `1.04x` | Climbs vertical walls, immune to cobwebs and poison | **Weaver's Cocoon** *(30s CD)* — Entangles target in cobwebs and applies Poison |
| 19 | **Halfling** (`halfling`) | `16 HP` | `112%` | `90%` | `0` | `0.78x` | +3 Luck, stealthy, reduced mob detection range | **Lucky Break** *(45s CD)* — Invisibility + Absorption II + cleanses negative effects |
| 20 | **Minotaur** (`minotaur`) | `30 HP` | `102%` | `125%` | `+3` | `1.30x` | Disables shields while sprinting, crushing melee strikes | **Labyrinth Charge** *(35s CD)* — Unstoppable bull rush that stuns and knocks back foes |

---

## 🎮 Controls & Commands

| Action / Command | Description | Permission |
|---|---|---|
| Key **`[R]`** *(configurable in Controls)* | Trigger racial active ability with HUD cooldown indicator | All players |
| `/race` | View current race stats (or open race selection screen if none chosen) | All players |
| `/race gui` | Force-open the race selection GUI | OP (Level 2+) |
| `/race set <player> <race_id>` | Assign a race to a player and immediately recalculate attributes & 3D scale | OP (Level 2+) |
| `/race reset <player>` | Reset a player's race and reopen their initial selection screen | OP (Level 2+) |
| `/race info [player]` | Print detailed statistics of a player's current race in chat | All players |

---

## 🏗️ Project Architecture

```
RtCoD_races/
├── src/main/java/com/rtcod/races/
│   ├── RtCoDRacesMod.java          # @Mod("rtcod_races") entry point and event bus registration
│   ├── race/
│   │   ├── Race.java               # Enum/registry of all 20 races: attributes, scale, icons, lore
│   │   ├── RaceAttributes.java     # AttributeModifier management (HP, Speed, Armor, Attack, Mana)
│   │   └── RaceAbilityHandler.java # Server-side execution and cooldown tracking for all 20 active skills
│   ├── capability/
│   │   ├── PlayerRaceData.java     # NBT serialization of chosen race and ability cooldowns
│   │   └── RaceCapability.java     # Forge Capability registration and respawn cloning
│   ├── client/
│   │   ├── RaceSelectionScreen.java# Fullscreen race selection GUI with 16x16 icons and scrolling
│   │   ├── ClientEvents.java       # Player model scale rendering (RenderPlayerEvent) + HUD overlay
│   │   └── KeyBindings.java        # Keybinding registration for [R] (key.rtcod_races.ability)
│   ├── event/
│   │   └── CommonEvents.java       # EntityEvent.Size (3D hitbox), sunlight burn, and passive auras
│   ├── command/
│   │   └── RaceCommand.java        # Brigadier commands (/race set, /race reset, /race gui)
│   └── network/
│       ├── PacketHandler.java      # SimpleChannel network protocol
│       ├── SelectRacePacket.java   # C2S packet sent when choosing a race in GUI
│       ├── UseAbilityPacket.java   # C2S packet sent on pressing [R]
│       └── SyncRacePacket.java     # S2C race and 3D scale synchronization across tracking clients
└── src/main/resources/
    ├── META-INF/mods.toml
    └── assets/rtcod_races/
        ├── lang/ru_ru.json, en_us.json
        └── textures/gui/races/     # 20 custom 16x16 pixel-art race icons
```

---

## 🚀 Installation & Build

### Prebuilt `.jar` from Releases
1. Go to [**Releases**](https://github.com/Goblin-Inc/RtCoD_races/releases) and download `rtcod_races-1.2.0.jar`.
2. Drop the file into the `mods/` folder on **both the server and client** (Forge `1.20.1 - 47.4.13+`).

### Build from Source
```bash
git clone https://github.com/Goblin-Inc/RtCoD_races.git
cd RtCoD_races
./gradlew build
# Output jar: build/libs/rtcod_races-1.2.0.jar
```

---

## 📋 Changelog

### `v1.2.0` — Full 20-Race Roster & 3D Scaling Release *(Current)*
- **Added**: Expanded the playable roster to **20 unique archetypes** (`bastet`, `kitsune`, `goliath`, `automaton`, `celestial`, `arachnid`, `halfling`, `minotaur`).
- **Added**: Custom 16×16 pixel-art icons (`assets/rtcod_races/textures/gui/races/*.png`) for every race in `RaceSelectionScreen`.
- **Improved**: Live `EntityEvent.Size` and `refreshDimensions()` synchronization on race change without relogging.
- **Improved**: Soft integration with `Iron's Spells 'n Spellbooks` attributes (`max_mana`, `mana_regen`, `spell_power`).

### `v1.1.0` — Active Abilities & Multiplayer Sync
- **Added**: `[R]` ability key (`UseAbilityPacket`) and server-side cooldown manager.
- **Added**: Multiplayer 3D scale and race synchronization for tracked players (`PlayerEvent.StartTracking`).
- **Fixed**: Preserving chosen race across death and End dimension travel (`PlayerEvent.Clone`).

### `v1.0.0` — Initial Prototype
- Core Forge Capability implementation, 12 base races, health/speed modifiers, and `/race` command.

---

<div align="center">
Designed for the <b>RtCoD</b> server • Author: <a href="https://github.com/Goblin-Inc">@Goblin-Inc</a> • <a href="README_Ru.md">Русская версия</a>
</div>
