package com.harmonymod.util;

import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

/**
 * Utility methods for working with biomes and structures in Harmony API.
 */
public class BiomeStructureUtils {

    /**
     * Convenience method to create a set of ResourceLocations from string IDs.
     * Example: resourceSet("minecraft:plains", "mymod:cool_biome")
     */
    public static Set<ResourceLocation> resourceSet(String... ids) {
        Set<ResourceLocation> set = new HashSet<>();
        for (String id : ids) {
            set.add(new ResourceLocation(id));
        }
        return set;
    }
}
