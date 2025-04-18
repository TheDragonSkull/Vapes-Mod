package net.thedragonskull.vapemod.screen;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.thedragonskull.vapemod.block.ModBlocks;
import net.thedragonskull.vapemod.block.entity.VapeExpositorBE;
import net.thedragonskull.vapemod.item.custom.Vape;
import org.jetbrains.annotations.NotNull;

public class VapeExpositorMenu extends AbstractContainerMenu {
    public final VapeExpositorBE blockEntity;
    private final Level level;

    public VapeExpositorMenu(int pContainerId, Inventory inv, FriendlyByteBuf extraData) {
        this(pContainerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public VapeExpositorMenu(int pContainerId, Inventory inv, BlockEntity blockEntity) {
        super(ModMenuTypes.VAPE_EXPOSITOR_MENU.get(), pContainerId);
        this.blockEntity = ((VapeExpositorBE) blockEntity);
        this.level = inv.player.level();

        addExpositorInventory();
        addPlayerHotbar(inv);
        addPlayerInventory(inv);
    }
    
    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        ItemStack returnStack = ItemStack.EMPTY;
        Slot fromSlot = this.slots.get(pIndex);

        if (fromSlot.hasItem()) {
            ItemStack slotStack = fromSlot.getItem();
            returnStack = slotStack.copy();

            if (pIndex < 5) { //In the expositor inventory
                if (!this.moveItemStackTo(slotStack, 5, this.slots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(slotStack, 0, 5, false)) { //In the hotbar/player inv
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                fromSlot.setByPlayer(ItemStack.EMPTY);
            } else {
                fromSlot.setChanged();
            }

        }
        return returnStack;
    }


    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                pPlayer, ModBlocks.VAPE_EXPOSITOR.get());
    }

    private void addExpositorInventory() {
        int startX = 8;
        int startY = 38;
        int spacing = 36;

        for (int i = 0; i < 5; i++) {
            this.addSlot(new Slot(blockEntity, i, startX + i * spacing, startY) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return stack.getItem() instanceof Vape;
                }
            });
        }
    }


    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }
}
