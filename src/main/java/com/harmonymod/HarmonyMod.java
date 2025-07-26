package com.harmonymod;

import com.harmonymod.config.HarmonyConfig;
import com.harmonymod.util.HarmonyLogger;
import com.harmonymod.worldgen.WorldGenManager;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import net.neoforged.neoforge.eventbus.api.IEventBus;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(HarmonyMod.MODID)
public class HarmonyMod {
    public static final String MODID = "harmony";
    public static final HarmonyLogger LOGGER = new HarmonyLogger(MODID);

    private static WorldGenManager worldGenManager;

    public HarmonyMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register config
        HarmonyConfig.register();

        // Register worldgen manager and API
        worldGenManager = new WorldGenManager(modEventBus);
        HarmonyAPI.initialize(worldGenManager);

        // Register mod lifecycle events
        modEventBus.addListener(this::onConstructMod);
        modEventBus.addListener(this::onCommonSetup);

        LOGGER.info("HarmonyMod initialized.");
    }

    private void onConstructMod(FMLConstructModEvent event) {
        LOGGER.debug("ConstructMod event fired.");
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        LOGGER.debug("CommonSetup event fired.");
    }

    public static WorldGenManager getWorldGenManager() {
        return worldGenManager;
    }
}
