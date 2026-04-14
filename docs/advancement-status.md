# Advancement Implementation Status

> Last Updated: 2026-04-13
> For use as context in continuation prompts

## Architecture

- **CustomAdvancements** (`D:\BadgersMC-Dev\CustomAdvancements`) — Kotlin plugin, Nexus DI, UAAPI 3.0.0
- **enthusia-network** (`D:\BadgersMC-Dev\enthusia-network`) — monorepo with git submodules for all plugins
- All listeners use `Class.forName()` + inner handler class pattern for optional deps
- External plugin JARs referenced as `compileOnly(files("..."))` in `build.gradle.kts`
- Tree configs are HOCON `.conf` files in `src/main/resources/trees/`

## Current Trees (6 total)

| Tree | File | Nodes |
|------|------|-------|
| combat | combat.conf | 4 (root, zombie_slayer, bone_collector, wither_storm) |
| exploration | exploration.conf | 7 (root, first_steps, miner, diamond_hunter, time_well_spent, dedicated, no_life) |
| guilds | guilds.conf | 30 nodes (fully complete) |
| diary | diary.conf | 9 nodes |
| economy | economy.conf | 2 nodes (root, printing_money) |
| reputation | reputation.conf | 8 nodes |

## Current RequirementTypes (46 total)

Bukkit native: BLOCK_BREAK, BLOCK_PLACE, ENTITY_KILL, CRAFT_ITEM, PLAYER_KILL, PLAYER_JOIN
LumaGuilds individual: GUILD_CREATE, GUILD_MEMBER_JOIN, GUILD_BANNER_SET, GUILD_HOME_SET, GUILD_VAULT_PLACED, GUILD_LEVEL_UP, GUILD_WAR_DECLARED, GUILD_WAR_WON, GUILD_MEMBER_REMOVED, GUILD_OWNERSHIP_TRANSFER, GUILD_FRIENDLY_FIRE
LumaGuilds collective: GUILD_COLLECTIVE_SMELT, GUILD_COLLECTIVE_BLOCK_BREAK, GUILD_COLLECTIVE_CROP_HARVEST, GUILD_COLLECTIVE_MOB_KILL, GUILD_COLLECTIVE_BOSS_KILL
ItemShops: GUILD_SHOP_SALE, GUILD_SHOP_PURCHASE
AxKoth: KOTH_CAPTURE, KOTH_CONSECUTIVE_CAPTURE
War: GUILD_WAR_KILL
Playtime guild: GUILD_COMBINED_PLAYTIME
Leaderboard: GUILD_LEADERBOARD_RANK
Diplomacy: GUILD_ALLIANCE_FORMED
Shop/Market: SHOP_CREATED, SHOP_DELETED, GUILD_REGION_PURCHASED
DiaryKeeper: DIARY_RECEIVED, DIARY_FILLED, DIARY_SIGNED, DIARY_OBTAINED, DIARY_VOID_RETURN, DIARY_DESTRUCTION_ATTEMPT, DIARY_CONTAINER_ATTEMPT, DIARY_DUPLICATE_WARNING
Currency: BALTOP_ENTER
Playtime individual: PLAYER_PLAYTIME
Commend: COMMEND_RECEIVED, COMMEND_GIVEN, REP_MILESTONE

## Current Listeners (14 total)

EntityKillListener, BlockBreakListener, CraftItemListener, PlayerJoinListener,
GuildCollectiveListener, ShopTransactionListener, KothListener, GuildPlaytimeListener,
LeaderboardRankListener, ShopEventListener, DiaryListener, CurrencyListener,
CommendListener, PlaytimeListener

Plus: LumaGuildsHook (in plugins/ package)

## Events Added to Forked Plugins

### DiaryKeeper (BadgersMC/DiaryKeeper) — 8 events in `com.lincoln.diary.events`
DiaryReceivedEvent, DiaryFilledEvent, DiarySignedEvent, DiaryObtainedEvent,
DiaryVoidReturnEvent, DiaryDestructionAttemptEvent, DiaryContainerAttemptEvent,
DiaryDuplicateWarningEvent
**Wired into**: JoinListener, EditListener, DropTrackListener, ItemProtectionListener, ContainerGuardListener, VoidWatcher, DuplicateWatcher. Compiles clean.

