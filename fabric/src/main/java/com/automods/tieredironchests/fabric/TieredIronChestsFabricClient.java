package com.automods.tieredironchests.fabric;

import com.automods.tieredironchests.ChestTier;
import com.automods.tieredironchests.TieredChestContent;
import com.automods.tieredironchests.client.TieredChestRenderer;
import com.automods.tieredironchests.client.TieredChestScreen;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

/** Fabric client entrypoint: vanilla renderer / screen registration, no Fabric API. */
public final class TieredIronChestsFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        for (ChestTier tier : ChestTier.values()) {
            BlockEntityRenderers.register(TieredChestContent.blockEntityType(tier), TieredChestRenderer::new);
            MenuScreens.register(TieredChestContent.menuType(tier), TieredChestScreen::new);
        }
    }
}
