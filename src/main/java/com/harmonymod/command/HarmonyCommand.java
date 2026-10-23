package com.harmonymod.command;

import com.harmonymod.HarmonyMod;
import com.harmonymod.claim.ClaimRegistry;
import com.harmonymod.claim.Conflict;
import com.harmonymod.claim.ModifierOwnership;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.List;

/**
 * The {@code /harmony} in-game command tree.
 *
 * <p>Gives operators visibility into what Harmony is enforcing: which mods own which
 * targets, what has been blocked, and why a specific target is (or is not) modifiable.
 */
public final class HarmonyCommand {

    private HarmonyCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("harmony")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("status")
                        .executes(HarmonyCommand::status))
                .then(Commands.literal("list")
                        .executes(HarmonyCommand::list))
                .then(Commands.literal("conflicts")
                        .executes(HarmonyCommand::conflicts))
                .then(Commands.literal("why")
                        .then(Commands.argument("target", ResourceLocationArgument.id())
                                .executes(HarmonyCommand::why)))
                .then(Commands.literal("reload")
                        .executes(HarmonyCommand::reload)));
    }

    private static int status(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        source.sendSuccess(() -> Component.literal(
                "Harmony: %d biome claim(s), %d structure claim(s), %d conflict(s), %d/%d modifier(s) attributed."
                        .formatted(
                                ClaimRegistry.allClaimedBiomes().size(),
                                ClaimRegistry.allClaimedStructures().size(),
                                ClaimRegistry.conflictCount(),
                                ModifierOwnership.attributedBiomeModifierCount(),
                                ModifierOwnership.attributedStructureModifierCount())), false);
        return 1;
    }

    private static int list(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        var biomes = ClaimRegistry.allClaimedBiomes();
        var structures = ClaimRegistry.allClaimedStructures();
        if (biomes.isEmpty() && structures.isEmpty()) {
            source.sendSuccess(() -> Component.literal(
                    "Harmony: no explicit claims. Modifiers are still attributed by namespace and enforced."), false);
            return 1;
        }
        biomes.forEach(id -> source.sendSuccess(() -> Component.literal(
                "  biome %s -> %s".formatted(id, ClaimRegistry.biomeOwner(id))), false));
        structures.forEach(id -> source.sendSuccess(() -> Component.literal(
                "  structure %s -> %s".formatted(id, ClaimRegistry.structureOwner(id))), false));
        return biomes.size() + structures.size();
    }

    private static int conflicts(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        List<Conflict> conflicts = ClaimRegistry.conflicts();
        if (conflicts.isEmpty()) {
            source.sendSuccess(() -> Component.literal("Harmony: no conflicts recorded."), false);
            return 0;
        }
        for (Conflict c : conflicts) {
            source.sendSuccess(() -> Component.literal("  " + c.describe()), false);
        }
        return conflicts.size();
    }

    private static int why(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        ResourceLocation id = ResourceLocationArgument.getId(ctx, "target");

        // Kind comes from registry membership, not from claim state: an unclaimed
        // target still has to be classified correctly to consult the right rules.
        Registry<Biome> biomes = source.registryAccess().registryOrThrow(Registries.BIOME);
        Registry<Structure> structures = source.registryAccess().registryOrThrow(Registries.STRUCTURE);
        boolean isBiome = biomes.containsKey(id);
        boolean isStructure = structures.containsKey(id);

        if (!isBiome && !isStructure) {
            source.sendFailure(Component.literal(
                    id + " is neither a biome nor a structure in the current registries."));
            return 0;
        }

        if (isBiome && isStructure) {
            // A target cannot be both in vanilla, but report both verdicts if it is.
            report(source, id, "biome", ClaimRegistry.biomeOwner(id),
                    ClaimRegistry.canModifyBiome(HarmonyMod.MODID, id, biomes.getHolder(id).orElse(null)));
            report(source, id, "structure", ClaimRegistry.structureOwner(id),
                    ClaimRegistry.canModifyStructure(HarmonyMod.MODID, id, structures.getHolder(id).orElse(null)));
            return 1;
        }

        if (isBiome) {
            report(source, id, "biome", ClaimRegistry.biomeOwner(id),
                    ClaimRegistry.canModifyBiome(HarmonyMod.MODID, id, biomes.getHolder(id).orElse(null)));
        } else {
            report(source, id, "structure", ClaimRegistry.structureOwner(id),
                    ClaimRegistry.canModifyStructure(HarmonyMod.MODID, id, structures.getHolder(id).orElse(null)));
        }
        return 1;
    }

    private static void report(CommandSourceStack source, ResourceLocation id, String kind,
                               String owner, boolean allowed) {
        source.sendSuccess(() -> Component.literal(
                "%s is a %s%s.".formatted(id, kind, owner == null ? " (unclaimed)" : " owned by " + owner)), false);
        source.sendSuccess(() -> Component.literal(
                "  Harmony itself may modify it: " + allowed), false);
    }

    private static int reload(CommandContext<CommandSourceStack> ctx) {
        ClaimRegistry.reset();
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Harmony: conflict ledger and modifier attribution cleared; explicit claims kept. "
                        + "Attribution is re-derived on the next modifier pass."), true);
        return 1;
    }
}
