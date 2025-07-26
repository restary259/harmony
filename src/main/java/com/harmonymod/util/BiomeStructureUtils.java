package com.harmonymod.util;

import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

/**
 * Utility methods for handling biome and structure resource locations.
 */
public class BiomeStructureUtils {

    /**
     * Quickly build a set of ResourceLocations from a list of string identifiers.
     *
     * @param ids Namespace:path formatted strings.
     * @return Set of ResourceLocations.
     */
    public static Set<ResourceLocation> resourceSet(String... ids) {
        Set<ResourceLocation> set = new HashSet<>();
        for (String id : ids) {
            set.add(new ResourceLocation(id));
        }
        return set;
    }
}
