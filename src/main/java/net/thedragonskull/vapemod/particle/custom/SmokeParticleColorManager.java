package net.thedragonskull.vapemod.particle.custom;

import net.minecraft.world.item.Item;
import net.thedragonskull.vapemod.item.ModItems;

import java.util.HashMap;
import java.util.Map;

public class SmokeParticleColorManager {

    public static final Map<Item, int[]> ITEM_COLORS = new HashMap<>();

    static {
        ITEM_COLORS.put(ModItems.VAPE.get(), new int[]{50, 50, 50});  // Flint
        ITEM_COLORS.put(ModItems.VAPE_YELLOW.get(), new int[]{252, 244, 147}); // Golden Apple
        ITEM_COLORS.put(ModItems.VAPE_RED.get(), new int[]{247, 136, 141}); // Apple
        ITEM_COLORS.put(ModItems.VAPE_STEEL.get(), new int[]{246,246,246}); // Iron Ingot
        ITEM_COLORS.put(ModItems.VAPE_BLUE.get(), new int[]{111, 146, 191}); // Lapis
        ITEM_COLORS.put(ModItems.VAPE_METAL.get(), new int[]{125,125,125}); // Gunpowder
    }

    public static int[] getColorForItem(Item item) {
        return ITEM_COLORS.getOrDefault(item, new int[]{255, 255, 255});
    }

}
