package net.thedragonskull.vapemod.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;
import net.thedragonskull.vapemod.block.custom.VapeCatalog;

import java.util.function.Supplier;

public class C2SCloseCatalogScreenPacket {
    private final BlockPos blockPos;

    public C2SCloseCatalogScreenPacket(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    public C2SCloseCatalogScreenPacket(FriendlyByteBuf buf) {
        this.blockPos = buf.readBlockPos();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.blockPos);
    }

    public static void handle(C2SCloseCatalogScreenPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();

            if (player == null)
                return;

            BlockState state = player.level().getBlockState(msg.blockPos);
            if (state.getBlock() instanceof VapeCatalog) {
                player.level().setBlock(msg.blockPos, state.setValue(VapeCatalog.OPEN, false), 3);
            }

        });

        ctx.get().setPacketHandled(true);
    }

}
