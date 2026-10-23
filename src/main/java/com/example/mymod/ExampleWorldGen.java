package com.example.mymod;

import com.harmonymod.HarmonyAPI;
import net.minecraft.resources.ResourceLocation;

/**
 * Example consumer showing how another mod would guard a biome edit with Harmony.
 */
public class ExampleWorldGen {
    public static void tryModifyBiome() {
        String myModId = "yourmodid";
        ResourceLocation targetBiome = ResourceLocation.parse("minecraft:plains");

        if (HarmonyAPI.canModModifyBiome(myModId, targetBiome)) {
            // Safe to add features or make changes to this biome.
        }
    }
}
