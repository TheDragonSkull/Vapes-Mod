package net.thedragonskull.vapemod.util;

import net.minecraft.world.item.ItemStack;
import net.thedragonskull.vapemod.block.entity.VapeExpositorBE;

public class VapeExpositorUtil {

    public static ItemStack getVapeInSlot(VapeExpositorBE be, int slot) {
        if (be == null || slot < 0 || slot >= be.getContainerSize()) {
            return ItemStack.EMPTY;
        }
        return be.getItem(slot);
    }

    public static boolean isSlotOccupied(VapeExpositorBE be, int slot) {
        return !getVapeInSlot(be, slot).isEmpty();
    }

}
