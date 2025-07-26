package com.harmonymod.util;

import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

public class BiomeStructureUtils {
    public static Set<ResourceLocation> resourceSet(String... ids) {
        Set<ResourceLocation> set = new HashSet<>();
        for (String id : ids) set.add(new ResourceLocation(id));
        return set;
    }

    public static boolean isVanilla(String id) {
        return id != null && id.startsWith("minecraft:");
    }
}
