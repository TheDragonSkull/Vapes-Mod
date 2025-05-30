package net.thedragonskull.vapemod.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.registries.ForgeRegistries;
import net.thedragonskull.vapemod.VapeMod;
import net.thedragonskull.vapemod.block.custom.VapeCatalog;
import net.thedragonskull.vapemod.capability.VapeEnergy;
import net.thedragonskull.vapemod.catalog_offers.*;
import net.thedragonskull.vapemod.config.VapeCommonConfigs;
import net.thedragonskull.vapemod.item.custom.DisposableVape;
import net.thedragonskull.vapemod.item.custom.Vape;
import net.thedragonskull.vapemod.network.C2SBuyVapePacket;
import net.thedragonskull.vapemod.network.C2SCloseCatalogPacket;
import net.thedragonskull.vapemod.network.PacketHandler;
import net.thedragonskull.vapemod.util.ModTags;
import net.thedragonskull.vapemod.util.VapeCatalogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.thedragonskull.vapemod.util.VapeCatalogOffersUtil.*;
import static net.thedragonskull.vapemod.util.VapeCatalogUtil.*;

public class VapeCatalogScreen extends Screen {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(VapeMod.MOD_ID, "textures/gui/vape_catalog_screen.png");
    private final VapeCatalogScreen.VapeTradeButton[] tradeOfferButtons = new VapeCatalogScreen.VapeTradeButton[7];
    private int pageIndex = 0;
    private final int BUTTON_WIDTH = 60;
    private final int BUTTON_HEIGHT = 20;
    private Button buyButton;
    private Button scrollUpButton;
    private Button scrollDownButton;

    public enum TabType { DISPOSABLES, NORMAL, SPECIAL }
    private TabType currentTab = TabType.DISPOSABLES;

    private long lastCycleUpdateTime = 0;

    private static Item COST_ITEM = VapeCommonConfigs.getCatalogCostItem();
    private static final int PRICE_DISPOSABLE = VapeCommonConfigs.PRICE_DISPOSABLE.get();
    private static final int PRICE_NORMAL = VapeCommonConfigs.PRICE_NORMAL.get();

    private ItemStack selectedCostA = ItemStack.EMPTY;
    private ItemStack selectedCostB = ItemStack.EMPTY;

    private static final int GUI_WIDTH = 276;
    private static final int GUI_HEIGHT = 166;

    private ItemStack selectedVape = ItemStack.EMPTY;
    private int selectedTradeIndex = -1;
    private VapeCatalogOffers selectedOffer = null;

    int scrollOff;
    private boolean isDragging = false;

    private final BlockPos blockPos;

    public VapeCatalogScreen(BlockPos blockPos) {
        super(Component.literal("Vape Catalog"));
        this.blockPos = blockPos;
    }

