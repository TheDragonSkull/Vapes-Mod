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

    public C2SBuyVapePacket(ItemStack stack) {
        this.stack = stack;
    }

    public static void encode(C2SBuyVapePacket msg, FriendlyByteBuf buf) {
        buf.writeItem(msg.stack);
    }

    public static C2SBuyVapePacket decode(FriendlyByteBuf buf) {
        return new C2SBuyVapePacket(buf.readItem());
    }

    public static void handle(C2SBuyVapePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            ItemStack vape = msg.stack.copy();
            int price = getPriceForVape(vape);
            Item costItem = VapeCommonConfigs.getCatalogCostItem();

            if (VapeCatalogUtil.hasEnoughCurrency(player, new ItemStack(costItem, price))) {
                removeCurrency(player, costItem, price);
                if (!player.getInventory().add(vape)) {
                    player.drop(vape, false);
                }
                player.playNotifySound(ModSounds.CATALOG_BUY.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static void removeCurrency(Player player, Item item, int amount) {
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack stack = player.getInventory().items.get(i);
            if (stack.getItem() == item) {
                int toRemove = Math.min(stack.getCount(), amount);
                stack.shrink(toRemove);
                amount -= toRemove;
                if (amount <= 0) break;
            }
        }
    }

    private static int getPriceForVape(ItemStack vape) {
        if (vape.is(ModTags.Items.DISPOSABLE_VAPES)) return VapeCommonConfigs.PRICE_DISPOSABLE.get();
        return VapeCommonConfigs.PRICE_NORMAL.get();
    }
}

