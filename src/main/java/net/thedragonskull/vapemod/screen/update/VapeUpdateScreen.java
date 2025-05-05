package net.thedragonskull.vapemod.screen.update;

import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.thedragonskull.vapemod.VapeMod;

@Mod.EventBusSubscriber(modid = VapeMod.MOD_ID, value = Dist.CLIENT)
public class VapeUpdateScreen {

    private static boolean hasChecked = false;
    private static boolean showUpdateScreen = false;

    private static final String CURRENT_VERSION = ModList.get()
            .getModContainerById("vapemod")
            .map(container -> container.getModInfo().getVersion().toString())
            .orElse("unknown");

    private static final String MINECRAFT_VERSION = SharedConstants.getCurrentVersion().getName();
    private static final String LATEST_VERSION = MINECRAFT_VERSION + "-1.1.0";

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();

        if (!hasChecked && mc.screen instanceof TitleScreen) {
            hasChecked = true;

            if (!CURRENT_VERSION.equals(LATEST_VERSION)) {
                showUpdateScreen = true;
            }
        }

        if (showUpdateScreen && mc.screen instanceof TitleScreen titleScreen) {
            showUpdateScreen = false;

            mc.setScreen(new AlertScreen(
                    () -> mc.setScreen(titleScreen),
                    Component.translatable("vapemod.update.title"),
                    Component.translatable("vapemod.update.message", CURRENT_VERSION, LATEST_VERSION),
                    Component.translatable("vapemod.update.button.close"),
                    true
            ));
        }
    }

}
