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
 * Manages all worldgen biome/structure assignments and enforces boundaries.
 */
public class WorldGenManager {
    private final Map<String, Set<ResourceLocation>> modBiomeAssignments = new HashMap<>();
    private final Map<String, Set<ResourceLocation>> modStructureAssignments = new HashMap<>();

    public WorldGenManager(IEventBus modEventBus) {
        modEventBus.addListener(EventPriority.HIGHEST, this::onBiomeLoad);
        modEventBus.addListener(EventPriority.HIGHEST, this::onStructureSpawnListGather);
        HarmonyMod.LOGGER.info("WorldGenManager initialized and event listeners registered.");
    }

    public void assignBiomesToMod(String modid, Set<ResourceLocation> biomes) {
        modBiomeAssignments.computeIfAbsent(modid, k -> new HashSet<>()).addAll(biomes);
    }

    public void assignStructuresToMod(String modid, Set<ResourceLocation> structures) {
        modStructureAssignments.computeIfAbsent(modid, k -> new HashSet<>()).addAll(structures);
    }

    public boolean canModModifyBiome(String modid, ResourceLocation biome) {
        return modBiomeAssignments.getOrDefault(modid, Set.of()).contains(biome);
    }

    public boolean canModModifyStructure(String modid, ResourceLocation structure) {
        return modStructureAssignments.getOrDefault(modid, Set.of()).contains(structure);
    }

    @SubscribeEvent
    public void onBiomeLoad(BiomeLoadingEvent event) {
        HarmonyMod.LOGGER.debug("BiomeLoad: {}", event.getName());
        // Conflict resolution logic can be added here
    }

    @SubscribeEvent
    public void onStructureSpawnListGather(StructureSpawnListGatherEvent event) {
        HarmonyMod.LOGGER.debug("StructureSpawnListGather: {}", event.getStructure());
        // Structure allocation/resolution logic can be added here
    }
}
