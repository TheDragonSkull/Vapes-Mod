package net.thedragonskull.vapemod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.thedragonskull.vapemod.config.VapeCommonConfigs;
import net.thedragonskull.vapemod.sound.ModSounds;
import net.thedragonskull.vapemod.util.ModTags;
import net.thedragonskull.vapemod.util.VapeCatalogUtil;

import java.util.function.Supplier;

public class C2SBuyVapePacket {
    private final ItemStack stack;
    private final ItemStack costA;
    private final ItemStack costB;

    public C2SBuyVapePacket(ItemStack stack, ItemStack costA, ItemStack costB) {
        this.stack = stack;
        this.costA = costA;
        this.costB = costB;
    }

    public static void encode(C2SBuyVapePacket msg, FriendlyByteBuf buf) {
        buf.writeItem(msg.stack);
        buf.writeItem(msg.costA);
        buf.writeItem(msg.costB);
    }

    public static C2SBuyVapePacket decode(FriendlyByteBuf buf) {
        ItemStack stack = buf.readItem();
        ItemStack costA = buf.readItem();
        ItemStack costB = buf.readItem();
        return new C2SBuyVapePacket(stack, costA, costB);
    }

    public static void handle(C2SBuyVapePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            ItemStack vape = msg.stack.copy();
            ItemStack costA = msg.costA.copy();
            ItemStack costB = msg.costB.copy();

            if (VapeCatalogUtil.hasEnoughCurrency(player, costA, costB)) {
                removeCurrency(player, costA, costB);

                if (!player.getInventory().add(vape)) {
                    player.drop(vape, false);
                }

                player.playNotifySound(ModSounds.CATALOG_BUY.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static void removeCurrency(Player player, ItemStack costA, ItemStack costB) {
        takeFromInventory(player, costA);
        takeFromInventory(player, costB);
    }

    private static void takeFromInventory(Player player, ItemStack required) {
        if (required == null || required.isEmpty()) return;

        int remaining = required.getCount();

        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack stack = player.getInventory().items.get(i);

            if (ItemStack.isSameItemSameTags(stack, required)) {
                int removed = Math.min(stack.getCount(), remaining);
                stack.shrink(removed);
                remaining -= removed;
                if (remaining <= 0) {
                    break;
                }
            }
        }
    }

    private static int getPriceForVape(ItemStack vape) {
        if (vape.is(ModTags.Items.DISPOSABLE_VAPES)) return VapeCommonConfigs.PRICE_DISPOSABLE.get();
        return VapeCommonConfigs.PRICE_NORMAL.get();
    }
}

