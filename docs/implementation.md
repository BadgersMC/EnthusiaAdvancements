# Implementation Blueprint — CustomAdvancements Plugin

> Version: 1.0 | Last Updated: 2026-04-02
> Related: `tech-stack.md` (what we build with), `requirements.md` (what we build), `tasks.md` (how we build it)

---

## How to Read This Document

This is the **architectural blueprint**. It shows how every layer connects, what each file does, and how data flows from a config file on disk to a rendered advancement in Minecraft's GUI. If `requirements.md` is the "what", this is the "how".

---

## 1. Package Layout

```
src/main/kotlin/com/badgersmc/advancements/
|
|-- CustomAdvancementsPlugin.kt          [BOOTSTRAP]
|
|-- domain/                              [LAYER 1: Pure Kotlin]
|   |-- AdvancementNodeDef.kt
|   |-- TreeDef.kt
|   |-- Requirement.kt
|   |-- Reward.kt
|
|-- application/                         [LAYER 2: Use Cases + Ports]
|   |-- ports/
|   |   |-- AdvancementRegistry.kt       (interface)
|   |   |-- RewardExecutor.kt            (interface)
|   |   |-- PluginHook.kt                (interface)
|   |-- actions/
|       |-- BuildTree.kt
|       |-- GrantProgress.kt
|       |-- ReloadTrees.kt
|
|-- infrastructure/                      [LAYER 3: Framework Adapters]
|   |-- advancement/
|   |   |-- RewardableRootAdvancement.kt
|   |   |-- RewardableAdvancement.kt
|   |   |-- UltimateAdvancementAdapter.kt
|   |-- rewards/
|   |   |-- BukkitRewardExecutor.kt
|   |-- listeners/
|   |   |-- EntityKillListener.kt
|   |   |-- BlockBreakListener.kt
|   |   |-- CraftItemListener.kt
|   |   |-- PlayerJoinListener.kt
|   |-- plugins/
|       |-- LumaGuildsHook.kt
|
|-- config/
|   |-- AdvancementsConfig.kt
|   |-- TreeConfigParser.kt
|
|-- commands/
    |-- AdvancementCommand.kt

src/test/kotlin/com/badgersmc/advancements/
|-- domain/
|   |-- TreeDefTest.kt
|   |-- RewardTest.kt
|-- application/actions/
|   |-- BuildTreeTest.kt
|   |-- GrantProgressTest.kt
|-- config/
    |-- TreeConfigParserTest.kt

src/main/resources/
|-- paper-plugin.yml
|-- trees/
    |-- combat.conf                      (bundled default)
    |-- exploration.conf                 (bundled default)
```

---

## 2. Layer Dependency Rules

```
  domain  <--  application  <--  infrastructure
  (pure)      (ports/actions)    (adapters/framework)

  - domain imports NOTHING from application or infrastructure
  - application imports domain only (+ port interfaces it defines)
  - infrastructure imports domain + application ports + frameworks (Nexus, UAAPI, Bukkit)
  - bootstrap (Plugin.kt) wires everything via Nexus
```

**Enforced by:** No `import com.fren_gor`, `import org.bukkit`, or `import net.badgersmc.nexus` in `domain/` files. No `import com.fren_gor` in `application/` files.

---

## 3. Data Flow: Config File to Advancement GUI

```
 [trees/combat.conf]
       |
       v
 TreeConfigParser.parseAll()          -- reads HOCON, produces List<TreeDef>
       |
       v
 TreeDef.validate()                   -- single root, valid parents, no cycles
 TreeDef.sortedNodes()                -- topological sort (Kahn's algorithm)
       |
       v
 BuildTree.execute(treeDef, api, rewardExecutor)
       |
       |-- api.createAdvancementTab(namespace, bgTexture)     --> AdvancementTab
       |-- RewardableRootAdvancement(tab, key, maxProg, display)
       |-- RewardableAdvancement(parent, key, maxProg, display)  x N children
       |-- tab.registerAdvancements(root, children)
       |-- tab.automaticallyShowToPlayers()
       |-- tab.automaticallyGrantRootAdvancement()
       |
       v
 UltimateAdvancementAdapter stores:
   tabs[namespace] = tab
   advancementMap[namespace][key] = advancement
   requirementIndex[type][target] = [(namespace, key), ...]
       |
       v
 Player opens advancement GUI (L key) --> UAAPI renders tabs + trees
```