    @Override
    protected void init() {
        super.init();

        int width = (this.width - GUI_WIDTH) / 2;
        int height = (this.height - GUI_HEIGHT) / 2;
        int yPos = height + 16 + 2;

        for (int i = 0; i < 7; i++) {
            this.tradeOfferButtons[i] = this.addRenderableWidget(new VapeTradeButton(width + 5, yPos, i,
                    ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
                    btn -> {
                        VapeTradeButton b = (VapeTradeButton) btn;
                        this.selectedVape = b.getResult();
                        this.selectedCostA = b.getCostA();
                        this.selectedCostB = b.getCostB();
                        this.selectedTradeIndex = b.getIndex();
                        this.selectedOffer = b.getOffer();
                        this.updateBuyButtonActiveState();
                    }
            ));
            yPos += 20;
        }

        int centerX = (this.width) / 2;
        int centerY = height + 16 + 2;
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();

        //Scroll buttons
        int size = 20;
        this.scrollUpButton = Button.builder(Component.literal("↑"), btn -> {
            scrollOff = Math.max(0, scrollOff - 1);
            updateVapeList();
            updateScrollButtons();
        }).pos(centerX - 31, height + 115).size(size, size).build();

        this.scrollDownButton = Button.builder(Component.literal("↓"), btn -> {
            List<VapeCatalogOffers> trades = switch (currentTab) {
                case SPECIAL -> VapeOfferRegistry.getSpecialTrades();
                case NORMAL -> VapeOfferRegistry.getNormalTrades();
                case DISPOSABLES -> VapeOfferRegistry.getDisposableTrades();
            };
            scrollOff = Math.min(scrollOff + 1, Math.max(0, trades.size() - 7));
            updateVapeList();
            updateScrollButtons();
        }).pos(centerX - 31, height + 139).size(size, size).build();

        this.addRenderableWidget(scrollUpButton);
        this.addRenderableWidget(scrollDownButton);

        //QVape D Pod tab
        this.addRenderableWidget(new VapeCatalogUtil.TabAndBuyButton(centerX - 31, centerY, 100, 20, Component.literal("QVape D Pod"), btn -> {
            if (this.currentTab != TabType.DISPOSABLES) {
                this.currentTab = TabType.DISPOSABLES;
                this.scrollOff = 0;
                this.selectedVape = ItemStack.EMPTY;
                this.selectedTradeIndex = -1;
                this.selectedOffer = null;
                updateVapeList();
                updateScrollButtons();
                updateBuyButtonActiveState();
            }
        }, SoundEvents.BOOK_PAGE_TURN));

        centerY += 25;

        //QVape Pen V2 tab
        this.addRenderableWidget(new VapeCatalogUtil.TabAndBuyButton(centerX - 31, centerY, 100, 20, Component.literal("QVape Pen V2"), btn -> {
            if (this.currentTab != TabType.NORMAL) {
                this.currentTab = TabType.NORMAL;
                this.scrollOff = 0;
                this.selectedVape = ItemStack.EMPTY;
                this.selectedTradeIndex = -1;
                this.selectedOffer = null;
                updateVapeList();
                updateScrollButtons();
                updateBuyButtonActiveState();
            }

        }, SoundEvents.BOOK_PAGE_TURN));

        centerY += 25;

        //Special Trades tab
        this.addRenderableWidget(new VapeCatalogUtil.TabAndBuyButton(centerX - 31, centerY, 100, 20, Component.literal("Special Offers"), btn -> {
            if (this.currentTab != TabType.SPECIAL) {
                this.currentTab = TabType.SPECIAL;
                this.scrollOff = 0;
                this.selectedVape = ItemStack.EMPTY;
                this.selectedTradeIndex = -1;
                this.selectedOffer = null;
                updateVapeList();
                updateScrollButtons();
                updateBuyButtonActiveState();
            }

        }, SoundEvents.BOOK_PAGE_TURN));

        //Buy button
        this.buyButton = Button.builder(Component.literal("$ Buy $"), btn -> {
            if (this.selectedVape.isEmpty()) return;

            ItemStack stack = this.selectedVape.copy();

            if (stack.getItem() instanceof DisposableVape && PotionUtils.getPotion(stack) == Potions.EMPTY) {
                List<Potion> potions = BuiltInRegistries.POTION.stream()
                        .filter(p -> !p.getEffects().isEmpty() && p != Potions.EMPTY)
                        .toList();
                if (!potions.isEmpty()) {
                    Potion randomPotion = potions.get(this.minecraft.level.getRandom().nextInt(potions.size()));
                    PotionUtils.setPotion(stack, randomPotion);
                    System.out.println("Potion added: " + randomPotion);
                    stack.getOrCreateTag().putBoolean("RandomizedPotion", true);
                }
            }

            if (this.selectedTradeIndex >= 0) {
                PacketHandler.sendToServer(new C2SBuyVapePacket(this.selectedTradeIndex, this.currentTab.ordinal()));
            }

        }).pos(centerX + 75, height + 83).size(54, 20).build();

        this.addRenderableWidget(this.buyButton);

        updateVapeList();
        updateScrollButtons();
    }