### EnthusiaCommend (wsg138/EnthusiaCommend, NOT forked) — 3 events in `org.enthusia.rep.events`
CommendationReceivedEvent, CommendationGivenEvent, RepMilestoneReachedEvent
**Wired into**: RepService.addOrUpdateCommendation() and adjustScore(). Note: plugin has pre-existing compile error (missing skin package) unrelated to our changes. Events compiled standalone into commend-events.jar.

### EnthusiaCurrency (BadgersMC/EnthusiaCurrency) — 1 event exists
BaltopTopEnterEvent in `com.enthusia.enthusiacurrency.event` (pre-existing, not added by us)

---

## REMAINING WORK — All Missing Advancements

### 1. General — Rough Landing (1 node)
**Need**: New `PLAYER_DEATH` RequirementType + PlayerDeathListener
**Advancement**: "Rough Landing" (Hidden) — die within first 10 minutes of joining
**Hook**: PlayerDeathEvent (Bukkit native), check if player joined recently
**Where to add**: New listener in CustomAdvancements, new node in exploration.conf

### 2. Commendation — Complex Advancements (8 nodes)
**Need**: New events in EnthusiaCommend + new RequirementTypes + expanded reputation.conf

| Advancement | Event Needed | Where to Add Event |
|---|---|---|
| Public Record | CommendationProfileViewedEvent | RepGuiManager.java (when GUI opens) |
| Change of Heart | CommendationEditedEvent | RepService.addOrUpdateCommendation() (update branch) |
| The Critic | CommendationGivenEvent + track categories covered | CommendListener needs category tracking |
| Fame or Shame? | CommendationLeaderboardViewedEvent | RepGuiManager.java or CommendCommand.java |
| Score thresholds (-20, -10, -5, +10) | Already have REP_MILESTONE | Just add 4 more nodes to reputation.conf |
| Cornered Beast | PlayerKillEvent + rep score lookup | New cross-system listener |
| Polarizing Figure (Hidden) | CommendationReceivedEvent + track pos/neg counts | CommendListener needs counter logic |
| Forgiven Not Forgotten (Hidden) | RepMilestoneReachedEvent + history check | Need to track "was ever <= -10" state |

**New RequirementTypes needed**: COMMEND_PROFILE_VIEWED, COMMEND_EDITED, COMMEND_LEADERBOARD_VIEWED, COMMEND_ALL_CATEGORIES, PLAYER_KILL_WITH_REP (or handle in listener)

### 3. EnthusiaCurrency — Economy Advancements (7 nodes)
**Need**: 6 new events in EnthusiaCurrency plugin + RequirementTypes + expanded economy.conf

| Advancement | Event to Add | Where in EnthusiaCurrency Source |
|---|---|---|
| Cold Hard Cash | PlayerPickupCurrencyEvent | Listen to PlayerPickupItemEvent, check if currency item |
| Safely Secured | CurrencyDepositEvent | DepositCommand.java after successful deposit |
| The Gold Standard | Inventory check for Raw Gold Block | Could use existing Bukkit InventoryCloseEvent + check |
| Philanthropist | CurrencyPayEvent | PayCommand.java after successful pay |
| Liquid Assets | CurrencyWithdrawEvent | WithdrawCommand.java after successful withdraw |
| Fat Fingers (Hidden) | CurrencyPaySelfAttemptEvent | PayCommand.java when sender == target |
| Rock Bottom (Hidden) | CurrencyBalanceZeroEvent | BalanceStorage.setBalance() or withdraw() when balance hits 0 |

