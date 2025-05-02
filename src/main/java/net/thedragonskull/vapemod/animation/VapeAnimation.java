package net.thedragonskull.vapemod.animation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

public class VapeAnimation implements IClientItemExtensions {
    public static final UseAnim VAPING_ANIMATION = UseAnim.CUSTOM;

    @OnlyIn(Dist.CLIENT)
    public static final HumanoidModel.ArmPose VAPING_POSE = HumanoidModel.ArmPose.valueOf("vapemod_arm_pose");

    // Third Person
    @Override
    public HumanoidModel.ArmPose getArmPose(@NotNull LivingEntity entity, @NotNull InteractionHand hand, ItemStack itemStack) {
        if (entity.isUsingItem() && entity.getUsedItemHand() == hand && entity.getUseItemRemainingTicks() > 0 && itemStack.getUseAnimation() == VAPING_ANIMATION) {
            return VAPING_POSE;
        }

        return null;
    }

    // First Person
    @Override
    public boolean applyForgeHandTransform(PoseStack pPoseStack, LocalPlayer player, @NotNull HumanoidArm arm, @NotNull ItemStack itemInHand, float partialTick, float equipProcess, float swingProcess) {
        HumanoidArm usingArm = player.getUsedItemHand() == InteractionHand.MAIN_HAND
                ? player.getMainArm()
                : player.getMainArm().getOpposite();

        int i = usingArm == HumanoidArm.RIGHT ? 1 : -1;

        if (player.isUsingItem() && player.getUseItemRemainingTicks() > 0 && usingArm == arm && itemInHand.getUseAnimation() == VAPING_ANIMATION && !player.isUnderWater()) {
            float f = (float)player.getUseItemRemainingTicks() - partialTick + 1.0F;
            float f1 = f / (float)itemInHand.getUseDuration(player);
            float f3 = 1.0F - (float)Math.pow(f1, 27.0D);

            pPoseStack.translate(i * 0.5F, -0.52F, -0.72F);
            pPoseStack.translate(i * -0.4F, 0.1F + equipProcess, 0.0F);

            pPoseStack.mulPose(Axis.YP.rotationDegrees(i * f3 * 45.0F));
            pPoseStack.mulPose(Axis.XP.rotationDegrees(f3 * 10.0F));
            pPoseStack.mulPose(Axis.ZP.rotationDegrees(i * f3 * 30.0F));
        } else {
            pPoseStack.translate(i * -0.0F, 0.0F + equipProcess * -0.6F, -0.0F);
        }

        return false;
    }

}
