package com.harmonymod;

import net.minecraft.resources.ResourceLocation;
import com.harmonymod.worldgen.WorldGenManager;

import java.util.Collections;
import java.util.Set;

/**
 * Public API for mods to interact with Harmony's worldgen mediation system.
 * Mods should use this class to register their claims and query modification rights.
 */
public class HarmonyAPI {
    private static WorldGenManager worldGenManager;

    /**
     * Called by HarmonyMod during initialization. Do not call directly.
     */
    static void initialize(WorldGenManager manager) {
        HarmonyAPI.worldGenManager = manager;
    }

    /**
     * Register the set of biomes your mod will control for worldgen.
     * @param modid Your mod ID.
     * @param biomes Set of biome ResourceLocations you intend to modify.
     */
    public static void registerWorldGenBiomes(String modid, Set<ResourceLocation> biomes) {
        if (worldGenManager == null) {
            HarmonyMod.LOGGER.error("HarmonyAPI not initialized! Cannot register biomes for {}.", modid);
            return;
        }
        worldGenManager.registerModBiomes(modid, biomes);
    }

    /**
     * Register the set of structures your mod will control for worldgen.
     * @param modid Your mod ID.
     * @param structures Set of structure ResourceLocations you intend to modify.
     */
    public static void registerWorldGenStructures(String modid, Set<ResourceLocation> structures) {
        if (worldGenManager == null) {
            HarmonyMod.LOGGER.error("HarmonyAPI not initialized! Cannot register structures for {}.", modid);
            return;
        }
        worldGenManager.registerModStructures(modid, structures);
    }

    /**
     * Check if your mod is allowed to modify this biome.
     * @param modid Your mod ID.
     * @param biome The biome in question.
     * @return True if your mod can modify, false if not.
     */
    public static boolean canModModifyBiome(String modid, ResourceLocation biome) {
        if (worldGenManager == null) {
            HarmonyMod.LOGGER.error("HarmonyAPI not initialized! Query denied for biome {}.", biome);
            return false;
        }
        return worldGenManager.canModModifyBiome(modid, biome);
    }

    /**
     * Check if your mod is allowed to modify this structure.
     * @param modid Your mod ID.
     * @param structure The structure in question.
     * @return True if your mod can modify, false if not.
     */
    public static boolean canModModifyStructure(String modid, ResourceLocation structure) {
        if (worldGenManager == null) {
            HarmonyMod.LOGGER.error("HarmonyAPI not initialized! Query denied for structure {}.", structure);
            return false;
        }
        return worldGenManager.canModModifyStructure(modid, structure);
    }

    /**
     * Returns the set of biomes registered by the given mod.
     */
    public static Set<ResourceLocation> getRegisteredBiomes(String modid) {
        if (worldGenManager == null) return Collections.emptySet();
        return worldGenManager.getRegisteredBiomes(modid);
    }

    /**
     * Returns the set of structures registered by the given mod.
     */
    public static Set<ResourceLocation> getRegisteredStructures(String modid) {
        if (worldGenManager == null) return Collections.emptySet();
        return worldGenManager.getRegisteredStructures(modid);
    }
}