---

## 4. Data Flow: Bukkit Event to Progression Increment

```
 [Player kills a Zombie]
       |
       v
 EntityKillListener.onEntityDeath(event)
       |
       |-- killer = event.entity.killer
       |-- entityType = event.entity.type.name    --> "ZOMBIE"
       |
       v
 registry.findByRequirement(ENTITY_KILL, "ZOMBIE")
       |
       |-- returns [(namespace="combat", key="kill_zombies")]
       |
       v
 registry.getAdvancement("combat", "kill_zombies")
       |
       v
 advancement.incrementProgression(killer)          --> CompletableFuture
       |
       |-- UAAPI updates DB asynchronously
       |-- If maxProgression reached:
       |     |-- fires AdvancementGrantEvent
       |     |-- calls onGrant(player, giveRewards=true)
       |     |-- calls giveReward(player)
       |           |
       |           v
       |     RewardableAdvancement.giveReward(player)
       |           |-- rewardExecutor.execute(player, rewards)
       |           |     |-- Reward.Command -> Bukkit.dispatchCommand()
       |           |     |-- Reward.Experience -> player.giveExp()
       |           |     |-- Reward.Item -> player.inventory.addItem()
       |           |     |-- Reward.Message -> player.sendMessage()
       |           |
       |-- UAAPI sends toast notification
       |-- UAAPI sends chat announcement
       |-- UAAPI updates advancement GUI for team
```

---

## 5. Key Class Details

### 5a. Domain Layer

**`TreeDef`** — The validated, parsed representation of a tree config file.
- `validate(): List<String>` — returns error messages (empty = valid)
- `sortedNodes(): List<AdvancementNodeDef>` — Kahn's topological sort
- `root: AdvancementNodeDef` — the single root node (`parentKey == null`)

**`AdvancementNodeDef`** — One node in a tree. Pure data class.
- Links to parent via `parentKey: String?` (null = root)
- Contains display info (title, description, icon, frame, coords)
- Contains optional `requirement: Requirement?` and `rewards: List<Reward>`

**`Reward`** — Sealed class. Four variants: `Command`, `Experience`, `Item`, `Message`.

**`Requirement`** — Data class. `type: RequirementType` + `target: String?`.

### 5b. Application Layer

**`AdvancementRegistry` (port)** — How the rest of the system queries advancements:
```kotlin
interface AdvancementRegistry {
    fun getAdvancement(treeNamespace: String, key: String): Advancement?
    fun getTreeNamespaces(): Set<String>
    fun isTreeLoaded(namespace: String): Boolean
    fun findByRequirement(type: RequirementType, target: String?): List<Pair<String, String>>
}
```
Implemented by `UltimateAdvancementAdapter`.

**`RewardExecutor` (port)** — How rewards are executed:
```kotlin
interface RewardExecutor {
    fun execute(player: Player, rewards: List<Reward>)
}
```
Implemented by `BukkitRewardExecutor`.

**`BuildTree` (action)** — Stateless function that takes a `TreeDef` + UAAPI instance + `RewardExecutor` and returns a built `AdvancementTab` + advancement map. Core logic:
1. Create tab
2. Sort nodes topologically
3. Build root advancement
4. Build child advancements (maintaining a `Map<String, Advancement>` for parent lookup)
5. Register with tab
6. Return results

**`GrantProgress` (action)** — Takes `(registry, type, target, player)`, finds matching advancements, increments progression.

