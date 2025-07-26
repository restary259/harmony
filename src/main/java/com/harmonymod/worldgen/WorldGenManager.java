package com.harmonymod.worldgen;

import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Manages worldgen ownership assignments and permission checks for Harmony.
 * Ensures that only one mod can control a given biome or structure at a time.
 */
public class WorldGenManager {
    // Maps from modid to claimed biomes/structures
    private final Map<String, Set<ResourceLocation>> modBiomeClaims = new HashMap<>();
    private final Map<String, Set<ResourceLocation>> modStructureClaims = new HashMap<>();
    // Reverse maps for quick lookup: biome/structure -> modid
    private final Map<ResourceLocation, String> biomeOwners = new HashMap<>();
    private final Map<ResourceLocation, String> structureOwners = new HashMap<>();

    /**
     * Register a set of biomes for a given mod.
     * Only the first mod to claim a specific biome is allowed to modify it.
     */
    public synchronized void registerModBiomes(String modid, Set<ResourceLocation> biomes) {
        modBiomeClaims.computeIfAbsent(modid, k -> new HashSet<>()).addAll(biomes);
        for (ResourceLocation biome : biomes) {
            biomeOwners.putIfAbsent(biome, modid);
        }
    }

    /**
     * Register a set of structures for a given mod.
     * Only the first mod to claim a specific structure is allowed to modify it.
     */
    public synchronized void registerModStructures(String modid, Set<ResourceLocation> structures) {
        modStructureClaims.computeIfAbsent(modid, k -> new HashSet<>()).addAll(structures);
        for (ResourceLocation structure : structures) {
            structureOwners.putIfAbsent(structure, modid);
        }
    }

    /**
     * Check if the given mod can modify the specified biome.
     */
    public boolean canModModifyBiome(String modid, ResourceLocation biome) {
        return modid.equals(biomeOwners.get(biome));
    }

    /**
     * Check if the given mod can modify the specified structure.
     */
    public boolean canModModifyStructure(String modid, ResourceLocation structure) {
        return modid.equals(structureOwners.get(structure));
    }

    /**
     * Get the set of biomes registered by a mod.
     */
    public Set<ResourceLocation> getRegisteredBiomes(String modid) {
        return modBiomeClaims.getOrDefault(modid, Collections.emptySet());
    }

    /**
     * Get the set of structures registered by a mod.
     */
    public Set<ResourceLocation> getRegisteredStructures(String modid) {
        return modStructureClaims.getOrDefault(modid, Collections.emptySet());
    }

    @Override
    public String toString() {
        return "WorldGenManager{" +
                "biomeOwners=" + biomeOwners +
                ", structureOwners=" + structureOwners +
                '}';
    }
}
