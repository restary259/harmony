````markdown
# Harmony Mod Developer API

This document describes the public API provided by Harmony Mod for worldgen coordination and mod compatibility.

---

## Registering WorldGen Biomes/Structures

Your mod should register the biomes and/or structures it intends to control during initialization.  
This ensures Harmony can enforce boundaries and avoid worldgen conflicts.

**Example (Java):**
```java
import com.harmonymod.HarmonyAPI;
import com.harmonymod.util.BiomeStructureUtils;
import net.minecraft.resources.ResourceLocation;

HarmonyAPI.registerWorldGenBiomes("yourmodid",
    BiomeStructureUtils.resourceSet("minecraft:plains", "yourmodid:custom_biome"));

HarmonyAPI.registerWorldGenStructures("yourmodid",
    BiomeStructureUtils.resourceSet("minecraft:village", "yourmodid:custom_structure"));
