# AGENTS.md

## Project

Harmony is a NeoForge mod for **Minecraft 1.21.1** that mediates worldgen claims
between mods (who may edit a given biome or structure).

## Build

- Toolchain: **Java 21**, Gradle 8.10 (wrapper is committed), `net.neoforged.moddev` plugin.
- `./gradlew clean build` — compiles, runs tests, produces `build/libs/harmonymod-<version>.jar`.
- Versions live in `gradle.properties`: `minecraft_version=1.21.1`, `neo_version=21.1.264`.
  The Minecraft and NeoForge versions must be a valid pair or dependency resolution fails.
- `./gradlew runServer` / `runClient` / `runData` for dev runs. Run output goes to `run/`.
- First build is slow (decompiles Minecraft via NeoForm, ~5 min); later builds are incremental.

## Testing

- JUnit 5 tests in `src/test/java`. The `neoForge { unitTest { enable() } }` block in
  `build.gradle` is required for the test source set to see Minecraft classes; without it
  `compileTestJava` fails with "cannot find symbol: class ResourceLocation".
- Tests must reset `HarmonyConfig` static fields in `@BeforeEach`, since config is global state.

## Conventions and gotchas

- Event handlers: `net.neoforged.bus.api.SubscribeEvent` for the game bus
  (`NeoForge.EVENT_BUS`); `net.neoforged.fml.event.*` events go on the mod bus.
  There is no `net.neoforged.neoforge.eventbus` package.
- `ResourceLocation` is constructed with `ResourceLocation.parse(String)` /
  `tryParse(String)`; the public constructor was removed.
- Config: `ModContainer.registerConfig(...)` from the `@Mod` constructor, backed by
  `ModConfigSpec`. Read values through the static fields in `HarmonyConfig.load()`,
  which runs on `ModConfigEvent`; do not read the spec values directly.
  Use the `defineListAllowEmpty` overload that takes a new-element supplier (the
  3-arg form is deprecated).
- Mod metadata file is `src/main/resources/META-INF/neoforge.mods.toml`
  (not `mods.toml`), with `loaderVersion="[4,)"`. A datapack `pack.mcmeta` for
  1.21.1 needs `pack_format` **48** (not 34).
- `ClaimRegistry` maps are the authoritative claim record. Only add to a mod's
  claim set when the claim is accepted, so `biomesOf` and `canModifyBiome` stay consistent.

## Enforcement (mixins)

- Ownership is derived, not declared: a biome/structure modifier's author is the
  namespace of its registry key. NeoForge's modifier pipeline discards that key, so
  `ModifierOwnership` re-reads it from the live `biome_modifier` / `structure_modifier`
  registries and maps the modifier *instance* (identity map) to its namespace.
- Mixins hook `ModifiableBiomeInfo.applyBiomeModifiers` and
  `ModifiableStructureInfo.applyStructureModifiers`. They return a
  `Predicate<Holder<Biome>>` / `Predicate<Holder<Structure>>` filter; returning `false`
  skips that modifier for that target.
- `ServerLifecycleHooks.getCurrentServer()` is already set when the modifier pass runs,
  which is what makes `ensureLoaded()` work from inside the mixin.
- NeoForge calls a modifier once per **phase per biome**, so the same logical conflict
  arrives many times per load. `ClaimRegistry.record` de-duplicates on
  (actor, target, kind) and only the first occurrence is logged.

## Datapack gotchas (cost real debugging time)

- NeoForge datapack registries live under `data/<ns>/neoforge/<registry>/`, e.g.
  `data/mymod/neoforge/biome_modifier/x.json` and `.../neoforge/structure_modifier/y.json`.
  A `data/<ns>/biome_modifier/` path silently loads nothing (`size() == 0`).
- The mod's own mixin config must be registered in `neoforge.mods.toml` via a
  `[[mixins]]` block with `config="harmonymod.mixins.json"`; the file alone is ignored.
- `minecraft:village` is a **structure tag**, not a structure. Open-list entries
  beginning with `#` are treated as tags (`#minecraft:village`); the tag-less form
  matches no structure at all. Tag matching needs the live `Holder`, so the
  holder-aware `canModifyBiome/Biome(..., Holder)` overloads must be used from mixins.

## Verifying enforcement

`./gradlew runServer` with a datapack under `run/world/datapacks/` is the end-to-end
check. Grep `run/logs/latest.log` for "Blocked biome modifier" / "Blocked structure
modifier". Confirmed working cases: `minecraft:desert` blocked, `minecraft:plains`
allowed (open list), `minecraft:ancient_city` blocked, `minecraft:village_plains`
allowed (`#minecraft:village`).

## Repository

- Upstream: `https://github.com/restary259/harmony` (branch `main`).
