package com.harmonymod.api;

import net.minecraft.resources.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.HashSet;
import java.util.Set;

/**
 * Simple event hooks for Harmony mod ecosystem.
 * Allows mods to listen for biome/structure claim events or worldgen permission checks.
 */
public class HarmonyEvents {

    private static final Set<BiConsumer<String, ResourceLocation>> biomeClaimListeners = new HashSet<>();
    private static final Set<BiConsumer<String, ResourceLocation>> structureClaimListeners = new HashSet<>();
    private static final Set<BiConsumer<String, ResourceLocation>> biomePermissionCheckListeners = new HashSet<>();
    private static final Set<BiConsumer<String, ResourceLocation>> structurePermissionCheckListeners = new HashSet<>();

    /**
     * Register a listener for when a mod claims a biome.
     */
    public static void onBiomeClaim(BiConsumer<String, ResourceLocation> listener) {
        biomeClaimListeners.add(listener);
    }

    /**
     * Register a listener for when a mod claims a structure.
     */
    public static void onStructureClaim(BiConsumer<String, ResourceLocation> listener) {
        structureClaimListeners.add(listener);
    }

    /**
     * Register a listener for when a biome permission is checked.
     */
    public static void onBiomePermissionCheck(BiConsumer<String, ResourceLocation> listener) {
        biomePermissionCheckListeners.add(listener);
    }

    /**
     * Register a listener for when a structure permission is checked.
     */
    public static void onStructurePermissionCheck(BiConsumer<String, ResourceLocation> listener) {
        structurePermissionCheckListeners.add(listener);
    }

    // Internal use by Harmony mod
    public static void fireBiomeClaim(String modid, ResourceLocation biome) {
        for (BiConsumer<String, ResourceLocation> listener : biomeClaimListeners) {
            listener.accept(modid, biome);
        }
    }

    public static void fireStructureClaim(String modid, ResourceLocation structure) {
        for (BiConsumer<String, ResourceLocation> listener : structureClaimListeners) {
            listener.accept(modid, structure);
        }
    }

    public static void fireBiomePermissionCheck(String modid, ResourceLocation biome) {
        for (BiConsumer<String, ResourceLocation> listener : biomePermissionCheckListeners) {
            listener.accept(modid, biome);
        }
    }

    public static void fireStructurePermissionCheck(String modid, ResourceLocation structure) {
        for (BiConsumer<String, ResourceLocation> listener : structurePermissionCheckListeners) {
            listener.accept(modid, structure);
        }
    }
}
