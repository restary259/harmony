package com.harmonymod.client;

import com.harmonymod.HarmonyMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * Client-only entrypoint.
 *
 * <p>Registers NeoForge's built-in {@link ConfigurationScreen} as Harmony's config UI,
 * which exposes every {@code ModConfigSpec} option in the mod list with proper widgets,
 * tooltips, and validation, and installs the optional HUD overlay.
 */
@Mod(value = HarmonyMod.MODID, dist = Dist.CLIENT)
public class HarmonyClient {

    public HarmonyClient(ModContainer container, IEventBus modEventBus) {
        container.registerExtensionPoint(IConfigScreenFactory.class,
                (mc, parent) -> new ConfigurationScreen(container, parent));
        modEventBus.addListener(this::onRegisterGuiLayers);
    }

    private void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, HarmonyHudOverlay.ID, new HarmonyHudOverlay());
    }
}
