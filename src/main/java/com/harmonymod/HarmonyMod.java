package com.harmonymod;

import com.harmonymod.api.HarmonyEventHandler;
import com.harmonymod.config.HarmonyConfig;
import com.harmonymod.worldgen.WorldGenManager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main mod entrypoint for Harmony.
 */
@Mod(HarmonyMod.MODID)
public class HarmonyMod {
    public static final String MODID = "harmonymod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    private static final WorldGenManager WORLD_GEN_MANAGER = new WorldGenManager();

    public HarmonyMod(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Initializing HarmonyMod...");
        modContainer.registerConfig(ModConfig.Type.COMMON, HarmonyConfig.SPEC);
        modEventBus.addListener(this::onConfigLoad);
        HarmonyAPI.initialize(WORLD_GEN_MANAGER);
        NeoForge.EVENT_BUS.register(new HarmonyEventHandler(WORLD_GEN_MANAGER));
        LOGGER.info("HarmonyMod initialized successfully.");
    }

    private void onConfigLoad(ModConfigEvent event) {
        HarmonyConfig.load();
    }

    public static WorldGenManager getWorldGenManager() {
        return WORLD_GEN_MANAGER;
    }
}
