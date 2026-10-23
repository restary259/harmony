package com.harmonymod.worldgen;

import com.harmonymod.HarmonyMod;
import com.harmonymod.api.HarmonyEvents;
import com.harmonymod.config.HarmonyConfig;
import com.harmonymod.util.BiomeStructureUtils;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Owns the worldgen claim state and answers permission queries.
 *
 * <p>{@code biomeOwners}/{@code structureOwners} are the authoritative record of who
 * owns an id. The per-mod claim sets are updated only when a claim is actually accepted,
 * so {@link #getRegisteredBiomes(String)} and {@link #canModModifyBiome(String, ResourceLocation)}
 * never disagree.
 */
public class WorldGenManager {
    private final Map<ResourceLocation, String> biomeOwners = new HashMap<>();
    private final Map<ResourceLocation, String> structureOwners = new HashMap<>();
    private final Map<String, Set<ResourceLocation>> modBiomeClaims = new HashMap<>();
    private final Map<String, Set<ResourceLocation>> modStructureClaims = new HashMap<>();

    public synchronized void registerModBiomes(String modid, Set<ResourceLocation> biomes) {
        for (ResourceLocation biome : biomes) {
            String currentOwner = biomeOwners.get(biome);
            if (currentOwner != null && !canOverrideClaim(modid, currentOwner, biome)) {
                HarmonyMod.LOGGER.warn("Mod {} tried to claim biome {} but it's already owned by {}",
                        modid, biome, currentOwner);
                continue;
            }
            biomeOwners.put(biome, modid);
            modBiomeClaims.computeIfAbsent(modid, k -> new HashSet<>()).add(biome);
            HarmonyEvents.fireBiomeClaim(modid, biome);
            if (HarmonyConfig.logAllClaims) HarmonyMod.LOGGER.info("Mod {} claimed biome {}", modid, biome);
        }
    }

    public synchronized void registerModStructures(String modid, Set<ResourceLocation> structures) {
        for (ResourceLocation structure : structures) {
            String currentOwner = structureOwners.get(structure);
            if (currentOwner != null && !canOverrideClaim(modid, currentOwner, structure)) {
                HarmonyMod.LOGGER.warn("Mod {} tried to claim structure {} but it's already owned by {}",
                        modid, structure, currentOwner);
                continue;
            }
            structureOwners.put(structure, modid);
            modStructureClaims.computeIfAbsent(modid, k -> new HashSet<>()).add(structure);
            HarmonyEvents.fireStructureClaim(modid, structure);
            if (HarmonyConfig.logAllClaims) HarmonyMod.LOGGER.info("Mod {} claimed structure {}", modid, structure);
        }
    }

    public boolean canModModifyBiome(String modid, ResourceLocation biome) {
        HarmonyEvents.fireBiomePermissionCheck(modid, biome);
        String owner = biomeOwners.get(biome);
        if (owner == null) {
            return !isBlockedVanilla(biome, HarmonyConfig.blockVanillaBiomeChanges, HarmonyConfig.openVanillaBiomes);
        }
        return modid.equals(owner);
    }

    public boolean canModModifyStructure(String modid, ResourceLocation structure) {
        HarmonyEvents.fireStructurePermissionCheck(modid, structure);
        String owner = structureOwners.get(structure);
        if (owner == null) {
            return !isBlockedVanilla(structure, HarmonyConfig.blockVanillaStructureChanges,
                    HarmonyConfig.openVanillaStructures);
        }
        return modid.equals(owner);
    }

    public synchronized Set<ResourceLocation> getRegisteredBiomes(String modid) {
        return Set.copyOf(modBiomeClaims.getOrDefault(modid, Collections.emptySet()));
    }

    public synchronized Set<ResourceLocation> getRegisteredStructures(String modid) {
        return Set.copyOf(modStructureClaims.getOrDefault(modid, Collections.emptySet()));
    }

    public synchronized int getBiomeClaimCount() {
        return biomeOwners.size();
    }

    public synchronized int getStructureClaimCount() {
        return structureOwners.size();
    }

    private boolean isBlockedVanilla(ResourceLocation id, boolean blockingEnabled, Set<String> openList) {
        return blockingEnabled && BiomeStructureUtils.isVanilla(id.toString()) && !openList.contains(id.toString());
    }

    private boolean canOverrideClaim(String newMod, String oldMod, ResourceLocation id) {
        if (HarmonyConfig.priorityMods.contains(newMod) && !HarmonyConfig.priorityMods.contains(oldMod)) {
            return true;
        }
        if (HarmonyConfig.allowForceClaim) {
            HarmonyMod.LOGGER.warn("Force-claim allowed: {} replaces {} for {}", newMod, oldMod, id);
            return true;
        }
        return false;
    }
}