**`ReloadTrees` (action)** — Orchestrates unregister -> re-parse -> rebuild.

### 5c. Infrastructure Layer

**`RewardableAdvancement`** — Extends `BaseAdvancement` (non-sealed in 3.0.0):
```kotlin
class RewardableAdvancement(
    parent: Advancement,              // UAAPI Advancement (parent node)
    key: String,
    maxProgression: Int,
    display: AbstractAdvancementDisplay,
    private val rewards: List<Reward>,
    private val rewardExecutor: RewardExecutor
) : BaseAdvancement(parent, key, maxProgression, display) {
    override fun giveReward(player: Player) {
        rewardExecutor.execute(player, rewards)
    }
}
```

**`RewardableRootAdvancement`** — Same pattern, extends `RootAdvancement`.

**`UltimateAdvancementAdapter` (`@Service`)** — The central bridge:
- Implements `AdvancementRegistry` port
- Holds the UAAPI instance, all tabs, all advancement maps, requirement index
- `@PostConstruct` calls `loadAllTrees()`
- `reloadAll()` called by reload command

**`BukkitRewardExecutor` (`@Service`)** — Implements `RewardExecutor`:
- Pattern-matches on `Reward` sealed class variants
- Executes Bukkit-specific actions (dispatch command, give items, etc.)

**Listeners** — Each is a `@Component` + Bukkit `Listener`:
- Self-registers via `@PostConstruct` -> `Bukkit.getPluginManager().registerEvents(this, plugin)`
- Receives events, queries `AdvancementRegistry.findByRequirement()`, calls `incrementProgression()`

**`LumaGuildsHook` (`@Component`)** — Optional:
- Checks `isAvailable()` before registering
- Listens for LumaGuilds custom events via Bukkit event API
- No direct dependency on LumaGuilds classes (uses reflection or event class name matching if needed)

### 5d. Config Layer

**`AdvancementsConfig` (`@ConfigFile("advancements")`)** — Auto-discovered by Nexus, auto-loaded as bean:
```kotlin
@ConfigFile("advancements")
class AdvancementsConfig {
    var debug: Boolean = false
    var treesDirectory: String = "trees"
}
```

**`TreeConfigParser` (`@Service`)** — Reads `.conf` files from `trees/` directory:
- Uses Typesafe Config (`ConfigFactory.parseFile()`) to parse HOCON
- Converts to `TreeDef` domain objects
- Calls `validate()` on each, logs errors and skips invalid trees

### 5e. Commands

**`AdvancementCommand` (`@Command(name = "advancements")`)** — Three subcommands:
- `reload` — `@Async`, `@Permission("advancements.admin")`
- `grant <player> <tree> <key>` — `@Permission("advancements.admin")`
- `list` — no permission required

---

## 6. Bootstrap Sequence

```
CustomAdvancementsPlugin.onEnable()
    |
    |-- 1. Copy default trees/ if missing
    |
    |-- 2. NexusContext.create(...)
    |       |-- Auto-discovers @ConfigFile("advancements") -> AdvancementsConfig bean
    |       |-- Auto-discovers @Service, @Component, @Repository beans:
    |       |     TreeConfigParser
    |       |     BukkitRewardExecutor (implements RewardExecutor)
    |       |     UltimateAdvancementAdapter (implements AdvancementRegistry)
    |       |       |-- @PostConstruct -> initialize()
    |       |       |     |-- UltimateAdvancementAPI.getInstance(plugin)
    |       |       |     |-- loadAllTrees() -> TreeConfigParser -> BuildTree -> registered tabs
    |       |     EntityKillListener
    |       |       |-- @PostConstruct -> registerEvents(this, plugin)
    |       |     BlockBreakListener, CraftItemListener, PlayerJoinListener (same pattern)
    |       |     LumaGuildsHook
    |       |       |-- @PostConstruct -> onEnable() -> registers only if LumaGuilds available
    |       |
    |       |-- Registers CoroutineScope, NexusDispatchers, ConfigManager as beans
    |
    |-- 3. nexus.registerPaperCommands(...)
            |-- Scans for @Command classes
            |-- Registers AdvancementCommand with Brigadier
```

