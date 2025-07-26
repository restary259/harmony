package com.harmonymod.api;

import com.harmonymod.HarmonyMod;
import com.harmonymod.HarmonyAPI;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.eventbus.api.SubscribeEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.Set;

/**
 * Example event handler that demonstrates how other mods can hook into Harmony.
 * This can be expanded to allow mods to request worldgen permissions at runtime or listen for assignments.
 */
public class HarmonyEventHandler {

    // Example: Listen for world load and log allowed biomes for this mod (as an example)
    @SubscribeEvent
    public void onWorldLoad(LevelEvent.Load event) {
        Level level = event.getLevel();
        if (!(level instanceof ServerLevel)) return;

        String modid = "examplemod"; // This would be the actual modid of the requesting mod
        Set<ResourceLocation> assignedBiomes = Set.of(); // You’d get this from config or mod logic

        // Register with Harmony (other mods would call this themselves)
        HarmonyAPI.registerWorldGenBiomes(modid, assignedBiomes);

        HarmonyMod.LOGGER.info("[Harmony] {} registered biomes for worldgen: {}", modid, assignedBiomes);
    }
}
