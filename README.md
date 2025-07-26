# Harmony Mod

**Harmony** is an open-source universal worldgen and structure claim mediator for Minecraft modding.  
It ensures compatibility between mods that modify biomes or structures, provides a clear and easy API for modders, and offers flexible configuration for pack creators and users.

---

## Features

- **Worldgen Claim System:**  
  Mods can claim ownership of specific biomes or structures for world generation, preventing conflicts.
- **Permission Checks:**  
  Mods can easily check if they are allowed to modify a biome or structure.
- **Configurable Ownership:**  
  Config options allow force-claiming, prioritizing certain mods, or opening up vanilla biomes/structures for modification.
- **Event Hooks:**  
  Listen for claim and permission events for analytics, logging, or custom logic.
- **API-First Design:**  
  Simple, safe API for registering claims and performing checks.
- **Debug and Compatibility Tools:**  
  Optional logging of all claims and permission requests for troubleshooting.
- **Open Contribution:**  
  Anyone is encouraged to contribute, improve, and expand Harmony! See [Contributing](#contributing).

---

## Installation

1. Download the mod JAR from [Releases](https://github.com/yourname/harmonymod/releases).
2. Place the JAR in your `mods` folder.
3. (Optional) Edit `harmony.toml` in your `config` folder to tweak settings.

This mod requires [NeoForge](https://neoforged.net/) and Minecraft 1.20+.

---

## Getting Started for Modders

### Claim a Biome or Structure

```java
// Example: Claim biomes and structures for your mod
HarmonyAPI.registerWorldGenBiomes("yourmodid", BiomeStructureUtils.resourceSet(
    "minecraft:plains",
    "yourmodid:special_biome"
));
HarmonyAPI.registerWorldGenStructures("yourmodid", BiomeStructureUtils.resourceSet(
    "minecraft:village",
    "yourmodid:custom_structure"
));
```

### Permission Check

```java
if (HarmonyAPI.canModModifyBiome("yourmodid", new ResourceLocation("minecraft:plains"))) {
    // Safe to modify this biome!
}
```

### Listen for Harmony Events

```java
HarmonyEvents.onBiomeClaim((modid, biome) -> {
    System.out.println(modid + " claimed biome: " + biome);
});
```

---

## Configuration

Edit `harmony.toml` in the `config` folder to control:

- **Logging:**  
  Enable `logAllClaims` to debug all claims and permission checks.
- **Force-Claiming:**  
  Allow mods to override claims (for expert use or dev packs).
- **Priority Mods:**  
  List mod IDs with higher claim priority.
- **Vanilla Worldgen Blocking:**  
  Prevent unclaimed changes to vanilla biomes/structures.
- **Open List:**  
  Whitelist certain vanilla biomes/structures to always be modifiable.

Sample:
```toml
[general]
logAllClaims = false
allowForceClaim = false
priorityMods = "harmonymod"

[worldgen]
blockVanillaBiomeChanges = true
blockVanillaStructureChanges = true
openVanillaBiomes = "minecraft:plains,minecraft:forest"
openVanillaStructures = "minecraft:village"
```

---

## For Pack Makers

- Use the config to fine-tune which mods or worldgen areas get priority.
- Use logging to debug worldgen conflicts in complex packs.

---

## Contributing

**Harmony is open-source and welcomes contributions!**

- Fork this repository and submit pull requests for features, bugfixes, or improvements.
- Open issues for suggestions or problems.
- Discuss ideas and collaborate—let’s make worldgen compatibility easy for everyone!

**By contributing, you agree to license your code under the MIT License.**

---

## License

MIT License.  
See [LICENSE](LICENSE) for details.

---

## Credits

- Designed and developed by Raneem G Dawahade.
- Thanks to the Minecraft modding community for inspiration and feedback.

---
