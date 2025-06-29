package net.thedragonskull.vapemod.item.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.network.PacketDistributor;
import net.thedragonskull.vapemod.capability.VapeEnergy;
import net.thedragonskull.vapemod.capability.VapeEnergyContainer;
import net.thedragonskull.vapemod.component.ModDataComponentTypes;
import net.thedragonskull.vapemod.network.S2CResistanceSoundPacket;
import net.thedragonskull.vapemod.network.PacketHandler;
import net.thedragonskull.vapemod.network.S2CStopResistanceSoundPacket;
import net.thedragonskull.vapemod.network.S2CVapeParticlesPacket;
import net.thedragonskull.vapemod.particle.ModParticles;
import net.thedragonskull.vapemod.sound.ClientSoundHandler;
import net.thedragonskull.vapemod.sound.ModSounds;
import net.thedragonskull.vapemod.util.VapeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

import static org.joml.Math.clamp;

public class Vape extends Item implements VapeEnergyContainer, IVape {
    private static final String MESSAGE_CANT_SMOKE_UNDERWATER = "message.vapemod.cant_smoke_underwater";

    public Vape(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = new ItemStack(this);
        stack.set(DataComponents.POTION_CONTENTS, PotionContents.EMPTY.withPotion(Potions.WATER));

        return stack;
    }

    @Nullable
    @Override
    public ICapabilityProvider getCapabilityProvider(ItemStack stack) {
        VapeEnergyContainer container = this;

        return new ICapabilityProvider() {
            @Override
            public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
                if (cap == ForgeCapabilities.ENERGY) {
                    return LazyOptional.of(() -> new VapeEnergy(stack, container)).cast();
                }
                return LazyOptional.empty();
            }
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        stack.getCapability(ForgeCapabilities.ENERGY).ifPresent(storage -> {
            int energy = storage.getEnergyStored();
            int max = storage.getMaxEnergyStored();
            int percent = Math.round(((float) energy / max) * 100);

            float ratio = (float) energy / max;
            int red = (int) ((1.0f - ratio) * 255);
            int green = (int) (ratio * 255);
            int color = (red << 16) | (green << 8);

            MutableComponent label = Component.literal("Capacity: ").withStyle(ChatFormatting.DARK_AQUA);
            MutableComponent value = Component.literal(percent + "%").withStyle(style -> style.withColor(color));

            tooltip.add(label.append(value));

            PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
            if (contents != null && !stack.get(DataComponents.POTION_CONTENTS).potion().get().value().getEffects().isEmpty()) {
                for (MobEffectInstance effect : stack.get(DataComponents.POTION_CONTENTS).potion().get().value().getEffects()) {
                    MutableComponent effectName = Component.translatable(effect.getDescriptionId());

                    if (effect.getAmplifier() > 0) {
                        effectName.append(" ").append(Component.translatable("potion.potency." + effect.getAmplifier()));
                    }

                    int adjustedDuration = (int)(effect.getDuration() / (float) max);
                    if (adjustedDuration > 20) {
                        String time = VapeUtil.formatDuration(adjustedDuration);
                        effectName.append(" (").append(Component.literal(time)).append(")");
                    }

                    tooltip.add(effectName.withStyle(ChatFormatting.BLUE));
                }
            } else {
                tooltip.add(Component.literal("No Effects").withStyle(ChatFormatting.GRAY));
            }
        });
    }

    public String getDescriptionId(ItemStack pStack) {
        return Potion.getName(pStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion(), this.getDescriptionId() + ".effect.");
    }

    @Override
    public Component getName(ItemStack stack) {
        Component baseName = Component.translatable(this.getDescriptionId());

        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents != null) {
            List<MobEffectInstance> effects = stack.get(DataComponents.POTION_CONTENTS).potion().get().value().getEffects();
            if (!effects.isEmpty()) {
                MobEffectInstance effect = effects.get(0);
                Component effectName = Component.translatable(effect.getDescriptionId());
                return Component.literal("").append(baseName).append(" (").append(effectName).append(")");
            }
        }

        return baseName;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack item = player.getItemInHand(hand);

        for (InteractionHand h : InteractionHand.values()) {
            ItemStack held = player.getItemInHand(h);
            if (held.getItem() instanceof Vape vape) {
                if (player.getCooldowns().isOnCooldown(held.getItem())) {
                    return InteractionResultHolder.fail(item);
                }
            }
        }

        boolean hasEnergy = item.getCapability(ForgeCapabilities.ENERGY)
                .map(energy -> energy.getEnergyStored() > 0)
                .orElse(false);

        if (!hasEnergy) {
            if (level.isClientSide) {
                player.displayClientMessage(
                        Component.literal("¡Empty tank, refill!").withStyle(ChatFormatting.DARK_RED),
                        true
                );
            }
            return InteractionResultHolder.fail(item);
        }

