package com.harmonymod.claim;

import com.harmonymod.HarmonyMod;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.StructureModifier;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Attributes a modifier instance to the mod that registered it.
 *
 * <p>NeoForge applies modifiers as a bare list, having discarded the registry keys
 * that identify their authors. The keys are still present in the datapack registry,
 * so ownership is recovered from there by identity and cached until the registry
 * instance changes (which happens on datapack reload).
 *
 * <p>Every lookup fails open: an unknown modifier is permitted. Enforcement is only
 * skipped for modifiers whose author cannot be determined, which keeps a NeoForge
 * mapping change from silently deleting worldgen.
 */
public final class ModifierOwnership {

    private static RegistryAccess cachedAccess;
    private static Map<BiomeModifier, String> biomeOwners = Map.of();
    private static Map<StructureModifier, String> structureOwners = Map.of();

    private ModifierOwnership() {}

    /** Drops the cache. Called on datapack/server reload. */
    public static void invalidate() {
        cachedAccess = null;
        biomeOwners = Map.of();
        structureOwners = Map.of();
    }

    private static synchronized void ensureLoaded() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        RegistryAccess access = server.registryAccess();
        if (access == cachedAccess) return;

        Map<BiomeModifier, String> biomes = new IdentityHashMap<>();
        Registry<BiomeModifier> biomeRegistry =
                access.registryOrThrow(NeoForgeRegistries.Keys.BIOME_MODIFIERS);
        for (Holder.Reference<BiomeModifier> holder : biomeRegistry.holders().toList()) {
            holder.unwrapKey().ifPresent(key -> biomes.put(holder.value(), key.location().getNamespace()));
        }

        Map<StructureModifier, String> structures = new IdentityHashMap<>();
        Registry<StructureModifier> structureRegistry =
                access.registryOrThrow(NeoForgeRegistries.Keys.STRUCTURE_MODIFIERS);
        for (Holder.Reference<StructureModifier> holder : structureRegistry.holders().toList()) {
            holder.unwrapKey().ifPresent(key -> structures.put(holder.value(), key.location().getNamespace()));
        }

        biomeOwners = biomes;
        structureOwners = structures;
        cachedAccess = access;
        HarmonyMod.LOGGER.debug("Attributed {} biome modifier(s) and {} structure modifier(s) to owners.",
                biomes.size(), structures.size());
    }

    /** @return the mod id owning this modifier, or {@code null} if it cannot be determined. */
    public static String ownerOf(BiomeModifier modifier) {
        ensureLoaded();
        return biomeOwners.get(modifier);
    }

    /** @return the mod id owning this modifier, or {@code null} if it cannot be determined. */
    public static String ownerOf(StructureModifier modifier) {
        ensureLoaded();
        return structureOwners.get(modifier);
    }

    /**
     * Whether this biome modifier may run against this biome.
     *
     * @return {@code true} if the owning mod is permitted, or if the owner is unknown.
     */
    public static boolean mayApplyBiomeModifier(BiomeModifier modifier, Holder<Biome> biome) {
        return biome.unwrapKey().map(biomeKey -> {
            ResourceLocation biomeId = biomeKey.location();
            String owner = ownerOf(modifier);
            if (owner == null) return true;
            if (ClaimRegistry.canModifyBiome(owner, biomeId, biome)) return true;

            String biomeOwner = ClaimRegistry.biomeOwner(biomeId);
            boolean first = ClaimRegistry.record(new Conflict(owner, biomeId,
                    biomeOwner == null ? "unclaimed vanilla" : biomeOwner,
                    Conflict.Kind.BIOME_MODIFIER, "blocked"));
            if (first) {
                HarmonyMod.LOGGER.warn("Blocked biome modifier from {} on {} (owner: {})",
                        owner, biomeId, biomeOwner == null ? "unclaimed vanilla" : biomeOwner);
            }
            return false;
        }).orElse(true);
    }

    /**
     * Whether this structure modifier may run against this structure.
     *
     * @return {@code true} if the owning mod is permitted, or if the owner is unknown.
     */
    public static boolean mayApplyStructureModifier(StructureModifier modifier, Holder<Structure> structure) {
        return structure.unwrapKey().map(structureKey -> {
            ResourceLocation structureId = structureKey.location();
            String owner = ownerOf(modifier);
            if (owner == null) return true;
            if (ClaimRegistry.canModifyStructure(owner, structureId, structure)) return true;

            String structureOwner = ClaimRegistry.structureOwner(structureId);
            boolean first = ClaimRegistry.record(new Conflict(owner, structureId,
                    structureOwner == null ? "unclaimed vanilla" : structureOwner,
                    Conflict.Kind.STRUCTURE_MODIFIER, "blocked"));
            if (first) {
                HarmonyMod.LOGGER.warn("Blocked structure modifier from {} on {} (owner: {})",
                        owner, structureId, structureOwner == null ? "unclaimed vanilla" : structureOwner);
            }
            return false;
        }).orElse(true);
    }

    /** Registries used for attribution, exposed for diagnostics. */
    public static int attributedBiomeModifierCount() {
        ensureLoaded();
        return biomeOwners.size();
    }

    public static int attributedStructureModifierCount() {
        ensureLoaded();
        return structureOwners.size();
    }
}
