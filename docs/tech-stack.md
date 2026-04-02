# Tech Stack — CustomAdvancements Plugin

> Version: 1.0 | Last Updated: 2026-04-02
> Target: Paper 1.21.1 | Java 21 | Kotlin 2.0

---

## Languages

| Language | Version | Purpose |
|----------|---------|---------|
| Kotlin | 2.0.21 | Primary language. All plugin code. |
| Java | 21 (LTS) | JVM target. Required by Paper 1.21.1. |
| HOCON | — | Config file format (Human-Optimized Config Object Notation) |

---

## Core Dependencies

### Runtime

| Dependency | Version | Scope | Purpose | Repository |
|------------|---------|-------|---------|------------|
| Paper API | 1.21.1-R0.1-SNAPSHOT | `compileOnly` | Minecraft server API | `https://repo.papermc.io/repository/maven-public/` |
| Nexus Core | 1.1.0 | `implementation` (shaded) | DI container, config system, coroutines | `mavenLocal()` |
| Nexus Paper | 1.1.0 | `implementation` (shaded) | Paper-specific extensions (commands, BukkitDispatcher) | `mavenLocal()` |
| UltimateAdvancementAPI | 3.0.0-beta-1 | `compileOnly` | Advancement GUI rendering, progression, persistence | `https://nexus.frengor.com/repository/public/` |

### Test

| Dependency | Version | Scope | Purpose |
|------------|---------|-------|---------|
| JUnit 5 | 5.11+ | `testImplementation` | Test framework |
| MockK | 1.13.13 | `testImplementation` | Kotlin-first mocking |
| kotlin-test | 2.0.21 | `testImplementation` | Kotlin test assertions |

### Build Tools

| Tool | Version | Purpose |
|------|---------|---------|
| Gradle | 8.10+ | Build system |
| Shadow Plugin | 8.3.5 (`com.gradleup.shadow`) | Fat JAR packaging (shades Nexus only) |
| Kotlin Gradle Plugin | 2.0.21 | Kotlin compilation |

---

## Framework Details

### Nexus (BadgersMC DI Framework)

**What it is:** Lightweight, Kotlin-first dependency injection framework. Spring-like DI without Spring's weight.

**Source:** `D:\BadgersMC-Dev\nexus` (local, two modules: `nexus-core`, `nexus-paper`)

**Key annotations:**
| Annotation | Module | Purpose |
|------------|--------|---------|
| `@Service` | nexus-core | Marks service-layer beans |
| `@Repository` | nexus-core | Marks data-access beans |
| `@Component` | nexus-core | Marks generic managed beans |
| `@PostConstruct` | nexus-core | Lifecycle hook: runs after injection |
| `@PreDestroy` | nexus-core | Lifecycle hook: runs before shutdown |
| `@ConfigFile("name")` | nexus-core | Marks a class as a HOCON config file |
| `@Comment("...")` | nexus-core | Adds comments to generated HOCON |
| `@ConfigName("name")` | nexus-core | Custom field name in config |
| `@Command(name = "...")` | nexus-paper | Registers a Brigadier command |
| `@Subcommand("...")` | nexus-paper | Registers a subcommand |
| `@Arg("name")` | nexus-core | Command argument |
| `@Context` | nexus-core | Injects command sender |
| `@Permission("...")` | nexus-paper | Permission check |
| `@Async` | nexus-paper | Runs command off main thread |
| `@PlayerOnly` | nexus-paper | Restricts command to players |

**Bootstrap pattern:**
```kotlin
val nexus = NexusContext.create(
    basePackage = "com.badgersmc.advancements",
    classLoader = this::class.java.classLoader,
    configDirectory = dataFolder.toPath(),
    contextName = "CustomAdvancements",
    externalBeans = mapOf("plugin" to this)
)
nexus.registerPaperCommands(
    basePackage = "com.badgersmc.advancements",
    classLoader = this::class.java.classLoader,
    plugin = this
)
```

