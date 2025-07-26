package com.harmonymod.worldgen;

import com.harmonymod.HarmonyMod;
import com.harmonymod.config.HarmonyConfig;
import com.harmonymod.api.HarmonyEvents;
import com.harmonymod.util.BiomeStructureUtils;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class WorldGenManager {
    private final Map<String, Set<ResourceLocation>> modBiomeClaims = new HashMap<>();
    private final Map<String, Set<ResourceLocation>> modStructureClaims = new HashMap<>();
    private final Map<ResourceLocation, String> biomeOwners = new HashMap<>();
    private final Map<ResourceLocation, String> structureOwners = new HashMap<>();

    public synchronized boolean registerModBiomes(String modid, Set<ResourceLocation> biomes) {
        for (ResourceLocation biome : biomes) {
            String currentOwner = biomeOwners.get(biome);
            if (currentOwner != null && !canOverrideClaim(modid, currentOwner, biome.toString(), true)) {
                HarmonyMod.LOGGER.warn("Mod {} tried to claim biome {} but it's already owned by {}", modid, biome, currentOwner);
                continue;
            }
            biomeOwners.put(biome, modid);
            HarmonyEvents.fireBiomeClaim(modid, biome);
            if (HarmonyConfig.logAllClaims) HarmonyMod.LOGGER.info("Mod {} claimed biome {}", modid, biome);
        }
        modBiomeClaims.computeIfAbsent(modid, k -> new HashSet<>()).addAll(biomes);
        return true;
    }

    public synchronized boolean registerModStructures(String modid, Set<ResourceLocation> structures) {
        for (ResourceLocation structure : structures) {
            String currentOwner = structureOwners.get(structure);
            if (currentOwner != null && !canOverrideClaim(modid, currentOwner, structure.toString(), false)) {
                HarmonyMod.LOGGER.warn("Mod {} tried to claim structure {} but it's already owned by {}", modid, structure, currentOwner);
                continue;
            }
            structureOwners.put(structure, modid);
            HarmonyEvents.fireStructureClaim(modid, structure);
            if (HarmonyConfig.logAllClaims) HarmonyMod.LOGGER.info("Mod {} claimed structure {}", modid, structure);
        }
        modStructureClaims.computeIfAbsent(modid, k -> new HashSet<>()).addAll(structures);
        return true;
    }

    public boolean canModModifyBiome(String modid, ResourceLocation biome) {
        HarmonyEvents.fireBiomePermissionCheck(modid, biome);
        String owner = biomeOwners.get(biome);
        if (owner == null) {
            if (HarmonyConfig.blockVanillaBiomeChanges && BiomeStructureUtils.isVanilla(biome.toString())
                    && !HarmonyConfig.openVanillaBiomes.contains(biome.toString())) {
                return false;
            }
            return true;
        }
        return modid.equals(owner);
    }

    public boolean canModModifyStructure(String modid, ResourceLocation structure) {
        HarmonyEvents.fireStructurePermissionCheck(modid, structure);
        String owner = structureOwners.get(structure);
        if (owner == null) {
            if (HarmonyConfig.blockVanillaStructureChanges && BiomeStructureUtils.isVanilla(structure.toString())
                    && !HarmonyConfig.openVanillaStructures.contains(structure.toString())) {
                return false;
            }
            return true;
        }
        return modid.equals(owner);
    }

    public Set<ResourceLocation> getRegisteredBiomes(String modid) {
        return modBiomeClaims.getOrDefault(modid, Collections.emptySet());
    }

    public Set<ResourceLocation> getRegisteredStructures(String modid) {
        return modStructureClaims.getOrDefault(modid, Collections.emptySet());
    }

    private boolean canOverrideClaim(String newMod, String oldMod, String id, boolean isBiome) {
        // Priority mods logic
        if (HarmonyConfig.priorityMods.contains(newMod) && !HarmonyConfig.priorityMods.contains(oldMod)) {
            return true;
        }
        // Config option
        if (HarmonyConfig.allowForceClaim) {
            HarmonyMod.LOGGER.warn("Force-claim allowed: {} replaces {} for {}", newMod, oldMod, id);
            return true;
        }
        return false;
    }
}
