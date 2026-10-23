package com.harmonymod.command;

import com.harmonymod.HarmonyMod;
import com.harmonymod.claim.ClaimRegistry;
import com.harmonymod.claim.Conflict;
import com.harmonymod.claim.ModifierOwnership;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

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
                        .then(Commands.argument("target", StringArgumentType.string())
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
        String raw = StringArgumentType.getString(ctx, "target");
        ResourceLocation id = ResourceLocation.tryParse(raw);
        if (id == null) {
            source.sendFailure(Component.literal("Not a valid id: " + raw));
            return 0;
        }
        String biomeOwner = ClaimRegistry.biomeOwner(id);
        String structureOwner = ClaimRegistry.structureOwner(id);
        String owner = biomeOwner != null ? biomeOwner : structureOwner;
        String kind = biomeOwner != null ? "biome" : (structureOwner != null ? "structure" : "unclaimed");

        source.sendSuccess(() -> Component.literal(
                "%s is %s%s.".formatted(id, kind,
                        owner == null ? "" : " owned by " + owner)), false);

        boolean allowed = biomeOwner != null
                ? ClaimRegistry.canModifyBiome(HarmonyMod.MODID, id)
                : ClaimRegistry.canModifyStructure(HarmonyMod.MODID, id);
        source.sendSuccess(() -> Component.literal(
                "  Harmony itself may modify it: " + allowed), false);
        return 1;
    }

    private static int reload(CommandContext<CommandSourceStack> ctx) {
        ClaimRegistry.reset();
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Harmony: claims and conflict ledger cleared. Ownership will be re-derived on next modifier pass."), true);
        return 1;
    }
}
