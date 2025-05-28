package net.thedragonskull.vapemod.network;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.network.NetworkEvent;
import net.thedragonskull.vapemod.capability.VapeEnergy;
import net.thedragonskull.vapemod.sound.ModSounds;
import net.thedragonskull.vapemod.util.VapeCatalogUtil;

import java.util.List;
import java.util.function.Supplier;

import static net.thedragonskull.vapemod.util.VapeCatalogUtil.removeCurrency;

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

                vape.getCapability(ForgeCapabilities.ENERGY).ifPresent(cap -> {
                    if (cap instanceof VapeEnergy e) {
                        VapeEnergy.setInt(e.stack, "Energy", e.getMaxEnergyStored());
                    }
                });

                Potion randomPotion = getRandomPotion(player);
                PotionUtils.setPotion(vape, randomPotion);

                removeCurrency(player, costA, costB);

                if (!player.getInventory().add(vape)) {
                    player.drop(vape, false);
                }

                player.playNotifySound(ModSounds.CATALOG_BUY.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static Potion getRandomPotion(ServerPlayer player) {
        List<Potion> potions = BuiltInRegistries.POTION.stream()
                .filter(p -> !p.getEffects().isEmpty() && p != Potions.EMPTY)
                .toList();

        if (potions.isEmpty()) return Potions.HEALING; // fallback

        return potions.get(player.level().random.nextInt(potions.size()));
    }


}

