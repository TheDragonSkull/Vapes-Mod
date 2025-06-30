package net.thedragonskull.vapemod.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.thedragonskull.vapemod.screen.VapeExpositorMenu;

import javax.annotation.Nullable;

public class VapeExpositorBE extends RandomizableContainerBlockEntity implements MenuProvider {
    private static final int SIZE = 5;
    private NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);

    public VapeExpositorBE(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VAPE_EXPOSITOR_BE.get(), pos, state);
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items.clear();
        this.items.addAll(items);
    }

    @Override
    protected Component getDefaultName() {
        return Component.literal("Vape Expositor");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory) {
        return new VapeExpositorMenu(id, playerInventory, this);
    }

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    public void drops() {
        Containers.dropContents(this.level, this.worldPosition, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider pRegistries) {
        super.saveAdditional(tag, pRegistries);
        if (!trySaveLootTable(tag)) {
            ContainerHelper.saveAllItems(tag, this.items, pRegistries);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.loadAdditional(pTag, pRegistries);
        this.items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        if (!tryLoadLootTable(pTag)) {
            ContainerHelper.loadAllItems(pTag, this.items, pRegistries);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (this.lootTable != null && this.level != null && !this.level.isClientSide) {
            this.unpackLootTable(null);
            this.setChanged();
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public void tick() {
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        return saveWithoutMetadata(pRegistries);
    }

}
