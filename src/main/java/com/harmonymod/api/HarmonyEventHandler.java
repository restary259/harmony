package com.harmonymod.api;

import com.harmonymod.HarmonyMod;
import com.harmonymod.worldgen.WorldGenManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

/**
 * Handles server lifecycle events for Harmony. Registered on the game event bus.
 */
public class HarmonyEventHandler {

    private final WorldGenManager worldGenManager;

    public HarmonyEventHandler(WorldGenManager worldGenManager) {
        this.worldGenManager = worldGenManager;
    }

    @SubscribeEvent
    public void onServerAboutToStart(ServerAboutToStartEvent event) {
        HarmonyMod.LOGGER.info("Server is about to start. Harmony owns {} biome(s) and {} structure(s).",
                worldGenManager.getBiomeClaimCount(), worldGenManager.getStructureClaimCount());
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        HarmonyMod.LOGGER.info("Server started. Harmony is active and enforcing worldgen boundaries.");
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        HarmonyMod.LOGGER.info("Server stopping. Harmony worldgen assignments are retained for the next world.");
    }
}
