package com.harmonymod.config;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;
import java.util.Set;

/**
 * Harmony's configuration, backed by NeoForge's {@link ModConfigSpec}.
 *
 * <p>Values are copied into the static fields by {@link #load()}, which runs on
 * every {@code ModConfigEvent}; code must not read the spec values directly.
 */
public class HarmonyConfig {
    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.BooleanValue LOG_ALL_CLAIMS;
    private static final ModConfigSpec.BooleanValue ALLOW_FORCE_CLAIM;
    private static final ModConfigSpec.BooleanValue SHOW_HUD_OVERLAY;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> PRIORITY_MODS;
    private static final ModConfigSpec.BooleanValue BLOCK_VANILLA_BIOME_CHANGES;
    private static final ModConfigSpec.BooleanValue BLOCK_VANILLA_STRUCTURE_CHANGES;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> OPEN_VANILLA_BIOMES;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> OPEN_VANILLA_STRUCTURES;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        LOG_ALL_CLAIMS = builder
                .comment("Log every biome/structure claim and permission check.")
                .define("general.logAllClaims", false);
        ALLOW_FORCE_CLAIM = builder
                .comment("Allow a mod to override an existing claim on a biome/structure.")
                .define("general.allowForceClaim", false);
        SHOW_HUD_OVERLAY = builder
                .comment("Show a Harmony summary overlay on the HUD (client only).")
                .define("general.showHudOverlay", false);
        PRIORITY_MODS = builder
                .comment("Mod ids whose claims take priority over non-priority mods.")
                .defineListAllowEmpty("general.priorityMods", List.of("harmonymod"), () -> "modid",
                        HarmonyConfig::isNonEmptyString);

        BLOCK_VANILLA_BIOME_CHANGES = builder
                .comment("Block changes to unclaimed vanilla biomes.")
                .define("worldgen.blockVanillaBiomeChanges", true);
        BLOCK_VANILLA_STRUCTURE_CHANGES = builder
                .comment("Block changes to unclaimed vanilla structures.")
                .define("worldgen.blockVanillaStructureChanges", true);
        OPEN_VANILLA_BIOMES = builder
                .comment("Vanilla biomes that are always open for modification.")
                .defineListAllowEmpty("worldgen.openVanillaBiomes",
                        List.of("minecraft:plains", "minecraft:forest"), () -> "minecraft:plains",
                        HarmonyConfig::isResourceLocation);
        OPEN_VANILLA_STRUCTURES = builder
                .comment("Vanilla structures that are always open for modification.",
                        "Entries beginning with '#' are structure tags, e.g. '#minecraft:village'.",
                        "Note: 'minecraft:village' is a tag, not a structure id; use the '#' form to open it.")
                .defineListAllowEmpty("worldgen.openVanillaStructures",
                        List.of("#minecraft:village"), () -> "minecraft:village",
                        HarmonyConfig::isResourceLocation);

        SPEC = builder.build();
    }

    public static boolean logAllClaims;
    public static boolean allowForceClaim;
    public static boolean showHudOverlay = false;
    public static Set<String> priorityMods = Set.of("harmonymod");
    public static boolean blockVanillaBiomeChanges = true;
    public static boolean blockVanillaStructureChanges = true;
    public static Set<String> openVanillaBiomes = Set.of("minecraft:plains", "minecraft:forest");
    public static Set<String> openVanillaStructures = Set.of("#minecraft:village");

    /**
     * Copies the loaded spec values into the static fields. Must be called before
     * any claim or permission check runs.
     */
    public static void load() {
        logAllClaims = LOG_ALL_CLAIMS.get();
        allowForceClaim = ALLOW_FORCE_CLAIM.get();
        showHudOverlay = SHOW_HUD_OVERLAY.get();
        priorityMods = Set.copyOf(PRIORITY_MODS.get());
        blockVanillaBiomeChanges = BLOCK_VANILLA_BIOME_CHANGES.get();
        blockVanillaStructureChanges = BLOCK_VANILLA_STRUCTURE_CHANGES.get();
        openVanillaBiomes = Set.copyOf(OPEN_VANILLA_BIOMES.get());
        openVanillaStructures = Set.copyOf(OPEN_VANILLA_STRUCTURES.get());
    }

    private static boolean isNonEmptyString(Object value) {
        return value instanceof String s && !s.isBlank();
    }

    private static boolean isResourceLocation(Object value) {
        if (!(value instanceof String s)) return false;
        String id = s.startsWith("#") ? s.substring(1) : s;
        return ResourceLocation.tryParse(id) != null;
    }

    private HarmonyConfig() {}
}
