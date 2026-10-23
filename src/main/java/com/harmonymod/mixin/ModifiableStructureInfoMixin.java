package com.harmonymod.mixin;

import com.harmonymod.claim.ModifierOwnership;
import net.minecraft.core.Holder;
import net.neoforged.neoforge.common.world.ModifiableStructureInfo;
import net.neoforged.neoforge.common.world.StructureModifier;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Structure counterpart of {@link ModifiableBiomeInfoMixin}.
 *
 * @see ModifiableBiomeInfoMixin
 */
@Mixin(value = ModifiableStructureInfo.class, remap = false)
public abstract class ModifiableStructureInfoMixin {

    @Redirect(
            method = "applyStructureModifiers",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/neoforged/neoforge/common/world/StructureModifier;modify(Lnet/minecraft/core/Holder;Lnet/neoforged/neoforge/common/world/StructureModifier$Phase;Lnet/neoforged/neoforge/common/world/ModifiableStructureInfo$StructureInfo$Builder;)V"),
            remap = false)
    private void harmonymod$guardStructureModify(StructureModifier modifier, Holder<Structure> structure,
                                                StructureModifier.Phase phase,
                                                ModifiableStructureInfo.StructureInfo.Builder builder) {
        if (ModifierOwnership.mayApplyStructureModifier(modifier, structure)) {
            modifier.modify(structure, phase, builder);
        }
    }
}