---

## 7. Reload Sequence

```
/advancements reload
    |
    v
AdvancementCommand.reload()
    |
    v
UltimateAdvancementAdapter.reloadAll()
    |-- api.unregisterPluginAdvancementTabs()   -- disposes all tabs
    |-- tabs.clear()
    |-- advancementMap.clear()
    |-- requirementIndex.clear()
    |-- loadAllTrees()                          -- re-parse, re-build, re-register
    |
    v
sender.sendMessage("Advancement trees reloaded.")
```

---

## 8. UAAPI 3.0.0 Display Construction

Building an `AdvancementDisplay` from a domain `AdvancementNodeDef`:

```kotlin
fun buildDisplay(node: AdvancementNodeDef): AdvancementDisplay {
    return AdvancementDisplayBuilder()
        .icon(ItemStack(Material.valueOf(node.icon)))
        .title(TextComponent(node.title))
        .description(node.description.map { TextComponent(it) })
        .frame(AdvancementFrameType.valueOf(node.frame.name))
        .showToast(node.showToast)
        .announceChat(node.announceChat)
        .coords(node.x, node.y)
        .build()
}
```

> **Note:** `AdvancementDisplayBuilder` API should be verified via Context7 or the reference source at `D:\BadgersMC-Dev\_api-reference\UltimateAdvancementAPI-3.0.0` before implementation. The builder method names above are inferred from the 3.0.0 source and may need minor adjustments.

---

## 9. Requirement Index Structure

The requirement index enables O(1) lookups from event listeners:

```
requirementIndex: Map<RequirementType, Map<String?, List<Pair<String, String>>>>

Example contents:
{
  ENTITY_KILL: {
    "ZOMBIE":    [("combat", "kill_zombies")],
    "SKELETON":  [("combat", "kill_skeletons")],
    "WITHER":    [("combat", "kill_wither")]
  },
  BLOCK_BREAK: {
    "DIAMOND_ORE": [("exploration", "mine_diamonds")]
  },
  PLAYER_JOIN: {
    null: [("exploration", "first_login")]
  }
}
```

**Built during `loadAllTrees()`** by iterating each tree's nodes and indexing those with a non-null `requirement`.

---

## 10. External Dependencies & Boundaries

| Boundary | Our Side | Their Side | Communication |
|----------|----------|-----------|---------------|
| UAAPI | `UltimateAdvancementAdapter` | `UltimateAdvancementAPI` singleton | Java API calls |
| Bukkit | Listeners, Reward executor | Paper event system | Bukkit events + API |
| Nexus | All `@Service`/`@Component` classes | `NexusContext` container | Constructor injection |
| LumaGuilds | `LumaGuildsHook` | Guild event classes | Bukkit custom events |
| Config files | `TreeConfigParser` | Filesystem | Typesafe Config HOCON parser |
| Player | Commands, Advancement GUI | Minecraft client | Paper commands + UAAPI rendering |

---

## 11. Error Handling Strategy

| Scenario | Handling |
|----------|---------|
| Invalid tree config (bad structure) | Log error with filename + details, skip tree, continue loading others |
| Unknown Material in icon field | Log warning, fall back to `Material.BARRIER` |
| Unknown EntityType/Material in requirement target | Log warning at load time, advancement will never trigger |
| UAAPI not installed | Plugin fails to enable (required dependency in paper-plugin.yml) |
| `incrementProgression` future fails | Log error, player progression unchanged |
| Reward command fails | Log error, continue executing remaining rewards |
| LumaGuilds not installed | `LumaGuildsHook.isAvailable()` returns false, hook silently skips |
