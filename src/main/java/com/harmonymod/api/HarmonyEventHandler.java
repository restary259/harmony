package com.harmonymod.api;

import com.harmonymod.HarmonyMod;
import com.harmonymod.claim.ClaimRegistry;
import com.harmonymod.command.HarmonyCommand;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

/**
 * Handles server lifecycle and command registration for Harmony.
 *
 * <p>{@link ClaimRegistry#reset()} clears only derived enforcement state (the conflict
 * ledger and the modifier-attribution cache), which is rebuilt on the next modifier pass.
 * Explicit {@code HarmonyAPI} claims are intentionally retained across stop and reload,
 * because a mod registers them once from its constructor and is never re-constructed.
 */
public class HarmonyEventHandler {

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        HarmonyCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onServerAboutToStart(ServerAboutToStartEvent event) {
        // Modifier enforcement runs before this event, so conflicts recorded during
        // that pass are deliberately left intact for /harmony conflicts.
        HarmonyMod.LOGGER.info("Server is about to start. Harmony will enforce worldgen boundaries.");
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        HarmonyMod.LOGGER.info("Server stopping. Harmony recorded {} conflict(s) this session.",
                ClaimRegistry.conflictCount());
        ClaimRegistry.reset();
    }
}
