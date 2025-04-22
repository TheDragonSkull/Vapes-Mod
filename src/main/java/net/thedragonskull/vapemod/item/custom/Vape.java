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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.thedragonskull.vapemod.capability.VapeEnergyProvider;
import net.thedragonskull.vapemod.particle.ModParticles;
import net.thedragonskull.vapemod.sound.ModSounds;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

public class Vape extends Item {
    private static final String MESSAGE_CANT_SMOKE_UNDERWATER = "message.vapemod.cant_smoke_underwater";

    @Nullable
    @OnlyIn(Dist.CLIENT)
    protected SimpleSoundInstance breatheSound;

    @Nullable @OnlyIn(Dist.CLIENT)
    protected SimpleSoundInstance resistanceSound;

    public Vape(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new VapeEnergyProvider(0);
    }

    @Override
    public ItemStack getDefaultInstance() {
        return PotionUtils.setPotion(super.getDefaultInstance(), Potions.WATER);
    }

    /*    @Override
    public boolean isBarVisible(ItemStack pStack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return stack.getCapability(ForgeCapabilities.ENERGY)
                .map(e -> Math.round(13.0F * e.getEnergyStored() / e.getMaxEnergyStored()))
                .orElse(0);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return stack.getCapability(ForgeCapabilities.ENERGY).map(storage -> {
            float percent = (float) storage.getEnergyStored() / storage.getMaxEnergyStored();
            int red = (int)((1.0f - percent) * 255);
            int green = (int)(percent * 255);
            return (red << 16) | (green << 8); // RGB
        }).orElse(0xFF0000);
    }*/

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        stack.getCapability(ForgeCapabilities.ENERGY).ifPresent(storage -> {
            int energy = storage.getEnergyStored();
            int max = storage.getMaxEnergyStored();
            int percent = Math.round(((float) energy / max) * 100);

            float ratio = (float) energy / max;
            int red = (int) ((1.0f - ratio) * 255);
            int green = (int) (ratio * 255);
            int color = (red << 16) | (green << 8); // RGB

            MutableComponent label = Component.literal("Capacity: ").withStyle(ChatFormatting.DARK_AQUA);
            MutableComponent value = Component.literal(percent + "%").withStyle(style -> style.withColor(color));

            tooltip.add(label.append(value));

            List<MobEffectInstance> effects = PotionUtils.getMobEffects(stack);
            if (!effects.isEmpty()) {

                for (MobEffectInstance effect : effects) {
                    MutableComponent effectName = Component.translatable(effect.getDescriptionId());

                    if (effect.getAmplifier() > 0) {
                        effectName.append(" ").append(Component.translatable("potion.potency." + effect.getAmplifier()));
                    }

                    int adjustedDuration = (int)(effect.getDuration() / storage.getMaxEnergyStored());
                    if (adjustedDuration > 20) {
                        String time = formatDuration(adjustedDuration);
                        effectName.append(" (").append(Component.literal(time)).append(")");
                    }

                    tooltip.add(effectName.withStyle(ChatFormatting.BLUE));
                }
            } else {
                tooltip.add(Component.literal("No Effects").withStyle(ChatFormatting.GRAY));
            }
        });
    }

    private String formatDuration(int ticks) {
        int seconds = ticks / 20;
        int minutes = seconds / 60;
        seconds = seconds % 60;

        return minutes + ":" + String.format("%02d", seconds);
    }

    public String getDescriptionId(ItemStack pStack) {
        return PotionUtils.getPotion(pStack).getName(this.getDescriptionId() + ".effect.");
    }

    @Override
    public Component getName(ItemStack stack) {
        Component baseName = Component.translatable(this.getDescriptionId());

        List<MobEffectInstance> effects = PotionUtils.getMobEffects(stack);
        if (!effects.isEmpty()) {
            MobEffectInstance effect = effects.get(0);
            Component effectName = Component.translatable(effect.getDescriptionId());
            return Component.literal("").append(baseName).append(" (").append(effectName).append(")");
        }

        return baseName;
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);

        if (!level.isClientSide) {
            level.playSound(null, player.blockPosition(), SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 1.0f, 1.0f);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack item = player.getItemInHand(hand);

        if (hand == InteractionHand.MAIN_HAND) {
            player.startUsingItem(hand);
        } else {
            player.stopUsingItem();
            if (level.isClientSide) stopSounds();
            return InteractionResultHolder.fail(item);
        }

        if (level.isClientSide && !player.isUnderWater()) {
            if (!Minecraft.getInstance().getSoundManager().isActive(breatheSound) || !Minecraft.getInstance().getSoundManager().isActive(resistanceSound)) {
                breatheSound = SimpleSoundInstance.forUI(ModSounds.SMOKING_BREATHE_SOUND.get(), 1.0F, 0.5F);
                resistanceSound = SimpleSoundInstance.forUI(ModSounds.VAPE_RESISTANCE.get(), 1.0F, 1.0F);
                Minecraft.getInstance().getSoundManager().play(resistanceSound);
            }
        }

        if (!item.getCapability(ForgeCapabilities.ENERGY).map(energy -> energy.getEnergyStored() > 0).orElse(false)) {
            if (level.isClientSide) {
                //stopSounds();
                player.displayClientMessage(
                        Component.literal("¡Empty tank, refill!").withStyle(ChatFormatting.DARK_RED),
                        true
                );
            }
            return InteractionResultHolder.fail(item);
        }

        return super.use(level, player, hand);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack item, int count) {

        if (!(livingEntity instanceof Player player)) return;

        if (player.isUnderWater()) {
            stopSounds();
            player.stopUsingItem();
            player.displayClientMessage(Component.translatable(MESSAGE_CANT_SMOKE_UNDERWATER).withStyle(ChatFormatting.DARK_RED), true);
            return;
        }

        if (player.getTicksUsingItem() >= getUseDuration(item) - 1) {
            item.getCapability(ForgeCapabilities.ENERGY).ifPresent(storage -> {
                int energy = storage.getEnergyStored();

                if (energy > 0) {

                    for (MobEffectInstance effect : PotionUtils.getMobEffects(item)) {
                        if (effect.getEffect().isInstantenous()) {
                            effect.getEffect().applyInstantenousEffect(player, player, player, effect.getAmplifier(), 1.0);
                        } else {
                            int duration = (int)(effect.getDuration() / storage.getMaxEnergyStored());
                            int amplifier = effect.getAmplifier();
                            player.addEffect(new MobEffectInstance(effect.getEffect(), duration, amplifier, false, true));
                        }
                    }


                    player.getCooldowns().addCooldown(this, 100);

                    if (!player.getAbilities().instabuild) {
                        storage.extractEnergy(1, false);
                    }

                    if (level.isClientSide) {
                        smokeParticles(player);
                    }
                }
            });
        }

    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (level.isClientSide) stopSounds();
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

    @Override
    public boolean isEnchantable(ItemStack pStack) {
        return false;
    }
}
