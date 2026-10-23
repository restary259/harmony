package com.harmony;

import com.harmonymod.config.HarmonyConfig;
import com.harmonymod.util.BiomeStructureUtils;
import com.harmonymod.worldgen.WorldGenManager;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HarmonyBasicTest {

    private static final ResourceLocation PLAINS = ResourceLocation.parse("minecraft:plains");
    private static final ResourceLocation SPECIAL = ResourceLocation.parse("testmod:special_biome");

    @BeforeEach
    void resetConfig() {
        HarmonyConfig.logAllClaims = false;
        HarmonyConfig.allowForceClaim = false;
        HarmonyConfig.priorityMods = Set.of("harmonymod");
        HarmonyConfig.blockVanillaBiomeChanges = true;
        HarmonyConfig.blockVanillaStructureChanges = true;
        HarmonyConfig.openVanillaBiomes = Set.of("minecraft:forest");
        HarmonyConfig.openVanillaStructures = Set.of();
    }

    @Test
    void claimingModOwnsItsBiome() {
        WorldGenManager manager = new WorldGenManager();
        manager.registerModBiomes("testmod", BiomeStructureUtils.resourceSet("testmod:special_biome"));

        assertTrue(manager.canModModifyBiome("testmod", SPECIAL));
        assertEquals(Set.of(SPECIAL), manager.getRegisteredBiomes("testmod"));
        assertEquals(1, manager.getBiomeClaimCount());
    }

    @Test
    void rejectedClaimIsNotRecordedAsOwned() {
        WorldGenManager manager = new WorldGenManager();
        manager.registerModBiomes("firstmod", BiomeStructureUtils.resourceSet("testmod:special_biome"));
        manager.registerModBiomes("secondmod", BiomeStructureUtils.resourceSet("testmod:special_biome"));

        assertTrue(manager.canModModifyBiome("firstmod", SPECIAL));
        assertFalse(manager.canModModifyBiome("secondmod", SPECIAL));
        assertTrue(manager.getRegisteredBiomes("secondmod").isEmpty(),
                "a refused claim must not appear in the mod's registered biomes");
        assertEquals(1, manager.getBiomeClaimCount());
    }

    @Test
    void priorityModCanOverrideExistingClaim() {
        WorldGenManager manager = new WorldGenManager();
        manager.registerModBiomes("othermod", BiomeStructureUtils.resourceSet("testmod:special_biome"));
        manager.registerModBiomes("harmonymod", BiomeStructureUtils.resourceSet("testmod:special_biome"));

        assertTrue(manager.canModModifyBiome("harmonymod", SPECIAL));
        assertFalse(manager.canModModifyBiome("othermod", SPECIAL));
    }

    @Test
    void unclaimedVanillaBiomeIsBlockedUnlessOpenListed() {
        WorldGenManager manager = new WorldGenManager();

        assertFalse(manager.canModModifyBiome("testmod", PLAINS));
        assertTrue(manager.canModModifyBiome("testmod", ResourceLocation.parse("minecraft:forest")));
    }

    @Test
    void unclaimedNonVanillaBiomeIsAllowed() {
        WorldGenManager manager = new WorldGenManager();
        assertTrue(manager.canModModifyBiome("testmod", ResourceLocation.parse("othermod:custom_biome")));
    }

    @Test
    void disablingVanillaBlockingAllowsModification() {
        WorldGenManager manager = new WorldGenManager();
        HarmonyConfig.blockVanillaBiomeChanges = false;

        assertTrue(manager.canModModifyBiome("testmod", PLAINS));
    }
}
