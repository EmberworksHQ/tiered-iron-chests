package com.automods.tieredironchests.neoforge;

import com.automods.tieredironchests.ChestTier;
import com.automods.tieredironchests.TieredChestContent;
import com.automods.tieredironchests.client.TieredChestRenderer;
import com.automods.tieredironchests.client.TieredChestScreen;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/** Client-only wiring, kept in its own class so the dedicated server never loads client classes. */
final class TieredIronChestsNeoForgeClient {
    private TieredIronChestsNeoForgeClient() {
    }

    static void init(IEventBus modBus) {
        modBus.addListener(TieredIronChestsNeoForgeClient::onRegisterRenderers);
        modBus.addListener(TieredIronChestsNeoForgeClient::onRegisterMenuScreens);
    }

    private static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        for (ChestTier tier : ChestTier.values()) {
            event.registerBlockEntityRenderer(TieredChestContent.blockEntityType(tier), TieredChestRenderer::new);
        }
    }

    private static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        for (ChestTier tier : ChestTier.values()) {
            event.register(TieredChestContent.menuType(tier), TieredChestScreen::new);
        }
    }
}
