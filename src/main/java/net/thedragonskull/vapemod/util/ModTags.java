package net.thedragonskull.vapemod.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.thedragonskull.vapemod.VapeMod;

public class ModTags {

    public static class Items {
        public static final TagKey<Item> VAPES = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(VapeMod.MOD_ID, "vapes"));
    }

}