        if (hasEnergy) {
            player.startUsingItem(hand);
        } else {
            player.stopUsingItem();
            return InteractionResultHolder.fail(item);
        }

        if (!level.isClientSide) {
            PacketHandler.INSTANCE.send(
                    new S2CResistanceSoundPacket(player.getUUID()),
                    PacketDistributor.TRACKING_ENTITY.with(player)
            );
        } else {
            ClientSoundHandler.start(player);
        }

        return super.use(level, player, hand);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack item, int count) {

        if (!(livingEntity instanceof Player player)) return;

        if (player.isUnderWater()) {
            player.stopUsingItem();
            player.displayClientMessage(Component.translatable(MESSAGE_CANT_SMOKE_UNDERWATER).withStyle(ChatFormatting.DARK_RED), true);
            return;
        }

        if (player.getTicksUsingItem() >= getUseDuration(item, livingEntity) - 1) {
            item.getCapability(ForgeCapabilities.ENERGY).ifPresent(storage -> {
                int energy = storage.getEnergyStored();

                if (energy > 0) {
                    for (MobEffectInstance effect : item.get(DataComponents.POTION_CONTENTS).potion().get().value().getEffects()) {
                        if (effect.getEffect().value().isInstantenous()) {
                            effect.getEffect().value().applyInstantenousEffect(player, player, player, effect.getAmplifier(), 1.0);
                        } else {
                            int duration = (int)(effect.getDuration() / storage.getMaxEnergyStored());
                            int amplifier = effect.getAmplifier();
                            player.addEffect(new MobEffectInstance(effect.getEffect(), duration, amplifier, false, true));
                        }
                    }

                    VapeUtil.applyCooldownToVapes(player, 100);

                    int color = 0xFFFFFF;
                    PotionContents contents = item.get(DataComponents.POTION_CONTENTS);
                    if (contents != null && contents.potion().isPresent() && contents.potion().get() != Potions.WATER) {
                        color = contents.getColor();
                    }

                    if (player instanceof ServerPlayer serverPlayer) {
                        PacketHandler.INSTANCE.send(
                                new S2CVapeParticlesPacket(player.getUUID(), color),
                                PacketDistributor.TRACKING_ENTITY.with(player)
                        );
                    } else {
                        VapeUtil.smokeParticles(player);
                    }

                    if (!player.getAbilities().instabuild) {
                        storage.extractEnergy(1, false);
                    }

                    if (!level.isClientSide) {
                        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                                ModSounds.VAPE_RESISTANCE_END.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

                        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                                ModSounds.SMOKING_BREATHE_OUT.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                    }
                }
            });
        }

    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) return;

        if (!level.isClientSide) {
            PacketHandler.INSTANCE.send(
                    new S2CStopResistanceSoundPacket(player.getUUID()),
                    PacketDistributor.TRACKING_ENTITY.with(player)
            );
        } else {
            ClientSoundHandler.stop(player);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof Player player) {
            if (!level.isClientSide) {
                PacketHandler.INSTANCE.send(
                        new S2CStopResistanceSoundPacket(player.getUUID()),
                        PacketDistributor.TRACKING_ENTITY.with(player)
                );
            } else {
                ClientSoundHandler.stop(player);
            }
        }
        return super.finishUsingItem(stack, level, entity);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.CUSTOM;
    }

    @Override
    public int getUseDuration(ItemStack pStack, LivingEntity pEntity) {
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
                        model.leftArm.xRot = (float) Math.toRadians(-90);
                        model.leftArm.yRot = (float) Math.toRadians(30);
                        model.leftArm.zRot = (float) Math.toRadians(-20);
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
                    float f1 = f / (float)itemInHand.getUseDuration(player);
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

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);

        if (!level.isClientSide) {
            level.playSound(null, player.blockPosition(), SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 1.0f, 1.0f);
        }
    }

    private void setEnergyStored(ItemStack container, int value) {
        container.set(ModDataComponentTypes.ENERGY.get(), clamp(value, 0, getCapacity(container)));
    }

    @Override
    public int receiveEnergy(ItemStack container, int maxReceive, boolean simulate) {
        return 0;
    }

    @Override
    public int extractEnergy(ItemStack container, int maxExtract, boolean simulate) {
        if (1 == 0) return 0;
        int energyStored = getEnergy(container);
        int energyExtracted = Math.min(energyStored, Math.min(1, maxExtract));
        if (!simulate) setEnergyStored(container, energyStored - energyExtracted);
        return energyExtracted;
    }

    @Override
    public int getEnergy(ItemStack container) {
        Integer energy = container.get(ModDataComponentTypes.ENERGY.get());

        if (energy == null) {
            energy = 0;
            container.set(ModDataComponentTypes.ENERGY.get(), energy);
        }

        return energy;
    }

    @Override
    public int getCapacity(ItemStack container) {
        return 15;
    }
}
