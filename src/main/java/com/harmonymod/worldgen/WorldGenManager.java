package com.harmonymod.worldgen;

import com.harmonymod.HarmonyMod;
import net.neoforged.neoforge.event.level.levelgen.BiomeLoadingEvent;
import net.neoforged.neoforge.event.level.levelgen.StructureSpawnListGatherEvent;
import net.neoforged.neoforge.event.EventPriority;
import net.neoforged.neoforge.eventbus.api.SubscribeEvent;
import net.neoforged.neoforge.eventbus.api.IEventBus;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Manages and coordinates world generation requests from mods.
 * Prevents conflicts by assigning biomes/chunks/structures to mods.
 */
public class WorldGenManager {
    // Keeps track of which mod is allowed to control which biome/chunk/structure
    private final Map<String, Set<ResourceLocation>> modBiomeAssignments = new HashMap<>();
    private final Map<String, Set<ResourceLocation>> modStructureAssignments = new HashMap<>();

    public WorldGenManager(IEventBus modEventBus) {
        modEventBus.addListener(EventPriority.HIGHEST, this::onBiomeLoad);
        modEventBus.addListener(EventPriority.HIGHEST, this::onStructureSpawnListGather);
        HarmonyMod.LOGGER.info("WorldGenManager initialized and event listeners registered.");
    }

    // Example: Register allowed biomes for a mod
    public void assignBiomesToMod(String modid, Set<ResourceLocation> biomes) {
        modBiomeAssignments.computeIfAbsent(modid, k -> new HashSet<>()).addAll(biomes);
    }

    // Example: Register allowed structures for a mod
    public void assignStructuresToMod(String modid, Set<ResourceLocation> structures) {
        modStructureAssignments.computeIfAbsent(modid, k -> new HashSet<>()).addAll(structures);
    }

    // Checks if a mod is allowed to modify a biome
    public boolean canModModifyBiome(String modid, ResourceLocation biome) {
        return modBiomeAssignments.getOrDefault(modid, Set.of()).contains(biome);
    }

    // Checks if a mod is allowed to spawn/modify a structure
    public boolean canModModifyStructure(String modid, ResourceLocation structure) {
        return modStructureAssignments.getOrDefault(modid, Set.of()).contains(structure);
    }

    // Listener for biome loading: decides if a mod can change biomes
    @SubscribeEvent
    public void onBiomeLoad(BiomeLoadingEvent event) {
        // Example: Here you could check which mod is firing the event and block/allow based on assignments
        // This requires mods to use Harmony's API for full effect
        HarmonyMod.LOGGER.debug("BiomeLoad: " + event.getName());
        // Implement conflict resolution logic here.
    }

    // Listener for structure spawn: prevents overlapping structures
    @SubscribeEvent
    public void onStructureSpawnListGather(StructureSpawnListGatherEvent event) {
        // Example: Prevent two mods from registering structures in the same spot
        HarmonyMod.LOGGER.debug("StructureSpawnListGather: " + event.getStructure());
        // Implement structure allocation/resolution logic here.
    }
}
