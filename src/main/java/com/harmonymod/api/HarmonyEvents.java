package com.harmonymod.api;

import net.minecraft.resources.ResourceLocation;
import java.util.Set;
import java.util.HashSet;
import java.util.function.BiConsumer;

public class HarmonyEvents {

    private static final Set<BiConsumer<String, ResourceLocation>> biomeClaimListeners = new HashSet<>();
    private static final Set<BiConsumer<String, ResourceLocation>> structureClaimListeners = new HashSet<>();
    private static final Set<BiConsumer<String, ResourceLocation>> biomePermissionCheckListeners = new HashSet<>();
    private static final Set<BiConsumer<String, ResourceLocation>> structurePermissionCheckListeners = new HashSet<>();

    public static void onBiomeClaim(BiConsumer<String, ResourceLocation> listener) {
        biomeClaimListeners.add(listener);
    }
    public static void onStructureClaim(BiConsumer<String, ResourceLocation> listener) {
        structureClaimListeners.add(listener);
    }
    public static void onBiomePermissionCheck(BiConsumer<String, ResourceLocation> listener) {
        biomePermissionCheckListeners.add(listener);
    }
    public static void onStructurePermissionCheck(BiConsumer<String, ResourceLocation> listener) {
        structurePermissionCheckListeners.add(listener);
    }

    public static void fireBiomeClaim(String modid, ResourceLocation biome) {
        for (BiConsumer<String, ResourceLocation> listener : biomeClaimListeners) listener.accept(modid, biome);
    }
    public static void fireStructureClaim(String modid, ResourceLocation structure) {
        for (BiConsumer<String, ResourceLocation> listener : structureClaimListeners) listener.accept(modid, structure);
    }
    public static void fireBiomePermissionCheck(String modid, ResourceLocation biome) {
        for (BiConsumer<String, ResourceLocation> listener : biomePermissionCheckListeners) listener.accept(modid, biome);
    }
    public static void fireStructurePermissionCheck(String modid, ResourceLocation structure) {
        for (BiConsumer<String, ResourceLocation> listener : structurePermissionCheckListeners) listener.accept(modid, structure);
    }

    private HarmonyEvents() {}
}
