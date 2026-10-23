package com.harmonymod.mixin;

import com.harmonymod.claim.ClaimRegistry;
import com.harmonymod.claim.ModifierOwnership;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Enforces biome claims inside NeoForge's own modifier pipeline.
 *
 * <p>Every biome modifier is attributed to the mod that registered it (by registry
 * namespace), so a modifier whose author has no permission for the biome is skipped
 * before its work is applied. This is what makes Harmony an enforcement layer rather
 * than an advisory registry: a mod that never calls the Harmony API is still governed.
 *
 * <p>Fails open. If ownership cannot be determined the modifier is allowed, so a
 * mapping change in a future NeoForge build degrades to vanilla behaviour instead of
 * silently dropping worldgen.
 */
@Mixin(value = ModifiableBiomeInfo.class, remap = false)
public abstract class ModifiableBiomeInfoMixin {

    @Redirect(
            method = "applyBiomeModifiers",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/neoforged/neoforge/common/world/BiomeModifier;modify(Lnet/minecraft/core/Holder;Lnet/neoforged/neoforge/common/world/BiomeModifier$Phase;Lnet/neoforged/neoforge/common/world/ModifiableBiomeInfo$BiomeInfo$Builder;)V"),
            remap = false)
    private void harmonymod$guardBiomeModify(BiomeModifier modifier, Holder<net.minecraft.world.level.biome.Biome> biome,
                                             BiomeModifier.Phase phase,
                                             ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (ModifierOwnership.mayApplyBiomeModifier(modifier, biome)) {
            modifier.modify(biome, phase, builder);
        }
    }
}
