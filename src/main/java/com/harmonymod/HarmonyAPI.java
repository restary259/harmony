package com.harmonymod;

import net.minecraft.resources.ResourceLocation;
import com.harmonymod.worldgen.WorldGenManager;

import java.util.Collections;
import java.util.Set;

/**
 * Public API for mods to interact with Harmony's worldgen mediation system.
 */
public class HarmonyAPI {
    private static WorldGenManager worldGenManager;

    static void initialize(WorldGenManager manager) {
        HarmonyAPI.worldGenManager = manager;
    }

    public static void registerWorldGenBiomes(String modid, Set<ResourceLocation> biomes) {
        if (worldGenManager == null) {
            HarmonyMod.LOGGER.error("HarmonyAPI not initialized! Cannot register biomes for {}.", modid);
            return;
        }
        worldGenManager.registerModBiomes(modid, biomes);
    }
    public static void registerWorldGenStructures(String modid, Set<ResourceLocation> structures) {
        if (worldGenManager == null) {
            HarmonyMod.LOGGER.error("HarmonyAPI not initialized! Cannot register structures for {}.", modid);
            return;
        }
        worldGenManager.registerModStructures(modid, structures);
    }
    public static boolean canModModifyBiome(String modid, ResourceLocation biome) {
        if (worldGenManager == null) {
            HarmonyMod.LOGGER.error("HarmonyAPI not initialized! Query denied for biome {}.", biome);
            return false;
        }
        return worldGenManager.canModModifyBiome(modid, biome);
    }
    public static boolean canModModifyStructure(String modid, ResourceLocation structure) {
        if (worldGenManager == null) {
            HarmonyMod.LOGGER.error("HarmonyAPI not initialized! Query denied for structure {}.", structure);
            return false;
        }
        return worldGenManager.canModModifyStructure(modid, structure);
    }
    public static Set<ResourceLocation> getRegisteredBiomes(String modid) {
        if (worldGenManager == null) return Collections.emptySet();
        return worldGenManager.getRegisteredBiomes(modid);
    }
    public static Set<ResourceLocation> getRegisteredStructures(String modid) {
        if (worldGenManager == null) return Collections.emptySet();
        return worldGenManager.getRegisteredStructures(modid);
    }
}
