package com.harmonymod;

import com.harmonymod.config.HarmonyConfig;
import com.harmonymod.worldgen.WorldGenManager;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main mod entrypoint for Harmony.
 * Responsible for initialization, config loading, and API setup.
 */
@Mod(HarmonyMod.MODID)
public class HarmonyMod {
    public static final String MODID = "harmonymod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    private static WorldGenManager worldGenManager;

    public HarmonyMod() {
        LOGGER.info("Initializing HarmonyMod...");

        // Load configuration
        HarmonyConfig.register();

        // Setup worldgen management and API
        worldGenManager = new WorldGenManager();
        HarmonyAPI.initialize(worldGenManager);

        LOGGER.info("HarmonyMod initialized successfully.");
    }

    /**
     * For internal use and testing.
     */
    public static WorldGenManager getWorldGenManager() {
        return worldGenManager;
    }
}