**Source location**: `D:\BadgersMC-Dev\enthusia-network\plugins\enthusia-currency\src\main\java\com\enthusia\enthusiacurrency\`
**Key files**: `command/DepositCommand.java`, `command/WithdrawCommand.java`, `command/PayCommand.java`, `storage/BalanceStorage.java`

**New RequirementTypes**: CURRENCY_PICKUP, CURRENCY_DEPOSIT, CURRENCY_WITHDRAW, CURRENCY_PAY, CURRENCY_PAY_SELF, CURRENCY_BALANCE_ZERO

### 4. Playtime — Hidden Advancements (2 nodes)
**Need**: 2 new events in playtime-plugin + RequirementTypes

| Advancement | Event to Add | Where |
|---|---|---|
| Caught in 4K (Hidden) | AntiAfkWarningEvent | Wherever AFK detection triggers a warning |
| The Ghost (Hidden) | AfkDurationEvent | When AFK duration crosses threshold |

**Source**: `D:\BadgersMC-Dev\enthusia-network\plugins\playtime-plugin\`
**New RequirementTypes**: AFK_WARNING, AFK_DURATION

### 5. DiaryKeeper — Missing Nodes (2 nodes)
**Need**: Just add nodes to diary.conf (events already exist)

| Advancement | RequirementType | Notes |
|---|---|---|
| Stubborn (Hidden) | DIARY_DESTRUCTION_ATTEMPT amount=10 | Survive 10 destruction attempts |
| Identity Theft (Hidden) | DIARY_DUPLICATE_WARNING | Trigger when duplicate detected for your diary |

### 6. Container Shops (enthusia-itemshops) — (7 nodes)
**Need**: 5 new events in ItemShops plugin + RequirementTypes + nodes

| Advancement | Event to Add | Where in ItemShops Source |
|---|---|---|
| Compact Commerce | ShopSignAttachedEvent | When a sign is placed on a shop container |
| Business Partners | ShopTrustAddedEvent | When owner adds a trusted player |
| Supply Chain | HopperConnectedToShopEvent | When hopper connects to shop container (NOT the sale-count approach currently used) |
| Sold Out! | ShopStockDepletedEvent | PostShopTransactionEvent handler when remaining stock = 0 |
| Wholesale Buyer | PostShopTransactionEvent + isBuyMax flag | Add flag to existing event |
| Monopoly | SHOP_CREATED amount=64 | Just a node — type exists, need node in tree |
| Bad Investment (Hidden) | SHOP_DELETED (type exists) | Just a node — need node in tree |
| False Advertising (Hidden) | PreShopTransactionEvent when stock=0 | Add check in existing event flow |

**Source**: `D:\BadgersMC-Dev\ItemShops\` (also submodule at enthusia-network/plugins/item-shops)
**New RequirementTypes**: SHOP_SIGN_ATTACHED, SHOP_TRUST_ADDED, SHOP_HOPPER_CONNECTED, SHOP_STOCK_DEPLETED, SHOP_WHOLESALE_BUY, SHOP_EMPTY_TRANSACTION

### 7. NotBounties — Fully Missing (8+ nodes)
**Status**: No source available. Skip until source is obtained or build Trigger API.

---

## Recommended Implementation Order

1. **Quick wins** (15 min) — DiaryKeeper 2 missing nodes + exploration Rough Landing + 4 rep score threshold nodes = 7 nodes
2. **EnthusiaCurrency events** (medium) — 6 new events + 7 economy nodes
3. **EnthusiaCommend events** (medium) — 4 new events + 8 reputation nodes (complex ones need state tracking)
4. **ItemShops events** (medium) — 5 new events + 7 shop nodes + fix Supply Chain to use hopper event
5. **Playtime hidden** (small) — 2 events + 2 nodes
6. **NotBounties** — deferred (no source)

## Build Notes

- DiaryKeeper: Maven, compiles with `./mvnw package` (wrapper copied from currency)
- EnthusiaCurrency: Maven, compiles with `./mvnw package`
- EnthusiaCommend: Maven, has pre-existing compile error (missing skin package). Events compiled standalone into `target/commend-events.jar`
- Playtime-plugin: Maven, compiles with `./mvnw package`
- ItemShops: Gradle, compiles with `./gradlew shadowJar`
- CustomAdvancements: Gradle + Kotlin, all 44 tests pass, `./gradlew test` green
