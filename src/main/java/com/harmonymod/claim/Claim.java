package com.harmonymod.claim;

import net.minecraft.resources.ResourceLocation;

/**
 * A single worldgen target (biome or structure) and the mod that owns it.
 *
 * <p>Ownership is authoritative: an {@code id} appears here at most once, so
 * "who owns this?" always has a single answer.
 */
public record Claim(ResourceLocation id, String owner) {

    public boolean ownedBy(String modid) {
        return owner.equals(modid);
    }
}
