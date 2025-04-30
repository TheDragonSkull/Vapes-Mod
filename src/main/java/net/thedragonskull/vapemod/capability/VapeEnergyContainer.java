package net.thedragonskull.vapemod.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;

public interface VapeEnergyContainer {
    @Nullable
    ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt);

    int receiveEnergy(ItemStack container, int maxReceive, boolean simulate);

    int extractEnergy(ItemStack container, int maxExtract, boolean simulate);

    int getEnergy(ItemStack container);

    int getCapacity(ItemStack container);
}
