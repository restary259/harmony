package com.harmonymod;

import com.harmonymod.worldgen.WorldGenManager;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

/**
 * Public API for other mods to interact with Harmony.
 * Mods can register for worldgen permissions and query their assignments.
 */
public class HarmonyAPI {
    private static WorldGenManager worldGenManager;

    // Called by HarmonyMod to provide the internal WorldGenManager instance
    public static void initialize(WorldGenManager manager) {
        worldGenManager = manager;
    }

    /**
     * Register the biomes this mod would like to control for world generation.
     */
    public static void registerWorldGenBiomes(String modid, Set<ResourceLocation> biomes) {
        if (worldGenManager != null) {
            worldGenManager.assignBiomesToMod(modid, biomes);
            HarmonyMod.LOGGER.info("Registered biomes for mod '{}': {}", modid, biomes);
        }
    }

    /**
     * Register the structures this mod would like to control for world generation.
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
