package net.thedragonskull.vapemod.item.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.thedragonskull.vapemod.particle.ModParticles;
import net.thedragonskull.vapemod.sound.ModSounds;

import javax.annotation.Nullable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

public class Vape extends Item {
    private static final String MESSAGE_CANT_SMOKE_UNDERWATER = "message.vapemod.cant_smoke_underwater";
    private final MobEffectInstance vapeEffect;

    @Nullable
    @OnlyIn(Dist.CLIENT)
    protected SimpleSoundInstance breatheSound;

    @Nullable @OnlyIn(Dist.CLIENT)
    protected SimpleSoundInstance resistanceSound;

    public Vape(Properties pProperties, MobEffectInstance vapeEffect) {
        super(pProperties);
        this.vapeEffect = vapeEffect;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {

        if (hand == InteractionHand.MAIN_HAND) {
            player.startUsingItem(hand);
        } else {
            player.stopUsingItem();
            if (level.isClientSide) stopSounds();
            return super.use(level, player, hand);
        }

        if (level.isClientSide && !player.isUnderWater()) {
            if (!Minecraft.getInstance().getSoundManager().isActive(breatheSound) || !Minecraft.getInstance().getSoundManager().isActive(resistanceSound)) {
                breatheSound = SimpleSoundInstance.forUI(ModSounds.SMOKING_BREATHE_SOUND.get(), 1.0F, 0.5F);
                resistanceSound = SimpleSoundInstance.forUI(ModSounds.VAPE_RESISTANCE.get(), 1.0F, 1.0F);
                Minecraft.getInstance().getSoundManager().play(breatheSound);
                Minecraft.getInstance().getSoundManager().play(resistanceSound);
            }
        }

        return super.use(level, player, hand);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack item, int count) {
        if (livingEntity instanceof Player player && !player.isUnderWater()) {
            if (player.getTicksUsingItem() >= getUseDuration(item) - 1) {
                player.stopUsingItem();

                player.addEffect(new MobEffectInstance(vapeEffect));
                player.getCooldowns().addCooldown(this, 100);

                if (!player.getAbilities().instabuild) {
                    item.hurtAndBreak(1, player, player1 -> player.broadcastBreakEvent(player.getUsedItemHand()));
                }

                smokeParticles(player);
            }
        } else if (livingEntity instanceof Player player && player.isUnderWater()) {
            stopSounds();
            player.stopUsingItem();
            player.displayClientMessage(Component.translatable(MESSAGE_CANT_SMOKE_UNDERWATER).withStyle(ChatFormatting.DARK_RED), true);
        }
    }


    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (level.isClientSide) stopSounds();
        super.releaseUsing(stack, level, entity, timeCharged);
    }

    public void smokeParticles(Player player) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Minecraft.getInstance().execute(() -> {
                    for (int i = 0; i < 10; i++) {
                        double distance = -0.5D;
                        double horizontalAngle = Math.toRadians(player.getYRot());
                        double verticalAngle = Math.toRadians(player.getXRot());
                        double xOffset = distance * Math.sin(horizontalAngle) * Math.cos(verticalAngle);
                        double yOffset = distance * Math.sin(verticalAngle);
                        double zOffset = -distance * Math.cos(horizontalAngle) * Math.cos(verticalAngle);
                        double x = player.getX() + xOffset;
                        double y = player.getEyeY() + yOffset;
                        double z = player.getZ() + zOffset;

                        player.level().addParticle(ModParticles.VAPE_SMOKE_PARTICLES.get(), x, y, z, 0.0D, 0.0D, 0.0D);
                    }
                });
            }
        }, 300);
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    private void stopSounds() {
        SoundManager sm = Minecraft.getInstance().getSoundManager();

        if (breatheSound != null) {
            sm.stop(breatheSound);
            breatheSound = null;
        }

        if (resistanceSound != null) {
            sm.stop(resistanceSound);
            resistanceSound = null;
        }
    }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.CUSTOM;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {

            private static final HumanoidModel.ArmPose VAPING = HumanoidModel.ArmPose.create("vaping", false, (model, entity, arm) -> {
                if (!entity.isUnderWater()) {
                    if (arm == HumanoidArm.RIGHT) {
                        model.rightArm.xRot = (float) Math.toRadians(-90);
                        model.rightArm.yRot = (float) Math.toRadians(-30);
                        model.rightArm.zRot = (float) Math.toRadians(20);
                    } else {
                        model.leftArm.xRot = (float) Math.toRadians(-30);
                        model.leftArm.yRot = (float) Math.toRadians(-10);
                        model.leftArm.zRot = (float) Math.toRadians(-15);
                    }
                }
            });

            // Third Person
            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
                if (!itemStack.isEmpty()) {
                    if (entityLiving.getUsedItemHand() == hand && entityLiving.getUseItemRemainingTicks() > 0) {
                        return VAPING;
                    }
                }

                return HumanoidModel.ArmPose.EMPTY;
            }

            // First Person
            @Override
            public boolean applyForgeHandTransform(PoseStack pPoseStack, LocalPlayer player, HumanoidArm arm, ItemStack itemInHand, float partialTick, float equipProcess, float swingProcess) {
                int i = arm == HumanoidArm.RIGHT ? 1 : -1;
                pPoseStack.translate(i * 0.5F, -0.52F, -0.72F);

                if (player.getUseItem() == itemInHand && player.isUsingItem() && !player.isUnderWater()) {
                    float f = (float)player.getUseItemRemainingTicks() - partialTick + 1.0F;
                    float f1 = f / (float)itemInHand.getUseDuration();
                    float f3 = 1.0F - (float)Math.pow((double)f1, 27.0D);

                    pPoseStack.translate(i * -0.4F, 0.1F + equipProcess, 0.0F);

                    pPoseStack.mulPose(Axis.YP.rotationDegrees((float)i * f3 * 45.0F));
                    pPoseStack.mulPose(Axis.XP.rotationDegrees(f3 * 10.0F));
                    pPoseStack.mulPose(Axis.ZP.rotationDegrees((float)i * f3 * 30.0F));
                } else {
                    pPoseStack.translate(i * -0.0F, 0.0F + equipProcess * -0.6F, -0.0F);

                    float f = Mth.sin(swingProcess * swingProcess * (float)Math.PI);
                    pPoseStack.mulPose(Axis.YP.rotationDegrees((float)i * (45.0F + f * -20.0F)));
                    float f1 = Mth.sin(Mth.sqrt(swingProcess) * (float)Math.PI);
                    pPoseStack.mulPose(Axis.ZP.rotationDegrees((float)i * f1 * -20.0F));
                    pPoseStack.mulPose(Axis.XP.rotationDegrees(f1 * -80.0F));
                    pPoseStack.mulPose(Axis.YP.rotationDegrees((float)i * -5.0F));
                }
                return true;
            }
        });
    }


}
