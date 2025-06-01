package net.thedragonskull.vapemod.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.thedragonskull.vapemod.component.ModDataComponentTypes;
import net.thedragonskull.vapemod.config.VapeCommonConfigs;
import net.thedragonskull.vapemod.network.S2CResistanceSoundPacket;
import net.thedragonskull.vapemod.network.S2CStopResistanceSoundPacket;
import net.thedragonskull.vapemod.network.S2CVapeParticlesPacket;
import net.thedragonskull.vapemod.sound.ClientSoundHandler;
import net.thedragonskull.vapemod.sound.ModSounds;
import net.thedragonskull.vapemod.util.VapeUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static net.thedragonskull.vapemod.util.VapeUtil.formatDuration;

public class DisposableVape extends Item implements IVape {
    private static final String MESSAGE_CANT_SMOKE_UNDERWATER = "message.vapemod.cant_smoke_underwater";
    private static final String MESSAGE_DEPLETED = "message.vapemod.depleted";
    private final DyeColor dyeColor;

    public DisposableVape(DyeColor dyeColor, Properties properties) {
        super(properties);
        this.dyeColor = dyeColor;
    }

    public DyeColor getColor() {
        return dyeColor;
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = new ItemStack(this);
        stack.set(DataComponents.POTION_CONTENTS, PotionContents.EMPTY.withPotion(Potions.WATER));

        return stack;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return VapeCommonConfigs.CONFIG.DISPOSABLE_VAPE_DURABILITY.get();
    }

    @Override
    public boolean isBarVisible(ItemStack pStack) {
        return false;
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        if (pLevel.isClientSide || !(pEntity instanceof Player)) return;

        Boolean alreadyRandomized = pStack.get(ModDataComponentTypes.RANDOMIZED_POTION.get());
        if (Boolean.TRUE.equals(alreadyRandomized)) return;

        var potionContents = pStack.get(DataComponents.POTION_CONTENTS);
        boolean isEmpty = potionContents == null ||
                potionContents.potion().map(holder -> holder.value().getEffects().isEmpty()).orElse(true);

        if (isEmpty) {
            List<Potion> potions = BuiltInRegistries.POTION.stream()
                    .filter(p -> !p.getEffects().isEmpty())
                    .toList();

            if (!potions.isEmpty()) {
                Potion randomPotion = potions.get(pLevel.getRandom().nextInt(potions.size()));
                Holder<Potion> randomHolder = BuiltInRegistries.POTION.wrapAsHolder(randomPotion);
                PotionContents contents = new PotionContents(randomHolder);

                pStack.set(DataComponents.POTION_CONTENTS, contents);
                pStack.set(ModDataComponentTypes.RANDOMIZED_POTION.get(), true);
            }
        }

    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack item = player.getItemInHand(hand);

        for (InteractionHand h : InteractionHand.values()) {
            ItemStack held = player.getItemInHand(h);

            if (held.getItem() instanceof DisposableVape) {
                if (player.getCooldowns().isOnCooldown(held.getItem())) {

                    return InteractionResultHolder.fail(item);
                } else if (held.getDamageValue() >= VapeCommonConfigs.CONFIG.DISPOSABLE_VAPE_DURABILITY.get()) {

                    player.displayClientMessage(Component.translatable(MESSAGE_DEPLETED).withStyle(ChatFormatting.DARK_RED), true);
                    return InteractionResultHolder.fail(item);
                }
            }
        }

        player.startUsingItem(hand);

        if (!level.isClientSide) {
            PacketDistributor.sendToPlayersTrackingEntity(player, new S2CResistanceSoundPacket(player.getUUID()));
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
            int maxDurability = item.getMaxDamage();
            PotionContents contents = item.get(DataComponents.POTION_CONTENTS);

            if (contents != null) {
                List<MobEffectInstance> effects = new ArrayList<>();

                contents.potion()
                        .map(holder -> holder.value().getEffects())
                        .ifPresent(effects::addAll);

                for (MobEffectInstance effect : effects) {
                    if (effect.getEffect().value().isInstantenous()) {
                        effect.getEffect().value().applyInstantenousEffect(player, player, player, effect.getAmplifier(), 1.0);
                    } else {
                        int duration = effect.getDuration() / maxDurability;
                        int amplifier = effect.getAmplifier();
                        player.addEffect(new MobEffectInstance(effect.getEffect(), duration * 2, amplifier, false, true));
                    }
                }
            }

            VapeUtil.applyCooldownToVapes(player, 100);

            int color = 0xFFFFFF;
            if (contents != null && contents.potion().isPresent() && contents.potion().get() != Potions.WATER) {
                color = contents.getColor();
            }

            if (!level.isClientSide) {
                if (player instanceof ServerPlayer) {
                    PacketDistributor.sendToPlayersTrackingEntity(player, new S2CVapeParticlesPacket(player.getUUID(), color));
                }

                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        ModSounds.VAPE_RESISTANCE_END.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        ModSounds.SMOKING_BREATHE_OUT.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            } else {
                if (Minecraft.getInstance().player != null &&
                        Minecraft.getInstance().player.getUUID().equals(player.getUUID())) {
                    VapeUtil.smokeParticles(player);
                }
            }

        }

    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) return;

