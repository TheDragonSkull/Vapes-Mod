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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import net.thedragonskull.vapemod.VapeMod;
import net.thedragonskull.vapemod.block.custom.VapeCatalog;
import net.thedragonskull.vapemod.config.VapeCommonConfigs;
import net.thedragonskull.vapemod.item.custom.DisposableVape;
import net.thedragonskull.vapemod.network.C2SBuyVapePacket;
import net.thedragonskull.vapemod.network.C2SCloseCatalogPacket;
import net.thedragonskull.vapemod.network.PacketHandler;
import net.thedragonskull.vapemod.sound.ModSounds;
import net.thedragonskull.vapemod.util.ModTags;
import net.thedragonskull.vapemod.util.VapeCatalogUtil;

import java.util.ArrayList;
import java.util.List;

import static net.thedragonskull.vapemod.util.VapeCatalogUtil.hasEnoughCurrency;

public class VapeCatalogScreen extends Screen {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(VapeMod.MOD_ID, "textures/gui/vape_catalog_screen.png");
    private final VapeCatalogScreen.VapeTradeButton[] tradeOfferButtons = new VapeCatalogScreen.VapeTradeButton[7];
    private List<ItemStack> vapeList = new ArrayList<>();
    private List<ItemStack> fullVapeList = new ArrayList<>();
    private int pageIndex = 0;
    private final int BUTTON_WIDTH = 60;
    private final int BUTTON_HEIGHT = 20;
    private Button buyButton;
    private Button scrollUpButton;
    private Button scrollDownButton;

    private enum TabType { DISPOSABLES, NORMAL }
    private TabType currentTab = TabType.DISPOSABLES;

    ItemStack costA = VapeTradeButton.getCostA();

    private static Item COST_ITEM = VapeCommonConfigs.getCatalogCostItem();
    private static final int PRICE_DISPOSABLE = VapeCommonConfigs.PRICE_DISPOSABLE.get();
    private static final int PRICE_NORMAL = VapeCommonConfigs.PRICE_NORMAL.get();

    private static final int GUI_WIDTH = 276;
    private static final int GUI_HEIGHT = 166;

    private ItemStack selectedVape = ItemStack.EMPTY;
    int scrollOff;
    private boolean isDragging = false;

    private final BlockPos blockPos;

