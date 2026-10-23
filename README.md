# Harmony

**Harmony** is a worldgen claim mediator for Minecraft. It stops two mods from
fighting over the same biome or structure, and it does so **by enforcement, not by
convention**: a mod that has never heard of Harmony is still governed, because
ownership is derived from registry namespaces rather than from cooperation.

- **Minecraft:** 1.21.1
- **Loader:** [NeoForge](https://neoforged.net/) 21.1+
- **Java:** 21

---

## What it actually does

NeoForge applies biome and structure modifiers from a flat list, having discarded the
registry keys that identify their authors. Harmony mixes into that pipeline
(`ModifiableBiomeInfo.applyBiomeModifiers` and
`ModifiableStructureInfo.applyStructureModifiers`) and re-derives the author of each
modifier from the datapack registry it came from. A modifier whose author is not
permitted for the target biome/structure is skipped before it can run.

Concretely, with default configuration, a datapack that adds diamond ore to
`minecraft:desert` is **blocked**, while the same modifier targeting
`minecraft:plains` (on the open list) is **allowed**. Both were verified on a real
dedicated server; see [Verification](#verification).

### Attribution, not honour system

Ownership comes from the namespace of the registry id:

- `harmonytest:inject_blocked` belongs to `harmonytest`
- `minecraft:desert` is an unclaimed vanilla biome

This is why a mod that never calls the Harmony API is still attributed and enforced.

---

## Installation

1. Install NeoForge for Minecraft 1.21.1.
2. Drop `harmonymod-1.0.0.jar` into your `mods` folder.
3. (Optional) Edit `config/harmonymod-common.toml`.

The same jar works on client and dedicated server.

---

## In-game interface

Operators (permission level 2) get a `/harmony` command:

| Command | Purpose |
| --- | --- |
| `/harmony status` | Claim counts, conflict count, and how many modifiers were attributed |
| `/harmony list` | Every claimed biome/structure and its owner |
| `/harmony conflicts` | Every blocked or refused modification, with both parties |
| `/harmony why <id>` | Why a target is or is not modifiable |
| `/harmony reload` | Clear derived state; ownership is re-derived on the next pass |

Client-side, the mod list's **Config** button opens NeoForge's generated config
screen, which exposes every option with validation and tooltips.

---

## Configuration

`config/harmonymod-common.toml`:

```toml
[general]
logAllClaims = false
allowForceClaim = false
priorityMods = ["harmonymod"]

[worldgen]
blockVanillaBiomeChanges = true
blockVanillaStructureChanges = true
openVanillaBiomes = ["minecraft:plains", "minecraft:forest"]
openVanillaStructures = ["#minecraft:village"]
```

- `blockVanillaBiomeChanges` / `blockVanillaStructureChanges` — when true, an unclaimed
  vanilla target may only be modified if it appears in the corresponding open list.
- `openVanillaBiomes` / `openVanillaStructures` — vanilla targets always open to any mod.
  Entries beginning with `#` are **registry tags**: `#minecraft:village` opens every
  village variant. Without the `#`, an entry is an exact id. (`minecraft:village` is a
  tag, not a structure, so the `#` form is required to open villages.)
- `priorityMods` — mod ids that may take over a non-priority mod's claim.
- `allowForceClaim` — lets any mod take over any claim. For dev packs only.
- `logAllClaims` — logs every claim and permission check.

Non-vanilla targets that nobody has claimed are always open.

---

## For modders

Using the API is **optional**. Harmony already attributes and enforces your modifiers
by namespace. Call in only when you want to participate explicitly.

```java
// Claim a vanilla biome you intend to own.
HarmonyAPI.claimBiome("yourmodid", ResourceLocation.parse("minecraft:plains"));

// Check before acting.
if (HarmonyAPI.canModifyBiome("yourmodid", biomeId)) {
    // safe to modify
}

// Query and observe.
String owner = HarmonyAPI.biomeOwner(biomeId);
Set<ResourceLocation> mine = HarmonyAPI.biomesOf("yourmodid");
List<Conflict> blocked = HarmonyAPI.conflicts();

HarmonyAPI.onBiomeClaim((modid, biome) -> LOGGER.info("{} claimed {}", modid, biome));
```

Full surface: `HarmonyAPI` (`API_VERSION` = 1). Every method is null-safe and never
throws if Harmony has not initialized.

---

## Verification

`./gradlew build` runs 21 unit tests over the claim registry, permission rules, reload
safety, event dispatch, tag resolution, and conflict de-duplication.

Enforcement was additionally verified end-to-end on a real dedicated server with a
datapack registering four modifiers:

| Modifier | Target | Result |
| --- | --- | --- |
| `harmonytest:inject_blocked` (biome) | `minecraft:desert` | blocked |
| `harmonytest:inject_allowed` (biome) | `minecraft:plains` (open-listed) | allowed |
| `harmonytest:spawn_blocked` (structure) | `minecraft:ancient_city` | blocked |
| `harmonytest:spawn_allowed` (structure) | `minecraft:village_plains` (`#minecraft:village` tag) | allowed |

Observed log output:

```
[harmonymod/]: Blocked biome modifier from harmonytest on minecraft:desert (owner: unclaimed vanilla)
[harmonymod/]: Blocked structure modifier from harmonytest on minecraft:ancient_city (owner: unclaimed vanilla)
```

Setting `blockVanillaBiomeChanges = false` produced zero blocks, confirming the
configuration genuinely drives enforcement rather than merely being read.

---

## Building

```sh
./gradlew build          # jar at build/libs/harmonymod-1.0.0.jar
./gradlew runServer      # dev dedicated server
./gradlew runClient      # dev client
```

Requires JDK 21.

---

## License

MIT. See [LICENSE](LICENSE).

## Credits

Designed and developed by Raneem G Dawahade.