        if (!level.isClientSide) {
            PacketDistributor.sendToPlayersTrackingEntity(player, new S2CStopResistanceSoundPacket(player.getUUID()));

        } else {
            ClientSoundHandler.stop(player);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof Player player) {
            if (!level.isClientSide) {
                PacketDistributor.sendToPlayersTrackingEntity(player, new S2CStopResistanceSoundPacket(player.getUUID()));
            } else {
                ClientSoundHandler.stop(player);

            }

            int current = stack.getDamageValue();
            int max = stack.getMaxDamage();

            if (current < max) {
                if (!player.isCreative())
                    stack.setDamageValue(current + 1);
            }
        }

        return super.finishUsingItem(stack, level, entity);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);


        int currentDurability = stack.getMaxDamage() - stack.getDamageValue();
        int maxDurability = stack.getMaxDamage();
        int percent = Math.round(((float) currentDurability / maxDurability) * 100);

        float ratio = (float) currentDurability / maxDurability;
        int red = (int) ((1.0f - ratio) * 255);
        int green = (int) (ratio * 255);
        int color = (red << 16) | (green << 8); // RGB

        MutableComponent label = Component.literal("Capacity: ").withStyle(ChatFormatting.DARK_AQUA);
        MutableComponent value = Component.literal(percent + "%").withStyle(style -> style.withColor(color));

        tooltip.add(label.append(value));

        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        List<MobEffectInstance> effects = new ArrayList<>();

        if (contents != null) {
            contents.potion()
                    .map(holder -> holder.value().getEffects())
                    .ifPresent(effects::addAll);
        }

        if (!effects.isEmpty()) {

            for (MobEffectInstance effect : effects) {
                MutableComponent effectName = Component.translatable(effect.getDescriptionId());

                if (effect.getAmplifier() > 0) {
                    effectName.append(" ").append(Component.translatable("potion.potency." + effect.getAmplifier()));
                }

                int adjustedDuration = (int)(effect.getDuration() / stack.getMaxDamage());
                if (adjustedDuration > 20) {
                    String time = formatDuration(adjustedDuration * 2);
                    effectName.append(" (").append(Component.literal(time)).append(")");
                }

                tooltip.add(effectName.withStyle(ChatFormatting.BLUE));
            }
        } else {
            tooltip.add(Component.literal("Effect: ???").withStyle(ChatFormatting.BLUE));
        }

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
                int level = effect.getAmplifier();

                return VapeUtil.formatEffectName(baseName, effectName, level);
            }
        }

        return baseName;
    }

    @Override
    public boolean isEnchantable(ItemStack pStack) {
        return false;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.CUSTOM;
    }

    @Override
    public int getUseDuration(ItemStack pStack, LivingEntity pEntity) {
        return 32;
    }
}
