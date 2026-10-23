package com.harmony;

import com.harmonymod.api.HarmonyEvents;
import com.harmonymod.claim.ClaimRegistry;
import com.harmonymod.claim.Conflict;
import com.harmonymod.config.HarmonyConfig;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClaimRegistryTest {

    private static final ResourceLocation PLAINS = ResourceLocation.parse("minecraft:plains");
    private static final ResourceLocation FOREST = ResourceLocation.parse("minecraft:forest");
    private static final ResourceLocation SPECIAL = ResourceLocation.parse("testmod:special_biome");
    private static final ResourceLocation VILLAGE = ResourceLocation.parse("minecraft:village");

    @BeforeEach
    void resetState() {
        ClaimRegistry.reset();
        HarmonyEvents.clearListeners();
        HarmonyConfig.logAllClaims = false;
        HarmonyConfig.allowForceClaim = false;
        HarmonyConfig.priorityMods = Set.of("harmonymod");
        HarmonyConfig.blockVanillaBiomeChanges = true;
        HarmonyConfig.blockVanillaStructureChanges = true;
        HarmonyConfig.openVanillaBiomes = Set.of("minecraft:forest");
        HarmonyConfig.openVanillaStructures = Set.of("#minecraft:village");
    }

    // --- claim bookkeeping -------------------------------------------------

    @Test
    void claimingModOwnsItsBiome() {
        assertTrue(ClaimRegistry.claimBiome("testmod", SPECIAL));
        assertEquals("testmod", ClaimRegistry.biomeOwner(SPECIAL));
        assertEquals(Set.of(SPECIAL), ClaimRegistry.biomesOf("testmod"));
    }

    @Test
    void rejectedClaimIsNotRecordedAsOwned() {
        assertTrue(ClaimRegistry.claimBiome("firstmod", SPECIAL));
        assertFalse(ClaimRegistry.claimBiome("secondmod", SPECIAL));

        assertTrue(ClaimRegistry.biomesOf("secondmod").isEmpty(),
                "a refused claim must not appear in the mod's owned biomes");
        assertEquals("firstmod", ClaimRegistry.biomeOwner(SPECIAL));
        assertEquals(1, ClaimRegistry.conflictCount());
    }

    @Test
    void priorityModCanTakeOverAnotherModsClaim() {
        ClaimRegistry.claimBiome("othermod", SPECIAL);
        assertTrue(ClaimRegistry.claimBiome("harmonymod", SPECIAL));

        assertEquals("harmonymod", ClaimRegistry.biomeOwner(SPECIAL));
        assertTrue(ClaimRegistry.canModifyBiome("harmonymod", SPECIAL));
    }

    @Test
    void forceClaimLetsAnyModTakeOver() {
        ClaimRegistry.claimBiome("firstmod", SPECIAL);
        HarmonyConfig.allowForceClaim = true;

        assertTrue(ClaimRegistry.claimBiome("secondmod", SPECIAL));
        assertEquals("secondmod", ClaimRegistry.biomeOwner(SPECIAL));
    }

    // --- permission rules --------------------------------------------------

    @Test
    void unclaimedVanillaBiomeIsBlocked() {
        assertFalse(ClaimRegistry.canModifyBiome("testmod", PLAINS));
    }

    @Test
    void openListedVanillaBiomeIsAllowed() {
        assertTrue(ClaimRegistry.canModifyBiome("testmod", FOREST));
    }

    @Test
    void disablingVanillaBlockingAllowsModification() {
        HarmonyConfig.blockVanillaBiomeChanges = false;
        assertTrue(ClaimRegistry.canModifyBiome("testmod", PLAINS));
    }

    @Test
    void unclaimedNonVanillaBiomeIsAllowed() {
        assertTrue(ClaimRegistry.canModifyBiome("testmod", ResourceLocation.parse("othermod:custom_biome")));
    }

    @Test
    void ownerMayModifyItsOwnClaimAndOthersMayNot() {
        ClaimRegistry.claimBiome("testmod", SPECIAL);

        assertTrue(ClaimRegistry.canModifyBiome("testmod", SPECIAL));
        assertFalse(ClaimRegistry.canModifyBiome("othermod", SPECIAL));
    }

    @Test
    void structurePermissionUsesItsOwnOpenList() {
        assertFalse(ClaimRegistry.canModifyStructure("testmod", VILLAGE));

        HarmonyConfig.openVanillaStructures = Set.of("minecraft:village");
        assertTrue(ClaimRegistry.canModifyStructure("testmod", VILLAGE));
    }

    @Test
    void hashEntryRequiresAHolderToMatch() {
        // "#minecraft:village" is a tag; without the live holder it cannot be resolved,
        // so the target stays closed rather than silently opening every structure.
        assertFalse(ClaimRegistry.canModifyStructure("testmod", VILLAGE));
    }

    @Test
    void structureClaimsAreTrackedSeparatelyFromBiomes() {
        ClaimRegistry.claimStructure("testmod", VILLAGE);

        assertEquals("testmod", ClaimRegistry.structureOwner(VILLAGE));
        assertNull(ClaimRegistry.biomeOwner(VILLAGE),
                "a structure claim must not leak into the biome registry");
        assertEquals(1, ClaimRegistry.allClaimedStructures().size());
        assertEquals(0, ClaimRegistry.allClaimedBiomes().size());
    }

    // --- reload safety -----------------------------------------------------

    @Test
    void resetClearsClaimsAndConflicts() {
        ClaimRegistry.claimBiome("testmod", SPECIAL);
        ClaimRegistry.claimBiome("othermod", SPECIAL);
        assertTrue(ClaimRegistry.conflictCount() > 0);

        ClaimRegistry.reset();

        assertTrue(ClaimRegistry.allClaimedBiomes().isEmpty());
        assertTrue(ClaimRegistry.conflicts().isEmpty());
        assertNull(ClaimRegistry.biomeOwner(SPECIAL));
    }

    // --- events ------------------------------------------------------------

    @Test
    void acceptedClaimFiresBiomeClaimEventOnly() {
        List<String> claims = new ArrayList<>();
        List<String> structureClaims = new ArrayList<>();
        HarmonyEvents.onBiomeClaim((modid, id) -> claims.add(modid + ":" + id));
        HarmonyEvents.onStructureClaim((modid, id) -> structureClaims.add(modid + ":" + id));

        ClaimRegistry.claimBiome("testmod", SPECIAL);

        assertEquals(List.of("testmod:testmod:special_biome"), claims);
        assertTrue(structureClaims.isEmpty(), "a biome claim must not fire the structure listener");
    }

    @Test
    void refusedClaimDoesNotFireEvent() {
        ClaimRegistry.claimBiome("firstmod", SPECIAL);

        List<String> claims = new ArrayList<>();
        HarmonyEvents.onBiomeClaim((modid, id) -> claims.add(modid));

        assertFalse(ClaimRegistry.claimBiome("secondmod", SPECIAL));
        assertTrue(claims.isEmpty(), "a refused claim must not fire the claim event");
    }

    @Test
    void permissionCheckFiresListener() {
        List<String> checks = new ArrayList<>();
        HarmonyEvents.onBiomePermissionCheck((modid, id) -> checks.add(modid + ":" + id));

        ClaimRegistry.canModifyBiome("testmod", PLAINS);

        assertEquals(List.of("testmod:minecraft:plains"), checks);
    }

    // --- conflict ledger ---------------------------------------------------

    @Test
    void conflictRecordsBothParties() {
        ClaimRegistry.claimBiome("firstmod", SPECIAL);
        ClaimRegistry.claimBiome("secondmod", SPECIAL);

        List<Conflict> conflicts = ClaimRegistry.conflicts();
        assertEquals(1, conflicts.size());

        Conflict c = conflicts.get(0);
        assertEquals("secondmod", c.actor());
        assertEquals(SPECIAL, c.target());
        assertEquals("firstmod", c.targetOwner());
        assertEquals(Conflict.Kind.CLAIM_REFUSED, c.kind());
        assertTrue(c.describe().contains("secondmod"));
        assertTrue(c.describe().contains("firstmod"));
    }

    @Test
    void conflictsSnapshotIsImmutable() {
        ClaimRegistry.claimBiome("firstmod", SPECIAL);
        ClaimRegistry.claimBiome("secondmod", SPECIAL);

        List<Conflict> snapshot = ClaimRegistry.conflicts();
        ClaimRegistry.claimBiome("thirdmod", SPECIAL);

        assertEquals(1, snapshot.size(), "an earlier snapshot must not observe later conflicts");
        assertEquals(2, ClaimRegistry.conflictCount());
    }

    @Test
    void repeatedIdenticalConflictIsRecordedOnce() {
        // NeoForge calls a modifier once per phase per biome, so the same logical
        // conflict arrives many times per load and must collapse to one entry.
        Conflict c = new Conflict("testmod", SPECIAL, "unclaimed vanilla",
                Conflict.Kind.BIOME_MODIFIER, "blocked");

        assertTrue(ClaimRegistry.record(c));
        assertFalse(ClaimRegistry.record(c), "a duplicate conflict must not be recorded twice");
        assertFalse(ClaimRegistry.record(new Conflict("testmod", SPECIAL, "unclaimed vanilla",
                Conflict.Kind.BIOME_MODIFIER, "blocked")));

        assertEquals(1, ClaimRegistry.conflictCount());
    }

    @Test
    void distinctConflictsOnSameTargetAreBothRecorded() {
        Conflict blocked = new Conflict("testmod", SPECIAL, "othermod",
                Conflict.Kind.BIOME_MODIFIER, "blocked");
        Conflict refused = new Conflict("testmod", SPECIAL, "othermod",
                Conflict.Kind.CLAIM_REFUSED, "already claimed");

        assertTrue(ClaimRegistry.record(blocked));
        assertTrue(ClaimRegistry.record(refused), "a different conflict kind is a distinct finding");
        assertEquals(2, ClaimRegistry.conflictCount());
    }

    @Test
    void resetClearsDeduplicationState() {
        Conflict c = new Conflict("testmod", SPECIAL, "unclaimed vanilla",
                Conflict.Kind.BIOME_MODIFIER, "blocked");
        ClaimRegistry.record(c);

        ClaimRegistry.reset();

        assertTrue(ClaimRegistry.record(c), "after reset the same conflict must be recordable again");
    }
}
