package com.harmonymod;

import com.harmonymod.worldgen.WorldGenManager;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

/**
 * Public API for other mods to interact with Harmony's worldgen assignment system.
 */
public class HarmonyAPI {
    private static WorldGenManager worldGenManager;

    /**
     * Called by HarmonyMod to provide the manager instance.
     * @param manager WorldGenManager instance
     */
    public static void initialize(WorldGenManager manager) {
        worldGenManager = manager;
    }

    /**
     * Register biomes this mod will control for worldgen.
     */
    public static void registerWorldGenBiomes(String modid, Set<ResourceLocation> biomes) {
        if (worldGenManager != null) {
            worldGenManager.assignBiomesToMod(modid, biomes);
            HarmonyMod.LOGGER.info("Registered biomes for mod '{}': {}", modid, biomes);
        }
    }

    /**
     * Register structures this mod will control for worldgen.
     */
    public static void registerWorldGenStructures(String modid, Set<ResourceLocation> structures) {
        if (worldGenManager != null) {
            worldGenManager.assignStructuresToMod(modid, structures);
            HarmonyMod.LOGGER.info("Registered structures for mod '{}': {}", modid, structures);
        }
    }

    /**
     * Query if a mod is allowed to modify a biome.
     */
    public static boolean canModModifyBiome(String modid, ResourceLocation biome) {
        return worldGenManager != null && worldGenManager.canModModifyBiome(modid, biome);
    }

    /**
     * Query if a mod is allowed to modify a structure.
     */
    public static boolean canModModifyStructure(String modid, ResourceLocation structure) {
        return worldGenManager != null && worldGenManager.canModModifyStructure(modid, structure);
    }
}
