package com.harmonymod.client;

import com.harmonymod.HarmonyMod;
import com.harmonymod.claim.ClaimRegistry;
import com.harmonymod.claim.ModifierOwnership;
import com.harmonymod.config.HarmonyConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Optional on-screen Harmony readout.
 *
 * <p>Renders a compact summary of what Harmony is enforcing: attributed modifier counts
 * and how many modifications have been refused this session. Off by default; enable with
 * {@code general.showHudOverlay} in the config.
 *
 * <p>The client has no access to the server's claim ledger, so the counts shown are the
 * client's own (which match the server in singleplayer). The overlay is informational,
 * not authoritative.
 */
public final class HarmonyHudOverlay implements LayeredDraw.Layer {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(HarmonyMod.MODID, "hud_overlay");

    private static final int MARGIN = 4;
    private static final int LINE_HEIGHT = 10;
    private static final int COLOR_TITLE = 0xFFD7A0FF;
    private static final int COLOR_TEXT = 0xFFFFFFFF;

    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        if (!HarmonyConfig.showHudOverlay) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui) return;
        if (minecraft.getDebugOverlay().showDebugScreen()) return;

        int conflicts = ClaimRegistry.conflictCount();
        int biomes = ModifierOwnership.attributedBiomeModifierCount();
        int structures = ModifierOwnership.attributedStructureModifierCount();

        String title = "Harmony";
        String line1 = "modifiers: " + biomes + " biome / " + structures + " structure";
        String line2 = "blocked: " + conflicts;

        int width = Math.max(minecraft.font.width(title),
                Math.max(minecraft.font.width(line1), minecraft.font.width(line2)));
        int x = MARGIN;
        int y = graphics.guiHeight() - (LINE_HEIGHT * 3) - MARGIN;

        graphics.fill(x - 2, y - 2, x + width + 2, y + LINE_HEIGHT * 3, 0x66000000);
        graphics.drawString(minecraft.font, Component.literal(title), x, y, COLOR_TITLE, true);
        graphics.drawString(minecraft.font, Component.literal(line1), x, y + LINE_HEIGHT, COLOR_TEXT, true);
        graphics.drawString(minecraft.font, Component.literal(line2), x, y + LINE_HEIGHT * 2, COLOR_TEXT, true);
    }
}
