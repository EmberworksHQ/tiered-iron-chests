package com.automods.tieredironchests.neoforge;

import com.automods.tieredironchests.ChestTier;
import com.automods.tieredironchests.TieredChestContent;
import com.automods.tieredironchests.client.TieredChestRenderer;
import com.automods.tieredironchests.client.TieredChestScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** Client-only wiring (Forge 47 packages), kept in its own class so the dedicated server never loads client classes. */
final class TieredIronChestsNeoForgeClient {
    private TieredIronChestsNeoForgeClient() {
    }

    static void init(IEventBus modBus) {
        modBus.addListener(TieredIronChestsNeoForgeClient::onRegisterRenderers);
        modBus.addListener(TieredIronChestsNeoForgeClient::onClientSetup);
    }

    private static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        for (ChestTier tier : ChestTier.values()) {
            event.registerBlockEntityRenderer(TieredChestContent.blockEntityType(tier), TieredChestRenderer::new);
        }
    }

    /** 1.20.1 has no RegisterMenuScreensEvent; MenuScreens.register (public via Forge's access transformer) on the main thread. */
    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            for (ChestTier tier : ChestTier.values()) {
                MenuScreens.register(TieredChestContent.menuType(tier), TieredChestScreen::new);
            }
        });
    }
}
