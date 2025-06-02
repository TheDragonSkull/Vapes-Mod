package net.thedragonskull.vapemod.screen;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.network.PacketDistributor;
import net.thedragonskull.vapemod.VapeMod;
import net.thedragonskull.vapemod.block.custom.VapeCatalog;
import net.thedragonskull.vapemod.catalog_offers.*;
import net.thedragonskull.vapemod.component.ModDataComponentTypes;
import net.thedragonskull.vapemod.config.VapeCommonConfigs;
import net.thedragonskull.vapemod.item.custom.DisposableVape;
import net.thedragonskull.vapemod.network.C2SBuyVapePacket;
import net.thedragonskull.vapemod.sound.ModSounds;
import net.thedragonskull.vapemod.util.VapeCatalogUtil;

import java.util.List;
import java.util.Optional;

import static net.thedragonskull.vapemod.util.VapeCatalogUtil.getVisualCostAWithTagInfo;

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

    private static Item COST_ITEM = VapeCatalogUtil.getCatalogCostItem();
    private static final int PRICE_DISPOSABLE = VapeCommonConfigs.CONFIG.PRICE_DISPOSABLE.get();
    private static final int PRICE_NORMAL = VapeCommonConfigs.CONFIG.PRICE_NORMAL.get();

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
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (Minecraft.getInstance().options.keyInventory.isActiveAndMatches(InputConstants.getKey(pKeyCode, pScanCode))) {
            this.onClose();
            return true;
        }

        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    protected void init() {
        super.init();

        if (this.minecraft == null || this.minecraft.level == null) return;

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

            if (stack.getItem() instanceof DisposableVape) {
                PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
                boolean hasPotionEffects = contents != null &&
                        contents.potion()
                                .map(holder -> !holder.value().getEffects().isEmpty())
                                .orElse(false);

                if (!hasPotionEffects) {
                    List<Potion> potions = BuiltInRegistries.POTION.stream()
                            .filter(p -> !p.getEffects().isEmpty())
                            .toList();

                    if (!potions.isEmpty()) {
                        Potion randomPotion = potions.get(this.minecraft.level.getRandom().nextInt(potions.size()));
                        Holder<Potion> holder = BuiltInRegistries.POTION.wrapAsHolder(randomPotion);
                        PotionContents newContents = new PotionContents(holder);

                        stack.set(DataComponents.POTION_CONTENTS, newContents);
                        stack.set(ModDataComponentTypes.RANDOMIZED_POTION, true);
                    }
                }
            }


            if (this.selectedTradeIndex >= 0) {
                PacketDistributor.sendToServer(new C2SBuyVapePacket(this.selectedTradeIndex, this.currentTab.ordinal()));

                VapeCatalogOffers offer = selectedOffer;
                if (offer != null && offer.getTradeLogic() instanceof RerollDisposableOffer) {
                    Minecraft.getInstance().player.playSound(ModSounds.CATALOG_BUY.get(), 1.0F, 1.0F);
                }
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

                if (offer.getTradeLogic() instanceof ExtensionVapeEffectOffer) {
                    for (ItemStack stack : VapeCatalogUtil.getAllRelevantStacks(Minecraft.getInstance().player)) {
                        if (!stack.isEmpty() && stack.is(offer.getCostATag())) {
                            PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
                            boolean hasPotion = contents != null &&
                                    contents.potion().map(holder -> !holder.value().getEffects().isEmpty()).orElse(false);

                            if (!hasPotion) continue;

                            IEnergyStorage cap = stack.getCapability(Capabilities.EnergyStorage.ITEM);
                            if (cap != null) {
                                int stored = cap.getEnergyStored();
                                int max = cap.getMaxEnergyStored();

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

                ItemStack visualCostB = offer.getCostB();

                if (offer.getTradeLogic() instanceof ExtensionVapeEffectOffer logic) {
                    int cost = logic.getDiamondCostFor(Minecraft.getInstance().player, offer);
                    visualCostB = new ItemStack(Items.DIAMOND, cost);
                }

                if (offer.getTradeLogic() instanceof RerollDisposableOffer) {
                    int count = VapeCatalogUtil.countItemsInTagWithFullDurability(Minecraft.getInstance().player, offer.getCostATag());
                    ItemStack costPer = offer.getCostB().copy();
                    costPer.setCount(costPer.getCount() * count * 2);
                    visualCostB = costPer;
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

        updateVapeList();
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
                } else if (logic instanceof RerollDisposableOffer) {
                    labelText = "Reroll Offer";
                } else if (logic instanceof ExtensionVapeEffectOffer) {
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

}
