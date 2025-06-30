package net.thedragonskull.vapemod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.thedragonskull.vapemod.catalog_offers.VapeCatalogOffers;
import net.thedragonskull.vapemod.catalog_offers.VapeOfferRegistry;
import net.thedragonskull.vapemod.screen.VapeCatalogScreen;
import net.thedragonskull.vapemod.sound.ModSounds;

import java.util.List;

public class C2SBuyVapePacket {
    private final int tradeIndex;
    private final int tabOrdinal;

    public C2SBuyVapePacket(int tradeIndex, int tabOrdinal) {
        this.tradeIndex = tradeIndex;
        this.tabOrdinal = tabOrdinal;
    }

    public static void encode(C2SBuyVapePacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.tradeIndex);
        buf.writeInt(msg.tabOrdinal);
    }

    public static C2SBuyVapePacket decode(FriendlyByteBuf buf) {
        int index = buf.readInt();
        int tab = buf.readInt();
        return new C2SBuyVapePacket(index, tab);
    }

    public static void handle(C2SBuyVapePacket msg, CustomPayloadEvent.Context ctx) {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;

            List<VapeCatalogOffers> trades = switch (VapeCatalogScreen.TabType.values()[msg.tabOrdinal]) {
                case SPECIAL -> VapeOfferRegistry.getSpecialTrades();
                case NORMAL -> VapeOfferRegistry.getNormalTrades();
                case DISPOSABLES -> VapeOfferRegistry.getDisposableTrades();
            };

            if (msg.tradeIndex >= 0 && msg.tradeIndex < trades.size()) {
                VapeCatalogOffers offer = trades.get(msg.tradeIndex);

                if (offer.playerHasEnough(player)) {
                    ItemStack result = offer.getTradeLogic().createResult(player, offer);
                    offer.getTradeLogic().removeCost(player, offer);

                    if (!result.isEmpty()) {
                        if (!player.getInventory().add(result)) {
                            player.drop(result, false);
                        }

                        player.playNotifySound(ModSounds.CATALOG_BUY.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                    }
                }
            }

        ctx.setPacketHandled(true);
    }
}