    private void updateVapeList() {
        List<VapeCatalogOffers> trades = switch (currentTab) {
            case SPECIAL -> VapeOfferRegistry.getSpecialTrades();
            case NORMAL -> VapeOfferRegistry.getNormalTrades();
            case DISPOSABLES -> VapeOfferRegistry.getDisposableTrades();
        };

        this.scrollOff = Mth.clamp(this.scrollOff, 0, Math.max(0, trades.size() - 7));

        for (int i = 0; i < 7; i++) {
            VapeTradeButton button = this.tradeOfferButtons[i];
            int index = i + scrollOff;

            if (index < trades.size()) {
                VapeCatalogOffers offer = trades.get(index);

                ItemStack visualCostA = offer.isCostAByTag()
                        ? getVisualCostAWithTagInfo(offer.getCostATag())
                        : offer.getCostA();

                ItemStack dynamicCostB = offer.getCostB();

                ItemStack visualResult = offer.isResultByTag()
                        ? getVisualResultFromTag(offer.getResultTag())
                        : offer.getResult();

                if (offer.getTradeLogic() instanceof VapeEffectExtensionOffer) {
                    // Buscar un vape válido en el inventario
                    for (ItemStack stack : Minecraft.getInstance().player.getInventory().items) {
                        if (!stack.isEmpty() && stack.is(offer.getCostATag())) {
                            if (PotionUtils.getPotion(stack) == Potions.EMPTY) continue;

                            Optional<IEnergyStorage> cap = stack.getCapability(ForgeCapabilities.ENERGY).resolve();
                            if (cap.isPresent()) {
                                IEnergyStorage energy = cap.get();
                                int stored = energy.getEnergyStored();
                                int max = energy.getMaxEnergyStored();

                                if (stored > 0 && stored < max) {
                                    int missing = max - stored;
                                    int cost = 1 + (missing / 2);

                                    dynamicCostB = new ItemStack(Items.DIAMOND, cost);
                                    break;
                                }
                            }
                        }
                    }
                }


                button.visible = true;
                button.active = true;
                button.setItem(offer, visualResult, visualCostA, dynamicCostB);
                button.setIndex(index);
            } else {
                button.visible = false;
                button.active = false;
            }
        }

        updateBuyButtonActiveState();
    }

    private void updateBuyButtonActiveState() {

        if (this.selectedTradeIndex >= 0) {
            List<VapeCatalogOffers> trades = switch (currentTab) {
                case SPECIAL -> VapeOfferRegistry.getSpecialTrades();
                case NORMAL -> VapeOfferRegistry.getNormalTrades();
                case DISPOSABLES -> VapeOfferRegistry.getDisposableTrades();
            };

            if (selectedTradeIndex < trades.size()) {
                VapeCatalogOffers offer = trades.get(selectedTradeIndex);
                this.buyButton.active = offer.clientPlayerHasEnough(Minecraft.getInstance().player);
                return;
            }
        }

        this.buyButton.active = false;
    }

