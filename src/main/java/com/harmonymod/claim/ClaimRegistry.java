package com.harmonymod.claim;

import com.harmonymod.HarmonyMod;
import com.harmonymod.api.HarmonyEvents;
import com.harmonymod.config.HarmonyConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The authoritative record of who owns which biome/structure, plus the ledger of
 * refused modifications.
 *
 * <p>Ownership is derived from namespace: {@code othermod:add_ores} belongs to
 * {@code othermod}. Because attribution comes from registry ids rather than from the
 * other mod calling Harmony, enforcement works even against mods that have never
 * heard of Harmony.
 *
 * <p>State is reload-safe: {@link #reset()} clears derived ownership so a datapack
 * reload re-derives from scratch instead of accumulating stale claims.
 */
public final class ClaimRegistry {

    private static final Map<ResourceLocation, Claim> BIOME_CLAIMS = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, Claim> STRUCTURE_CLAIMS = new ConcurrentHashMap<>();
    private static final List<Conflict> CONFLICTS = Collections.synchronizedList(new ArrayList<>());
    private static final Set<String> CONFLICT_KEYS = ConcurrentHashMap.newKeySet();

    private ClaimRegistry() {}

    /** Clears all derived ownership and the conflict ledger. Called on datapack/server reload. */
    public static void reset() {
        BIOME_CLAIMS.clear();
        STRUCTURE_CLAIMS.clear();
        CONFLICTS.clear();
        CONFLICT_KEYS.clear();
        ModifierOwnership.invalidate();
    }

    /**
     * Registers an explicit biome claim.
     *
     * @return {@code true} if accepted; {@code false} if refused because another mod
     *         already owns the biome and override is not permitted.
     */
    public static boolean claimBiome(String modid, ResourceLocation biome) {
        Claim existing = BIOME_CLAIMS.get(biome);
        if (existing != null && !existing.ownedBy(modid) && !mayOverride(modid, existing.owner())) {
            refuse(modid, biome, existing.owner(), "biome", Conflict.Kind.CLAIM_REFUSED);
            return false;
        }
        BIOME_CLAIMS.put(biome, new Claim(biome, modid));
        HarmonyEvents.fireBiomeClaim(modid, biome);
        logClaim(modid, "biome", biome);
        return true;
    }

    /** @see #claimBiome(String, ResourceLocation) */
    public static boolean claimStructure(String modid, ResourceLocation structure) {
        Claim existing = STRUCTURE_CLAIMS.get(structure);
        if (existing != null && !existing.ownedBy(modid) && !mayOverride(modid, existing.owner())) {
            refuse(modid, structure, existing.owner(), "structure", Conflict.Kind.CLAIM_REFUSED);
            return false;
        }
        STRUCTURE_CLAIMS.put(structure, new Claim(structure, modid));
        HarmonyEvents.fireStructureClaim(modid, structure);
        logClaim(modid, "structure", structure);
        return true;
    }



    private static void refuse(String modid, ResourceLocation id, String owner, String what, Conflict.Kind kind) {
        if (record(new Conflict(modid, id, owner, kind, "already claimed"))) {
            HarmonyMod.LOGGER.warn("Mod {} tried to claim {} {} but it is owned by {}", modid, what, id, owner);
        }
    }

    private static void logClaim(String modid, String what, ResourceLocation id) {
        if (HarmonyConfig.logAllClaims) {
            HarmonyMod.LOGGER.info("Mod {} claimed {} {}", modid, what, id);
        }
    }

    /** The mod that owns this biome, or {@code null} if unclaimed. */
    public static String biomeOwner(ResourceLocation biome) {
        Claim c = BIOME_CLAIMS.get(biome);
        return c == null ? null : c.owner();
    }

    /** The mod that owns this structure, or {@code null} if unclaimed. */
    public static String structureOwner(ResourceLocation structure) {
        Claim c = STRUCTURE_CLAIMS.get(structure);
        return c == null ? null : c.owner();
    }

    /**
     * Whether {@code modid} is allowed to modify this biome.
     *
     * <p>Rules, in order:
     * <ol>
     *   <li>Unclaimed non-vanilla biomes are open.</li>
     *   <li>Unclaimed vanilla biomes are closed unless listed in {@code openVanillaBiomes}
     *       (and only while {@code blockVanillaBiomeChanges} is enabled).</li>
     *   <li>A claim by the same mod is allowed.</li>
     *   <li>A claim by another mod is allowed only for priority mods or with force-claim on.</li>
     * </ol>
     */
    public static boolean canModifyBiome(String modid, ResourceLocation biome) {
        return canModifyBiome(modid, biome, null);
    }

    /**
     * As {@link #canModifyBiome(String, ResourceLocation)}, but also honours tag entries
     * in the open list (for example {@code #minecraft:is_forest}) by testing the holder.
     *
     * @param holder the biome holder being modified, or {@code null} if unavailable
     */
    public static boolean canModifyBiome(String modid, ResourceLocation biome, Holder<Biome> holder) {
        boolean allowed = decide(BIOME_CLAIMS, modid, biome, HarmonyConfig.openVanillaBiomes,
                HarmonyConfig.blockVanillaBiomeChanges, holder, Registries.BIOME);
        HarmonyEvents.fireBiomePermissionCheck(modid, biome);
        return allowed;
    }

    /** @see #canModifyBiome(String, ResourceLocation) */
    public static boolean canModifyStructure(String modid, ResourceLocation structure) {
        return canModifyStructure(modid, structure, null);
    }

    /** @see #canModifyBiome(String, ResourceLocation, Holder) */
    public static boolean canModifyStructure(String modid, ResourceLocation structure, Holder<Structure> holder) {
        boolean allowed = decide(STRUCTURE_CLAIMS, modid, structure, HarmonyConfig.openVanillaStructures,
                HarmonyConfig.blockVanillaStructureChanges, holder, Registries.STRUCTURE);
        HarmonyEvents.fireStructurePermissionCheck(modid, structure);
        return allowed;
    }

    private static <T> boolean decide(Map<ResourceLocation, Claim> claims, String modid, ResourceLocation id,
                                      Set<String> openVanilla, boolean blockVanilla,
                                      Holder<T> holder, ResourceKey<Registry<T>> registryKey) {
        Claim existing = claims.get(id);
        if (existing != null) {
            return existing.ownedBy(modid) || mayOverride(modid, existing.owner());
        }
        boolean vanilla = "minecraft".equals(id.getNamespace());
        if (!vanilla) return true;
        if (isOpenListed(id, openVanilla, holder, registryKey)) return true;
        return !blockVanilla;
    }

    /**
     * Whether the open list covers this target, by exact id or by tag.
     *
     * <p>Open-list entries beginning with {@code #} are treated as registry tags, so
     * {@code #minecraft:village} opens every village variant rather than expecting a
     * structure literally named {@code minecraft:village}.
     */
    private static <T> boolean isOpenListed(ResourceLocation id, Set<String> openVanilla,
                                            Holder<T> holder, ResourceKey<Registry<T>> registryKey) {
        if (openVanilla.contains(id.toString())) return true;
        if (holder == null) return false;
        for (String entry : openVanilla) {
            if (!entry.startsWith("#")) continue;
            ResourceLocation tagId = ResourceLocation.tryParse(entry.substring(1));
            if (tagId != null && holder.is(TagKey.create(registryKey, tagId))) return true;
        }
        return false;
    }

    private static boolean mayOverride(String modid, String currentOwner) {
        if (HarmonyConfig.allowForceClaim) return true;
        return HarmonyConfig.priorityMods.contains(modid) && !HarmonyConfig.priorityMods.contains(currentOwner);
    }

    /** Every biome currently owned by {@code modid}. */
    public static Set<ResourceLocation> biomesOf(String modid) {
        return ownedBy(BIOME_CLAIMS, modid);
    }

    /** Every structure currently owned by {@code modid}. */
    public static Set<ResourceLocation> structuresOf(String modid) {
        return ownedBy(STRUCTURE_CLAIMS, modid);
    }

    private static Set<ResourceLocation> ownedBy(Map<ResourceLocation, Claim> claims, String modid) {
        Set<ResourceLocation> out = new LinkedHashSet<>();
        claims.forEach((id, claim) -> {
            if (claim.ownedBy(modid)) out.add(id);
        });
        return out;
    }

    public static Set<ResourceLocation> allClaimedBiomes() {
        return new LinkedHashSet<>(BIOME_CLAIMS.keySet());
    }

    public static Set<ResourceLocation> allClaimedStructures() {
        return new LinkedHashSet<>(STRUCTURE_CLAIMS.keySet());
    }

    /**
     * Records a conflict, collapsing repeats.
     *
     * <p>NeoForge invokes a modifier once per phase per biome, so the same logical
     * conflict is observed many times per load. Only the first occurrence of each
     * (actor, target, kind) triple is kept, which keeps the ledger readable.
     *
     * @return {@code true} if this conflict was newly recorded.
     */
    public static boolean record(Conflict conflict) {
        String key = conflict.actor() + "\u0000" + conflict.target() + "\u0000" + conflict.kind();
        if (!CONFLICT_KEYS.add(key)) return false;
        CONFLICTS.add(conflict);
        return true;
    }

    /** Immutable snapshot of every refused modification so far. */
    public static List<Conflict> conflicts() {
        synchronized (CONFLICTS) {
            return List.copyOf(CONFLICTS);
        }
    }

    public static int conflictCount() {
        return CONFLICTS.size();
    }
}
