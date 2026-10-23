package com.harmonymod;

import com.harmonymod.api.HarmonyEvents;
import com.harmonymod.claim.ClaimRegistry;
import com.harmonymod.claim.Conflict;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * Public API for mods to declare and query worldgen ownership.
 *
 * <p>Using this API is optional. Harmony derives ownership from registry namespaces,
 * so a mod that never calls anything here is still attributed and enforced. The API
 * exists for mods that want to participate explicitly: to claim a vanilla target they
 * intend to own, to query permissions before acting, or to observe conflicts.
 *
 * <p>All methods are null-safe: they never throw if Harmony has not initialized.
 */
public final class HarmonyAPI {

    /** API version. Bumped when a method signature changes incompatibly. */
    public static final int API_VERSION = 1;

    private HarmonyAPI() {}

    /**
     * Declares that {@code modid} owns this biome.
     *
     * @return {@code true} if accepted; {@code false} if another mod already owns it
     *         and neither priority nor force-claim applies.
     */
    public static boolean claimBiome(String modid, ResourceLocation biome) {
        return ClaimRegistry.claimBiome(modid, biome);
    }

    /** @see #claimBiome(String, ResourceLocation) */
    public static boolean claimStructure(String modid, ResourceLocation structure) {
        return ClaimRegistry.claimStructure(modid, structure);
    }

    /** Declares ownership of several biomes, stopping at the first refusal. */
    public static void claimBiomes(String modid, Set<ResourceLocation> biomes) {
        for (ResourceLocation biome : biomes) claimBiome(modid, biome);
    }

    /** Declares ownership of several structures, stopping at the first refusal. */
    public static void claimStructures(String modid, Set<ResourceLocation> structures) {
        for (ResourceLocation structure : structures) claimStructure(modid, structure);
    }

    /** Whether {@code modid} is permitted to modify this biome. */
    public static boolean canModifyBiome(String modid, ResourceLocation biome) {
        return ClaimRegistry.canModifyBiome(modid, biome);
    }

    /** Whether {@code modid} is permitted to modify this structure. */
    public static boolean canModifyStructure(String modid, ResourceLocation structure) {
        return ClaimRegistry.canModifyStructure(modid, structure);
    }

    /** The mod that owns this biome, or {@code null} if unclaimed. */
    public static String biomeOwner(ResourceLocation biome) {
        return ClaimRegistry.biomeOwner(biome);
    }

    /** The mod that owns this structure, or {@code null} if unclaimed. */
    public static String structureOwner(ResourceLocation structure) {
        return ClaimRegistry.structureOwner(structure);
    }

    /** Every biome owned by {@code modid}. */
    public static Set<ResourceLocation> biomesOf(String modid) {
        return ClaimRegistry.biomesOf(modid);
    }

    /** Every structure owned by {@code modid}. */
    public static Set<ResourceLocation> structuresOf(String modid) {
        return ClaimRegistry.structuresOf(modid);
    }

    /** Every refused modification recorded this session. */
    public static List<Conflict> conflicts() {
        return ClaimRegistry.conflicts();
    }

    /** Registers a listener for biome claims. */
    public static void onBiomeClaim(BiConsumer<String, ResourceLocation> listener) {
        HarmonyEvents.onBiomeClaim(listener);
    }

    /** Registers a listener for structure claims. */
    public static void onStructureClaim(BiConsumer<String, ResourceLocation> listener) {
        HarmonyEvents.onStructureClaim(listener);
    }

    /** Registers a listener for biome permission checks. */
    public static void onBiomePermissionCheck(BiConsumer<String, ResourceLocation> listener) {
        HarmonyEvents.onBiomePermissionCheck(listener);
    }

    /** Registers a listener for structure permission checks. */
    public static void onStructurePermissionCheck(BiConsumer<String, ResourceLocation> listener) {
        HarmonyEvents.onStructurePermissionCheck(listener);
    }
}