**Auto-registered beans:**
- `CoroutineScope` — plugin-scoped, SupervisorJob + virtual threads
- `NexusDispatchers` — virtual-thread-backed dispatchers
- `ConfigManager` — if `configDirectory` provided

---

### UltimateAdvancementAPI 3.0.0-beta

**What it is:** API for creating custom advancement tabs/trees rendered in Minecraft's vanilla advancement GUI. Handles all persistence (SQLite/MySQL) internally.

**Reference source:** `D:\BadgersMC-Dev\_api-reference\UltimateAdvancementAPI-3.0.0`

**Key classes:**

| Class | Purpose |
|-------|---------|
| `UltimateAdvancementAPI` | Entry point. `getInstance(plugin)` to obtain. |
| `AdvancementTab` | One tab in the GUI. Created via `api.createAdvancementTab(namespace, bgTexture)`. |
| `RootAdvancement` | First node in a tree. `non-sealed`, extendable. |
| `BaseAdvancement` | Child node in a tree. `non-sealed`, extendable. |
| `AdvancementDisplay` | Immutable display info (icon, title, description, frame, coords). |
| `AdvancementDisplayBuilder` | Builder for `AdvancementDisplay`. Uses `BaseComponent` for text. |
| `AdvancementFrameType` | Enum: `TASK`, `GOAL`, `CHALLENGE` |
| `TeamProgression` | Progress is per-team, not per-player. |
| `AdvancementGrantEvent` | Bukkit event fired when an advancement is completed. |

**Critical constraints:**
- `Advancement` is `sealed` — only `BaseAdvancement` and `RootAdvancement` can extend it
- `BaseAdvancement` / `RootAdvancement` are `non-sealed` — we CAN extend these
- Advancements are **immutable after `tab.registerAdvancements()`** — no add/remove after init
- `incrementProgression()` returns `CompletableFuture<ProgressionUpdateResult>` (async)
- `giveReward(Player)` is overridable on both `BaseAdvancement` and `RootAdvancement`

**Constructor signatures (3.0.0):**
```java
// Note: parent comes FIRST in 3.0.0 (was last in 2.7.2)
new BaseAdvancement(parent, key, maxProgression, display)
new RootAdvancement(advancementTab, key, maxProgression, display)
// Background texture moved to tab creation:
api.createAdvancementTab(namespace, backgroundTexture)
```

---

## Optional Integrations

| Plugin | Required? | Purpose | Communication |
|--------|-----------|---------|---------------|
| LumaGuilds | No | Guild-based advancements | Bukkit custom events |
| PlaceholderAPI | No | Placeholder support | PAPI expansion registration |

---

## Development Methodology

| Practice | Details |
|----------|---------|
| **TDD** | Tests written BEFORE implementation. Domain models and actions tested first. |
| **Spec-Driven Development** | Requirements (EARS format) drive what gets built. See `requirements.md`. |
| **Hybrid approach** | Specs define WHAT. Tests verify the spec. Implementation satisfies both. |

---

## AI Development Rules

> These rules apply when any LLM is used to assist development on this project.

1. **Use Context7 MCP** (`resolve-library-id` + `get-library-docs`) to look up library APIs instead of reading large source files. This saves tokens.
2. **Use Semgrep** for code scanning instead of raw grep when checking for patterns or vulnerabilities.
3. **Reference these docs** — don't re-derive architecture. The blueprint is in `implementation.md`.
4. **Follow EARS requirements** — every feature maps to a requirement ID in `requirements.md`.
5. **Check `tasks.md`** before starting work — find your task, read its references, execute.
6. **Don't add features not in the spec.** If it's not in `requirements.md`, it doesn't get built.
7. **Nexus is the DI framework.** Do not introduce Koin, Dagger, Spring, or manual wiring.
8. **UAAPI handles persistence.** Do not build custom progress storage.
9. **HOCON for config.** Do not use YAML or JSON for plugin configuration.
10. **Kotlin only.** No Java source files in this project.
