package com.harmonymod.claim;

import net.minecraft.resources.ResourceLocation;

/**
 * Records that a mod attempted to modify a worldgen target it does not own.
 *
 * <p>Kept so operators can answer "why didn't mod X's feature show up?" without
 * digging through server logs.
 */
public record Conflict(
        String actor,
        ResourceLocation target,
        String targetOwner,
        Kind kind,
        String detail) {

    public enum Kind {
        /** A biome modifier was skipped. */
        BIOME_MODIFIER,
        /** A structure modifier was skipped. */
        STRUCTURE_MODIFIER,
        /** A claim was refused because another mod already owns the target. */
        CLAIM_REFUSED
    }

    public String describe() {
        return "%s tried to modify %s (owned by %s) via %s%s"
                .formatted(actor, target, targetOwner, kind, detail.isEmpty() ? "" : " [" + detail + "]");
    }
}
