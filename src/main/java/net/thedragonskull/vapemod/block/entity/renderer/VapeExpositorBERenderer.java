package net.thedragonskull.vapemod.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.thedragonskull.vapemod.block.entity.VapeExpositorBE;
import net.thedragonskull.vapemod.util.VapeExpositorUtil;

public class VapeExpositorBERenderer implements BlockEntityRenderer<VapeExpositorBE> {

    public VapeExpositorBERenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(VapeExpositorBE be, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {

        be.unpackLootTable(null);


        Direction facing = be.getBlockState().getValue(HorizontalDirectionalBlock.FACING);

        for (int i = 0; i < 5; i++) {
            ItemStack stack = VapeExpositorUtil.getVapeInSlot(be, i);
            if (!stack.isEmpty()) {
                renderVapes(stack, pPoseStack, pBuffer, pPackedLight, pPackedOverlay, i, facing);
            }
        }
    }

    private void renderVapes(ItemStack stack, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay, int index, Direction facing) {
        poseStack.pushPose();

        float spacing = 0.2f;
        int renderIndex = 4 - index;
        float offsetX = (renderIndex - 2) * spacing;

        poseStack.translate(0.5, 0.1, 0.5);

        float yRot = switch (facing) {
            case NORTH -> 0f;
            case SOUTH -> 180f;
            case WEST -> 90f;
            case EAST -> -90f;
            default -> 0f;
        };

        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(20.5F));
        poseStack.translate(offsetX, 0, -0.175);

        Minecraft.getInstance().getItemRenderer().renderStatic(
                stack,
                ItemDisplayContext.HEAD,
                light,
                overlay,
                poseStack,
                buffer,
                null,
                0
        );

        poseStack.popPose();
    }
}