    private void updateScrollButtons() {
        List<VapeCatalogOffers> trades = switch (currentTab) {
            case SPECIAL -> VapeOfferRegistry.getSpecialTrades();
            case NORMAL -> VapeOfferRegistry.getNormalTrades();
            case DISPOSABLES -> VapeOfferRegistry.getDisposableTrades();
        };

        if (currentTab == TabType.SPECIAL) {
            this.scrollUpButton.active = false;
            this.scrollDownButton.active = false;
        } else {
            int total = trades.size();
            int visible = 7;
            int max = Math.max(0, total - visible);
            this.scrollUpButton.active = scrollOff > 0;
            this.scrollDownButton.active = scrollOff < max;
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (minecraft != null && minecraft.level != null) {
            BlockState state = minecraft.level.getBlockState(blockPos);
            if (!(state.getBlock() instanceof VapeCatalog)) {
                minecraft.setScreen(null);
                return;
            }
        }

        if (selectedOffer != null) {
            this.buyButton.active = selectedOffer.clientPlayerHasEnough(Minecraft.getInstance().player);
        } else {
            this.buyButton.active = false;
        }
    }

    private void renderScroller(GuiGraphics graphics, int x, int y) {
        List<VapeCatalogOffers> trades = switch (currentTab) {
            case SPECIAL -> VapeOfferRegistry.getSpecialTrades();
            case NORMAL -> VapeOfferRegistry.getNormalTrades();
            case DISPOSABLES -> VapeOfferRegistry.getDisposableTrades();
        };

        int total = trades.size();
        int visible = 7;
        int maxScroll = Math.max(0, total - visible);

        if (maxScroll > 0 && currentTab != TabType.SPECIAL) {
            int thumbHeight = 27;
            int trackHeight = 140;

            float scrollRatio = (float) this.scrollOff / maxScroll;
            int thumbY = (int) (scrollRatio * (trackHeight - thumbHeight));

            graphics.blit(BACKGROUND, x + 94, y + 18 + thumbY, 0, 0.0F, 199.0F, 6, 27, 512, 256);
        } else {
            graphics.blit(BACKGROUND, x + 94, y + 18, 0, 6.0F, 199.0F, 6, 27, 512, 256);
        }
    }

    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        int labelY = (this.height - GUI_HEIGHT) / 2 + 6;
        int labelXLeft = (this.width - GUI_WIDTH) / 2 + 48;

        // === Dynamic Tab Title ===
        String labelText;

        if (currentTab != TabType.SPECIAL) {
            labelText = "Vape Offers";
        } else {
            if (this.selectedOffer != null) {
                ISpecialOfferLogic logic = this.selectedOffer.getTradeLogic();
                if (logic instanceof RandomPotionRechargeOffer) {
                    labelText = "Randomize Offer";
                } else if (logic instanceof RecycleDisposableOffer) {
                    labelText = "Recycle Offer";
                } else if (logic instanceof DisposableRerollOffer) {
                    labelText = "Reroll Offer";
                } else if (logic instanceof VapeEffectExtensionOffer) {
                    labelText = "Extension Offer";
                } else {
                    labelText = "Special Offers";
                }
            } else {
                labelText = "Special Offers";
            }
        }

        int fontWidth = this.font.width(labelText);
        int labelX = labelXLeft - fontWidth / 2;

        pGuiGraphics.drawString(this.font, labelText, labelX, labelY, 4210752, false);

        // === Right Section ===
        int vapeModelsWidth = this.font.width("Vape Models");
        int centerX = (this.width / 2) + 50;
        pGuiGraphics.drawString(this.font, "Vape Models", centerX - vapeModelsWidth, labelY, 4210752, false);

        int vape3DWidth = this.font.width("3D View");
        int displayCenter = (this.width / 2) + 75 + (54 - vape3DWidth) / 2;
        pGuiGraphics.drawString(this.font, "3D View", displayCenter, labelY, 4210752, false);
    }


    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);

        int x = (this.width - GUI_WIDTH) / 2;
        int y = (this.height - GUI_HEIGHT) / 2;

        // Background
        graphics.blit(BACKGROUND, x, y, 0, 0, GUI_WIDTH, GUI_HEIGHT, 512, 256);

        this.renderScroller(graphics, x, y);
        this.renderLabels(graphics, mouseX, mouseY);
        super.render(graphics, mouseX, mouseY, partialTicks);

        // Tooltips
        for (Renderable widget : this.renderables) {
            if (widget instanceof VapeTradeButton button) {
                button.renderToolTip(graphics, mouseX, mouseY, this.font);
            }
        }

        if (this.buyButton != null && this.buyButton.isHoveredOrFocused()) {
            Component tooltip = null;

            if (this.selectedVape.isEmpty()) {
                if (!this.buyButton.active) {
                    tooltip = Component.literal("No offer selected").withStyle(ChatFormatting.RED);
                }
            }

            if (tooltip != null) {
                graphics.renderTooltip(this.font, tooltip, mouseX, mouseY);
            }
        }

        // 3D item
        if (this.selectedOffer != null) {
            ItemStack itemToRender = this.selectedOffer.isResultByTag()
                    ? VapeCatalogUtil.getCycledItemFromTag(this.selectedOffer.getResultTag())
                    : this.selectedOffer.getResult().copy();

            int centerX = this.width / 2;
            int centerY = this.height / 2;
            int scale = 110;
            boolean is3d = itemToRender.getItem() instanceof Vape || itemToRender.getItem() instanceof DisposableVape;
            int yOffset = is3d ? -30 : -23;

            PoseStack poseStack = graphics.pose();
            poseStack.pushPose();

            poseStack.translate((centerX + 100) + 1.5F, centerY + yOffset, 100);
            poseStack.scale(scale, scale, scale);

            poseStack.mulPose(Axis.XP.rotationDegrees(180f));

            float rotation = (System.currentTimeMillis() % 3600L) / 10f;
            poseStack.mulPose(Axis.YP.rotationDegrees(-rotation));

            if (is3d) {
                poseStack.mulPose(Axis.ZP.rotationDegrees(25));
            }

            RenderSystem.disableCull();

            Minecraft.getInstance().getItemRenderer().renderStatic(
                    itemToRender,
                    ItemDisplayContext.GROUND,
                    15728880,
                    OverlayTexture.NO_OVERLAY,
                    poseStack,
                    graphics.bufferSource(),
                    null,
                    0
            );

            graphics.bufferSource().endBatch();
            poseStack.popPose();
        }

    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        super.onClose();
        if (blockPos != null) {
            PacketHandler.sendToServer(new C2SCloseCatalogPacket(blockPos));
        }
    }

    private boolean canScroll(int pNumOffers) {
        return currentTab != TabType.SPECIAL && pNumOffers > 7;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        List<VapeCatalogOffers> trades = switch (currentTab) {
            case SPECIAL -> VapeOfferRegistry.getSpecialTrades();
            case NORMAL -> VapeOfferRegistry.getNormalTrades();
            case DISPOSABLES -> VapeOfferRegistry.getDisposableTrades();
        };

        if (this.canScroll(trades.size())) {
            int maxScroll = trades.size() - 7;
            this.scrollOff = Mth.clamp((int)(this.scrollOff - delta), 0, maxScroll);
            this.updateVapeList();
            this.updateScrollButtons();
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.isDragging = false;

        int x = (this.width - GUI_WIDTH) / 2;
        int y = (this.height - GUI_HEIGHT) / 2;

        List<VapeCatalogOffers> trades = switch (currentTab) {
            case SPECIAL -> VapeOfferRegistry.getSpecialTrades();
            case NORMAL -> VapeOfferRegistry.getNormalTrades();
            case DISPOSABLES -> VapeOfferRegistry.getDisposableTrades();
        };

        if (this.canScroll(trades.size()) &&
                mouseX > x + 94 && mouseX < x + 94 + 6 &&
                mouseY > y + 18 && mouseY <= y + 18 + 139 + 1) {
            this.isDragging = true;
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.isDragging) {
            int y = (this.height - GUI_HEIGHT) / 2;
            int top = y + 18;
            int bottom = top + 139;

            List<VapeCatalogOffers> trades = switch (currentTab) {
                case SPECIAL -> VapeOfferRegistry.getSpecialTrades();
                case NORMAL -> VapeOfferRegistry.getNormalTrades();
                case DISPOSABLES -> VapeOfferRegistry.getDisposableTrades();
            };

            int maxScroll = Math.max(0, trades.size() - 7);
            float scrollProgress = ((float)mouseY - top - 13.5F) / ((bottom - top) - 27.0F);
            scrollProgress = Mth.clamp(scrollProgress, 0.0F, 1.0F);

            this.scrollOff = Mth.clamp((int)(scrollProgress * maxScroll + 0.5F), 0, maxScroll);
            this.updateVapeList();
            this.updateScrollButtons();
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @OnlyIn(Dist.CLIENT)
    public class VapeTradeButton extends Button {
        private ItemStack result;
        private ItemStack costA;
        private ItemStack costB;
        private TagKey<Item> costATag = null;

        private VapeCatalogOffers offer;
        private int index;

        public VapeTradeButton(int x, int y, int index, ItemStack costA, ItemStack costB, ItemStack result, OnPress onPress) {
            super(x, y, 88, 20, Component.empty(), onPress, DEFAULT_NARRATION);
            this.index = index;
            this.costA = costA;
            this.costB = costB;
            this.result = result;
        }

        public ItemStack getResult() {
            return result;
        }

        public ItemStack getCostA() {
            return this.costA;
        }

        public ItemStack getCostB() {
            return this.costB;
        }

        public int getIndex() {
            return this.index;
        }

        public VapeCatalogOffers getOffer() {
            return this.offer;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public void setItem(VapeCatalogOffers offer, ItemStack result, ItemStack costA, ItemStack costB) {
            this.offer = offer;
            this.costA = costA.copy();
            this.costB = costB.copy();
            this.result = result.copy();

            if (isTagCost(costA)) {
                this.costATag = getTagFromCostA(this.costA);

                if (!this.costA.isEmpty()) {
                    this.costA.getOrCreateTag().putString("TagKey", costATag.location().toString());
                }

                if (!this.result.isEmpty() && offer.isResultByTag()) {
                    this.result.getOrCreateTag().putString("TagKey", costATag.location().toString());
                }

            } else {
                this.costATag = null;
            }

        }

        private void renderButtonArrows(GuiGraphics pGuiGraphics, int pPosX, int pPosY) {
            boolean enough = offer != null && offer.clientPlayerHasEnough(Minecraft.getInstance().player);
            RenderSystem.enableBlend();

            if (enough) {
                pGuiGraphics.blit(BACKGROUND, pPosX + 5 + 35 + 10, pPosY + 4, 0, 15.0F, 171.0F, 10, 9, 512, 256);
            } else {
                pGuiGraphics.blit(BACKGROUND, pPosX + 5 + 35 + 10, pPosY + 4, 0, 25.0F, 171.0F, 10, 9, 512, 256);
            }

        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.renderWidget(graphics, mouseX, mouseY, partialTicks);

            PoseStack poseStack = graphics.pose();
            poseStack.pushPose();

            int x = this.getX();
            int y = this.getY() + 1;

            // Render costA
            ItemStack toRenderA = this.costA;
            if (this.costATag != null) {
                List<Item> tagItems = ForgeRegistries.ITEMS.getValues().stream()
                        .filter(item -> item.builtInRegistryHolder().is(this.costATag))
                        .toList();

                if (!tagItems.isEmpty()) {
                    long time = System.currentTimeMillis() / 1000L;
                    int index = (int)(time % tagItems.size());
                    toRenderA = new ItemStack(tagItems.get(index));
                }
            }

            if (offer != null && offer.getTradeLogic() instanceof RecycleDisposableOffer) {
                List<Item> tagItems = ForgeRegistries.ITEMS.getValues().stream()
                        .filter(item -> item.builtInRegistryHolder().is(this.costATag))
                        .toList();
                if (!tagItems.isEmpty()) {
                    long time = System.currentTimeMillis() / 1000L;
                    int index = (int)(time % tagItems.size());
                    ItemStack depletedVape = new ItemStack(tagItems.get(index));
                    if (depletedVape.isDamageableItem()) {
                        depletedVape.setDamageValue(depletedVape.getMaxDamage());
                    }
                    toRenderA = depletedVape;
                }
            }
            graphics.renderItem(toRenderA, x + 2, y + 1);
            graphics.renderItemDecorations(Minecraft.getInstance().font, toRenderA, x + 2, y + 1);


            //Render costB
            if (!this.costB.isEmpty()) {
                graphics.renderItem(this.costB, x + 24, y + 1);
                graphics.renderItemDecorations(Minecraft.getInstance().font, this.costB, x + 24, y + 1);
            }

            // Render result
            ItemStack resultToRender = this.result;
            if (this.costATag != null) {
                List<Item> tagItems = ForgeRegistries.ITEMS.getValues().stream()
                        .filter(item -> item.builtInRegistryHolder().is(this.costATag))
                        .toList();

                if (!tagItems.isEmpty()) {
                    long time = System.currentTimeMillis() / 1000L;
                    int index = (int)(time % tagItems.size());
                    ItemStack vapeItem = new ItemStack(tagItems.get(index));

                    if (vapeItem.getCapability(ForgeCapabilities.ENERGY).isPresent()) {
                        ItemStack vapeWithEnergy = vapeItem.copy();
                        vapeWithEnergy.getCapability(ForgeCapabilities.ENERGY).ifPresent(cap -> {
                            if (cap instanceof VapeEnergy energyCap) {
                                VapeEnergy.setInt(vapeWithEnergy, "Energy", energyCap.getMaxEnergyStored());
                            }
                        });

                        resultToRender = vapeWithEnergy;
                    } else {
                        resultToRender = vapeItem.copy();
                        if (resultToRender.isDamageableItem()) {
                            resultToRender.setDamageValue(0);
                        }
                    }
                }
            }

            if (offer != null && offer.getTradeLogic() instanceof RecycleDisposableOffer) {
                int price = VapeCommonConfigs.PRICE_DISPOSABLE.get();
                int refund = Math.max(1, (int)(price * 0.25));
                resultToRender = new ItemStack(VapeCommonConfigs.getCatalogCostItem(), refund);
            }

            graphics.renderItem(resultToRender, x + 68, y + 1);
            graphics.renderItemDecorations(Minecraft.getInstance().font, resultToRender, x + 68, y);

            poseStack.popPose();

            this.renderButtonArrows(graphics, x, y);

            int arrowX = x + 5 + 35 + 10;
            int arrowY = y + 4;
            int arrowWidth = 10;
            int arrowHeight = 9;
            boolean enough = offer != null && offer.clientPlayerHasEnough(Minecraft.getInstance().player);

            if (!enough) {
                if (mouseX >= arrowX && mouseX < arrowX + arrowWidth &&
                        mouseY >= arrowY && mouseY < arrowY + arrowHeight) {
                    graphics.renderTooltip(font, Component.literal("Not enough currency").withStyle(ChatFormatting.RED), mouseX, mouseY);
                }
            }
        }

        public void renderToolTip(GuiGraphics graphics, int mouseX, int mouseY, Font font) {
            if (isHovered) {
                if (mouseX < this.getX() + 20) {

                    // CostA
                    ItemStack tooltipStack = this.costA;
                    if (this.costATag != null) {
                        List<Item> tagItems = ForgeRegistries.ITEMS.getValues().stream()
                                .filter(item -> item.builtInRegistryHolder().is(this.costATag))
                                .toList();
                        if (!tagItems.isEmpty()) {
                            long time = System.currentTimeMillis() / 1000L;
                            int index = (int)(time % tagItems.size());
                            tooltipStack = new ItemStack(tagItems.get(index));
                        }
                    }

                    if (offer != null && offer.getTradeLogic() instanceof RecycleDisposableOffer) {
                        List<Item> tagItems = ForgeRegistries.ITEMS.getValues().stream()
                                .filter(item -> item.builtInRegistryHolder().is(this.costATag))
                                .toList();
                        if (!tagItems.isEmpty()) {
                            long time = System.currentTimeMillis() / 1000L;
                            int index = (int)(time % tagItems.size());
                            ItemStack depletedVape = new ItemStack(tagItems.get(index));
                            if (depletedVape.isDamageableItem()) {
                                depletedVape.setDamageValue(depletedVape.getMaxDamage());
                            }
                            tooltipStack = depletedVape;
                        }

                        graphics.renderTooltip(font, tooltipStack, mouseX, mouseY);

                    } else if (offer != null && offer.getTradeLogic() instanceof VapeEffectExtensionOffer) {
                        List<Component> originalTooltip = tooltipStack.getTooltipLines(Minecraft.getInstance().player, TooltipFlag.Default.NORMAL);
                        List<Component> modifiable = new ArrayList<>();

                        for (Component line : originalTooltip) {
                            String text = line.getString();

                            if (text.equals("No Effects")) {
                                modifiable.add(Component.literal("Current effect").withStyle(ChatFormatting.BLUE));
                            } else if (text.startsWith("Capacity:")) {
                                modifiable.add(Component.literal("Current Capacity").withStyle(ChatFormatting.DARK_AQUA));
                            } else {
                                modifiable.add(line);
                            }
                        }

                        ResourceLocation id = ForgeRegistries.ITEMS.getKey(tooltipStack.getItem());
                        if (id != null && Minecraft.getInstance().options.advancedItemTooltips) {
                            modifiable.add(Component.literal(id.toString()).withStyle(ChatFormatting.DARK_GRAY));
                        }

                        if (tooltipStack.hasTag() && Minecraft.getInstance().options.advancedItemTooltips) {
                            modifiable.add(Component.literal("NBT: " + tooltipStack.getTag().getAllKeys().size() + " tag(s)").withStyle(ChatFormatting.DARK_GRAY));
                        }

                        graphics.renderTooltip(font, modifiable, Optional.empty(), mouseX, mouseY);
                    } else {
                        graphics.renderTooltip(font, tooltipStack, mouseX, mouseY);

                    }

                } else if (mouseX < this.getX() + 42 && mouseX > this.getX() + 25 && !this.costB.isEmpty()) {

                    // CostB
                    graphics.renderTooltip(font, this.costB, mouseX, mouseY);
                } else if (mouseX > this.getX() + 65) {

                    // Result
                    ItemStack tooltipStack = this.result;

                    if (this.costATag != null) {
                        List<Item> tagItems = ForgeRegistries.ITEMS.getValues().stream()
                                .filter(item -> item.builtInRegistryHolder().is(this.costATag))
                                .toList();

                        if (!tagItems.isEmpty()) {
                            long time = System.currentTimeMillis() / 1000L;
                            int index = (int)(time % tagItems.size());
                            ItemStack vapeItem = new ItemStack(tagItems.get(index));

                            if (vapeItem.getCapability(ForgeCapabilities.ENERGY).isPresent()) {
                                ItemStack vapeWithEnergy = vapeItem.copy();
                                vapeWithEnergy.getCapability(ForgeCapabilities.ENERGY).ifPresent(cap -> {
                                    if (cap instanceof VapeEnergy energyCap) {
                                        VapeEnergy.setInt(vapeWithEnergy, "Energy", energyCap.getMaxEnergyStored());
                                    }
                                });

                                tooltipStack = vapeWithEnergy;
                            } else {
                                tooltipStack = vapeItem.copy();
                                if (tooltipStack.isDamageableItem()) {
                                    tooltipStack.setDamageValue(0);
                                }
                            }
                        }

                    }

                    if (offer != null && offer.getTradeLogic() instanceof RecycleDisposableOffer) {
                        int price = VapeCommonConfigs.PRICE_DISPOSABLE.get();
                        int refund = Math.max(1, (int)(price * 0.25));
                        tooltipStack = new ItemStack(VapeCommonConfigs.getCatalogCostItem(), refund);
                    }

                    List<Component> tooltip = new ArrayList<>(tooltipStack.getTooltipLines(Minecraft.getInstance().player, TooltipFlag.Default.NORMAL));

                    for (int i = 0; i < tooltip.size(); i++) {
                        String line = tooltip.get(i).getString();
                        if (offer != null && offer.getTradeLogic() instanceof VapeEffectExtensionOffer && line.equals("No Effects")) {
                            tooltip.set(i, Component.literal("Refill current effect").withStyle(ChatFormatting.BLUE));
                            break;
                        } else if (currentTab == TabType.SPECIAL && line.equals("No Effects")) {
                            tooltip.set(i, Component.literal("Effect: ???").withStyle(ChatFormatting.BLUE));
                            break;
                        }
                    }

                    ResourceLocation id = ForgeRegistries.ITEMS.getKey(tooltipStack.getItem());
                    if (id != null && Minecraft.getInstance().options.advancedItemTooltips) {
                        tooltip.add(Component.literal(id.toString()).withStyle(ChatFormatting.DARK_GRAY));
                    }

                    if (tooltipStack.hasTag() && Minecraft.getInstance().options.advancedItemTooltips) {
                        tooltip.add(Component.literal("NBT: " + tooltipStack.getTag().getAllKeys().size() + " tag(s)").withStyle(ChatFormatting.DARK_GRAY));
                    }

                    graphics.renderTooltip(Minecraft.getInstance().font, tooltip, Optional.empty(), mouseX, mouseY);
                }
            }
        }

    }

}


