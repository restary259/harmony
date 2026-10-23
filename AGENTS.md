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
  (not `mods.toml`), with `loaderVersion="[4,)"` and `pack_format` 34 for 1.21.1.
- `WorldGenManager` maps are the authoritative claim record. Only add to a mod's
  claim set when the claim is accepted, so `getRegisteredBiomes` and
  `canModModifyBiome` stay consistent.

## Repository

- Upstream: `https://github.com/restary259/harmony` (branch `main`).
