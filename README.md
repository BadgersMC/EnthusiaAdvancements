# EnthusiaAdvancements

Config-driven custom advancement trees for Paper 1.21.1. Define advancement trees in HOCON config files — no code required. Supports progression tracking, rewards, and optional LumaGuilds integration.

## Requirements

- Paper 1.21.1+
- Java 21+
- [UltimateAdvancementAPI 3.0.0-beta](https://github.com/frengor/UltimateAdvancementAPI) (required)
- [LumaGuilds](https://github.com/BadgersMC/LumaGuilds) (optional — for guild-based advancements)

## Installation

1. Drop `EnthusiaAdvancements-1.0.0-SNAPSHOT.jar` into your `plugins/` folder
2. Ensure UltimateAdvancementAPI is also installed
3. Start the server — default `combat` and `exploration` trees are generated automatically

## Commands

| Command | Permission | Description |
|---------|-----------|-------------|
| `/advancements list` | — | List all loaded advancement trees |
| `/advancements reload` | `advancements.admin` | Hot-reload all tree configs |
| `/advancements grant <player> <tree> <key>` | `advancements.admin` | Grant an advancement to a player |

## Creating Advancement Trees

Trees are defined as `.conf` files (HOCON format) in the `plugins/EnthusiaAdvancements/trees/` directory. Each file defines one tree. Drop in a new file and run `/advancements reload`.

### Tree Structure

```hocon
namespace = "my_tree"
background-texture = "minecraft:textures/block/stone.png"

nodes = [
    {
        // Root node (no parent) — every tree needs exactly one
        key = "root"
        title = "My Tree"
        description = ["The beginning of your journey"]
        icon = "DIAMOND_SWORD"
        frame = "TASK"
        x = 0.0
        y = 0.0
        max-progression = 1
    },
    {
        // Child node — references parent by key
        key = "first_challenge"
        parent = "root"
        title = "First Challenge"
        description = ["Kill 10 zombies"]
        icon = "ROTTEN_FLESH"
        frame = "TASK"
        x = 1.0
        y = 0.0
        max-progression = 10
        requirement = {
            type = "ENTITY_KILL"
            target = "ZOMBIE"
            amount = 10
        }
        rewards = [
            { type = "experience", amount = 100 },
            { type = "command", command = "give %player% diamond 1" },
            { type = "message", text = "<gold>Well done!" }
        ]
    }
]
```

### Node Fields

| Field | Required | Description |
|-------|----------|-------------|
| `key` | Yes | Unique identifier within the tree |
| `title` | Yes | Display name shown to players |
| `description` | Yes | List of description lines |
| `icon` | Yes | Bukkit Material name (e.g. `DIAMOND_SWORD`) |
| `frame` | Yes | `TASK`, `GOAL`, or `CHALLENGE` — controls border style |
| `x`, `y` | Yes | Position in the advancement GUI |
| `max-progression` | Yes | Number of times the requirement must be met (1 = instant) |
| `parent` | No | Key of the parent node. Omit for the root node |
| `requirement` | No | Auto-tracking trigger (see below) |
| `rewards` | No | List of rewards granted on completion |
| `show-toast` | No | Show popup notification (default: `true`) |
| `announce-chat` | No | Announce in chat (default: `true`) |

### Requirement Types

Requirements define what the plugin automatically tracks for progression.

| Type | Target | Example |
|------|--------|---------|
| `BLOCK_BREAK` | Material name | `{ type = "BLOCK_BREAK", target = "DIAMOND_ORE", amount = 10 }` |
| `BLOCK_PLACE` | Material name | `{ type = "BLOCK_PLACE", target = "OAK_PLANKS", amount = 50 }` |
| `ENTITY_KILL` | Entity type | `{ type = "ENTITY_KILL", target = "ZOMBIE", amount = 10 }` |
| `CRAFT_ITEM` | Material name | `{ type = "CRAFT_ITEM", target = "DIAMOND_PICKAXE", amount = 1 }` |
| `PLAYER_KILL` | — | `{ type = "PLAYER_KILL", amount = 5 }` |
| `PLAYER_JOIN` | — | `{ type = "PLAYER_JOIN", amount = 1 }` |
| `GUILD_CREATE` | — | `{ type = "GUILD_CREATE", amount = 1 }` (requires LumaGuilds) |
| `GUILD_LEVEL` | — | `{ type = "GUILD_LEVEL", amount = 5 }` (requires LumaGuilds) |
| `GUILD_MEMBER_JOIN` | — | `{ type = "GUILD_MEMBER_JOIN", amount = 3 }` (requires LumaGuilds) |

### Reward Types

| Type | Fields | Example |
|------|--------|---------|
| `command` | `command` — server command (`%player%` is replaced with player name) | `{ type = "command", command = "give %player% diamond 5" }` |
| `experience` | `amount` — XP points | `{ type = "experience", amount = 500 }` |
| `item` | `material`, `amount` (default 1) | `{ type = "item", material = "DIAMOND", amount = 3 }` |
| `message` | `text` — MiniMessage formatted | `{ type = "message", text = "<gold>Congratulations!" }` |

### Validation Rules

The plugin validates every tree on load. A tree will be skipped with a console warning if:

- It has **no root node** (a node without a `parent`)
- It has **more than one root node**
- A node references a `parent` key that doesn't exist
- The tree contains a **cycle** (node A -> B -> A)

### Frame Types

- **TASK** — Default square border. Use for standard milestones.
- **GOAL** — Rounded border. Use for medium-difficulty goals.
- **CHALLENGE** — Spiky border. Use for hard or endgame achievements.

## LumaGuilds Integration

If [LumaGuilds](https://github.com/BadgersMC/LumaGuilds) is installed, three additional requirement types become available:

- `GUILD_CREATE` — Triggered when a player creates a guild
- `GUILD_MEMBER_JOIN` — Triggered when a player joins a guild
- `GUILD_LEVEL` — Triggered on guild level-up

The integration is fully automatic — no configuration needed. If LumaGuilds is present on the server, the hooks activate. If it's absent, they're silently skipped with no errors.

### Example: Guild Advancement Tree

```hocon
namespace = "guilds"
background-texture = "minecraft:textures/block/oak_planks.png"

nodes = [
    {
        key = "guild_root"
        title = "Guilds"
        description = ["Join a guild and rise through the ranks"]
        icon = "SHIELD"
        frame = "TASK"
        x = 0.0
        y = 0.0
        max-progression = 1
    },
    {
        key = "guild_founder"
        parent = "guild_root"
        title = "Guild Founder"
        description = ["Create your own guild"]
        icon = "GOLDEN_HELMET"
        frame = "GOAL"
        x = 1.0
        y = 0.0
        max-progression = 1
        requirement = {
            type = "GUILD_CREATE"
            amount = 1
        }
        rewards = [
            { type = "experience", amount = 300 },
            { type = "message", text = "<gold>Your guild has been founded!" }
        ]
    },
    {
        key = "guild_recruiter"
        parent = "guild_root"
        title = "Guild Recruiter"
        description = ["Have 5 members join your guild"]
        icon = "PLAYER_HEAD"
        frame = "GOAL"
        x = 1.0
        y = 1.0
        max-progression = 5
        requirement = {
            type = "GUILD_MEMBER_JOIN"
            amount = 5
        }
    }
]
```

## Configuration

The main config is generated at `plugins/EnthusiaAdvancements/advancements.conf`:

```hocon
# Enable debug logging
debug = false

# Directory containing tree config files (relative to plugin data folder)
treesDirectory = "trees"
```

## Building from Source

```bash
./gradlew build
```

The shaded JAR is output to `build/libs/EnthusiaAdvancements-1.0.0-SNAPSHOT.jar`.
