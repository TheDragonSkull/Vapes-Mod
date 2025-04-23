package net.thedragonskull.vapemod.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

/*public class VapeEnergyProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    private final VapeEnergy storage;
    private final LazyOptional<IEnergyStorage> optional;

    public VapeEnergyProvider() {
        this(0);
    }

    public VapeEnergyProvider(int initialEnergy) {
        this.storage = new VapeEnergy(15, initialEnergy); // ← configurable starting energy
        this.optional = LazyOptional.of(() -> storage);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return cap == ForgeCapabilities.ENERGY ? optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Energy", storage.getEnergyStored());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        storage.setEnergy(nbt.getInt("Energy"));
    }
}*/
