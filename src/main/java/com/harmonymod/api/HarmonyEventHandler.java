package com.harmonymod;

import com.harmonymod.config.HarmonyConfig;
import com.harmonymod.worldgen.WorldGenManager;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.eventbus.api.SubscribeEvent;

/**
 * Handles server lifecycle events for Harmony, such as setup, reload, and shutdown.
 */
public class HarmonyEventHandler {

    private final WorldGenManager worldGenManager;

    public HarmonyEventHandler(WorldGenManager worldGenManager) {
        this.worldGenManager = worldGenManager;
    }

    @SubscribeEvent
    public void onServerAboutToStart(ServerAboutToStartEvent event) {
        HarmonyMod.LOGGER.info("Server is about to start. Harmony worldgen assignments: {}", worldGenManager);
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        HarmonyMod.LOGGER.info("Server started. Harmony is active and enforcing worldgen boundaries.");
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        HarmonyMod.LOGGER.info("Server stopping. Cleaning up Harmony worldgen assignments.");
        // If you need to clear or persist assignments, add logic here.
    }
}
