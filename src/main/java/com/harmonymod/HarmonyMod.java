package com.harmonymod;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

/**
 * Harmony mod main class. Coordinates and mediates between other mods for worldgen and performance.
 */
@Mod(HarmonyMod.MODID)
public class HarmonyMod {
    public static final String MODID = "harmony";
    public static final Logger LOGGER = LogUtils.getLogger();

    public HarmonyMod() {
        LOGGER.info("Harmony mod initialized! Ready to coordinate mods for stability and performance.");
        // Initialization code and event bus registration will go here in future steps.
    }
}