    public VapeCatalogScreen(BlockPos blockPos) {
        super(Component.literal("Vape Catalog"));
        this.fullVapeList = generateFullVapeList();
        this.vapeList = paginateVapeList();
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
                    ItemStack.EMPTY, ItemStack.EMPTY,
                    btn -> {
                        VapeTradeButton b = (VapeTradeButton) btn;
                        this.selectedVape = b.getResult();
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
            scrollOff = Math.min(scrollOff + 1, Math.max(0, fullVapeList.size() - 7));
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
                updateVapeList();
                updateScrollButtons();
            }
        }, SoundEvents.BOOK_PAGE_TURN));

        centerY += 25;

        //QVape Pen V2 tab
        this.addRenderableWidget(new VapeCatalogUtil.TabAndBuyButton(centerX - 31, centerY, 100, 20, Component.literal("QVape Pen V2"), btn -> {
            if (this.currentTab != TabType.NORMAL) {
                this.currentTab = TabType.NORMAL;
                this.scrollOff = 0;
                this.selectedVape = ItemStack.EMPTY;
                updateVapeList();
                updateScrollButtons();
            }
        }, SoundEvents.BOOK_PAGE_TURN));

        //Buy button
        this.buyButton = Button.builder(Component.literal("$ Buy $"), btn -> {
            if (this.selectedVape.isEmpty()) return;

            ItemStack stack = this.selectedVape.copy();

            // Asignar poción aleatoria si es un vape desechable sin poción
            if (stack.getItem() instanceof DisposableVape && PotionUtils.getPotion(stack) == Potions.EMPTY) {
                List<Potion> potions = BuiltInRegistries.POTION.stream()
                        .filter(p -> !p.getEffects().isEmpty() && p != Potions.EMPTY)
                        .toList();
                if (!potions.isEmpty()) {
                    Potion randomPotion = potions.get(this.minecraft.level.getRandom().nextInt(potions.size()));
                    PotionUtils.setPotion(stack, randomPotion);
                }
            }

            PacketHandler.sendToServer(new C2SBuyVapePacket(stack));

        }).pos(centerX + 75, height + 83).size(54, 20).build();


        buyButton.active = !this.selectedVape.isEmpty() && hasEnoughCurrency(this.minecraft.player, costA);
        this.addRenderableWidget(this.buyButton);

        updateVapeList();
        updateScrollButtons();
    }

    private void removeCurrency(Player player, Item currencyItem, int amount) {
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack stack = player.getInventory().items.get(i);
            if (stack.getItem() == currencyItem) {
                int toRemove = Math.min(stack.getCount(), amount);
                stack.shrink(toRemove);
                amount -= toRemove;
                if (amount <= 0) break;
            }
        }
    }

    private void updateVapeList() {
        this.fullVapeList = generateVapeListForTab(this.currentTab);
        this.scrollOff = Mth.clamp(this.scrollOff, 0, Math.max(0, fullVapeList.size() - 7));

        int from = this.scrollOff;
        int to = Math.min(from + 7, fullVapeList.size());
        this.vapeList = new ArrayList<>(fullVapeList);

        for (int i = 0; i < 7; i++) {
            if (i + from < fullVapeList.size()) {
                ItemStack vape = fullVapeList.get(i + from);
                int price = getPriceForVape(vape);
                ItemStack costStack = new ItemStack(COST_ITEM, price);

                VapeTradeButton button = this.tradeOfferButtons[i];
                button.visible = true;
                button.active = true;
                button.setItem(vape, costStack);
            } else {
                VapeTradeButton button = this.tradeOfferButtons[i];
                button.visible = false;
                button.active = false;
            }
        }

    }

    private void updateScrollButtons() {
        int total = this.fullVapeList.size();
        int visible = 7;
        int maxScroll = Math.max(0, total - visible);

        this.scrollUpButton.active = scrollOff > 0;
        this.scrollDownButton.active = scrollOff < maxScroll;
    }

    private int getPriceForVape(ItemStack vape) {
        if (vape.is(ModTags.Items.DISPOSABLE_VAPES)) {
            return PRICE_DISPOSABLE;
        } else if (vape.is(ModTags.Items.VAPES)) {
            return PRICE_NORMAL;
        } else {
            return PRICE_NORMAL;
        }
    }

    @Override
    public void tick() {
        super.tick();
        boolean hasSelection = !this.selectedVape.isEmpty();
        boolean hasCurrency = hasSelection && hasEnoughCurrency(this.minecraft.player, costA);
        this.buyButton.active = hasSelection && hasCurrency;

        if (minecraft != null && minecraft.level != null) {
            BlockState state = minecraft.level.getBlockState(blockPos);
            if (!(state.getBlock() instanceof VapeCatalog)) {
                minecraft.setScreen(null);
            }
        }

        if (!this.selectedVape.isEmpty()) {
            int price = getPriceForVape(this.selectedVape);
            this.buyButton.active = hasEnoughCurrency(this.minecraft.player, new ItemStack(COST_ITEM, price));
        } else {
            this.buyButton.active = false;
        }

    }

    private void renderScroller(GuiGraphics pGuiGraphics, int pPosX, int pPosY, List<ItemStack> vapeList) {
        int total = this.fullVapeList.size();
        int visible = 7;
        int maxScroll = Math.max(0, total - visible);

        if (maxScroll > 0) {
            int thumbHeight = 27;
            int trackHeight = 140;

            float scrollRatio = (float) this.scrollOff / maxScroll;
            int thumbY = (int) (scrollRatio * (trackHeight - thumbHeight));

            pGuiGraphics.blit(BACKGROUND, pPosX + 94, pPosY + 18 + thumbY, 0, 0.0F, 199.0F, 6, 27, 512, 256);
        } else {
            pGuiGraphics.blit(BACKGROUND, pPosX + 94, pPosY + 18, 0, 6.0F, 199.0F, 6, 27, 512, 256);
        }
    }

    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        int fontWidth = this.font.width("Vape List");
        int labelX = (this.width - GUI_WIDTH) / 2 + 48 - fontWidth / 2;
        int labelY = (this.height - GUI_HEIGHT) / 2 + 6;

        pGuiGraphics.drawString(this.font, "Vape List", labelX, labelY, 4210752, false);

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

        this.renderScroller(graphics, x, y, this.vapeList);
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
                    tooltip = Component.literal("No vape selected").withStyle(ChatFormatting.RED);
                }
            }

            if (tooltip != null) {
                graphics.renderTooltip(this.font, tooltip, mouseX, mouseY);
            }
        }

        // 3D item
        if (!this.selectedVape.isEmpty()) {
            int centerX = this.width / 2;
            int centerY = this.height / 2;
            int scale = 110;

            PoseStack poseStack = graphics.pose();
            poseStack.pushPose();

            poseStack.translate((centerX + 100) + 1.5F, centerY - 30, 100);
            poseStack.scale(scale, scale, scale);

            poseStack.mulPose(Axis.XP.rotationDegrees(180f));


            float rotation = (System.currentTimeMillis() % 3600L) / 10f;
            poseStack.mulPose(Axis.YP.rotationDegrees(-rotation));
            poseStack.mulPose(Axis.ZP.rotationDegrees(25));

            RenderSystem.disableCull();

            Minecraft.getInstance().getItemRenderer().renderStatic(
                    this.selectedVape,
                    ItemDisplayContext.GROUND,
                    15728880,
                    OverlayTexture.NO_OVERLAY,
                    poseStack,
                    graphics.bufferSource(),
                    null,
                    0
            );

            poseStack.popPose();
        }

    }

    private List<ItemStack> generateVapeListForTab(TabType tab) {
        TagKey<Item> tag = switch (tab) {
            case DISPOSABLES -> ModTags.Items.DISPOSABLE_VAPES;
            case NORMAL -> ModTags.Items.VAPES;
        };

        return ForgeRegistries.ITEMS.getValues().stream()
                .filter(item -> item.builtInRegistryHolder().is(tag))
                .map(ItemStack::new)
                .toList();
    }

    private List<ItemStack> generateFullVapeList() {
        TagKey<Item> tag = ModTags.Items.DISPOSABLE_VAPES;

        return ForgeRegistries.ITEMS.getValues().stream()
                .filter(item -> item.builtInRegistryHolder().is(tag))
                .map(ItemStack::new)
                .toList();
    }

    private List<ItemStack> paginateVapeList() {
        int from = Mth.clamp(this.scrollOff, 0, Math.max(0, fullVapeList.size() - 7));
        int to = Math.min(from + 7, fullVapeList.size());
        return new ArrayList<>(fullVapeList.subList(from, to));
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
        return pNumOffers > 7;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int total = fullVapeList.size();

        if (this.canScroll(total)) {
            int maxScroll = total - 7;
            this.scrollOff = Mth.clamp((int)(this.scrollOff - delta), 0, maxScroll);
            this.vapeList = paginateVapeList();
            this.clearWidgets();
            this.init();
        }

        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.isDragging = false;
        int x = (this.width - GUI_WIDTH) / 2;
        int y = (this.height - GUI_HEIGHT) / 2;

        if (this.canScroll(this.fullVapeList.size()) &&
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

            int maxScroll = Math.max(0, fullVapeList.size() - 7);
            float scrollProgress = ((float)mouseY - top - 13.5F) / ((bottom - top) - 27.0F);
            scrollProgress = Mth.clamp(scrollProgress, 0.0F, 1.0F);

            this.scrollOff = Mth.clamp((int)(scrollProgress * maxScroll + 0.5F), 0, maxScroll);
            this.vapeList = paginateVapeList();
            this.clearWidgets();
            this.init();
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @OnlyIn(Dist.CLIENT)
    public class VapeTradeButton extends Button {
        private ItemStack result;
        private static ItemStack costA;
        private final int index;

        public VapeTradeButton(int x, int y, int index, ItemStack costA, ItemStack result, OnPress onPress) {
            super(x, y, 88, 20, Component.empty(), onPress, DEFAULT_NARRATION);
            this.index = index;
            this.costA = costA;
            this.result = result;
        }

        public ItemStack getResult() {
            return result;
        }

        public static ItemStack getCostA() {
            return costA;
        }

        public int getIndex() {
            return this.index;
        }

        public void setItem(ItemStack result, ItemStack costA) {
            this.result = result;
            this.costA = costA;
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.renderWidget(graphics, mouseX, mouseY, partialTicks);

            PoseStack poseStack = graphics.pose();
            poseStack.pushPose();

            int x = this.getX();
            int y = this.getY() + 1;

            // Render costA
            graphics.renderItem(this.costA, x + 2, y);
            graphics.renderItemDecorations(Minecraft.getInstance().font, this.costA, x + 2, y);

            // Render result
            graphics.renderItem(this.result, x + 68, y + 1);
            graphics.renderItemDecorations(Minecraft.getInstance().font, this.result, x + 68, y);

            poseStack.popPose();

            this.renderButtonArrows(graphics, x, y);

            int arrowX = x + 5 + 35;
            int arrowY = y + 4;
            int arrowWidth = 10;
            int arrowHeight = 9;
            boolean enough = hasEnoughCurrency(Minecraft.getInstance().player, costA);

            if (!enough) {
                if (mouseX >= arrowX && mouseX < arrowX + arrowWidth &&
                        mouseY >= arrowY && mouseY < arrowY + arrowHeight) {
                    graphics.renderTooltip(font, Component.literal("Not enough currency").withStyle(ChatFormatting.RED), mouseX, mouseY);
                }
            }
        }

        private void renderButtonArrows(GuiGraphics pGuiGraphics, int pPosX, int pPosY) {
            boolean enough = hasEnoughCurrency(Minecraft.getInstance().player, costA);
            RenderSystem.enableBlend();

            if (enough) {
                pGuiGraphics.blit(BACKGROUND, pPosX + 5 + 35, pPosY + 4, 0, 15.0F, 171.0F, 10, 9, 512, 256);
            } else {
                pGuiGraphics.blit(BACKGROUND, pPosX + 5 + 35, pPosY + 4, 0, 25.0F, 171.0F, 10, 9, 512, 256);
            }

        }

        public void renderToolTip(GuiGraphics graphics, int mouseX, int mouseY, Font font) {
            if (isHovered) {
                if (mouseX < this.getX() + 20) {
                    graphics.renderTooltip(font, this.costA, mouseX, mouseY);
                } else if (mouseX > this.getX() + 65) {
                    graphics.renderTooltip(font, this.result, mouseX, mouseY);
                }
            }
        }
    }


}