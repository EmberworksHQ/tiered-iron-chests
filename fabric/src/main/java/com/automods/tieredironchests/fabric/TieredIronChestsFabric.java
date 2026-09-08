package com.automods.tieredironchests.fabric;

import com.automods.tieredironchests.ChestTier;
import com.automods.tieredironchests.ChestUpgradeItem;
import com.automods.tieredironchests.TieredChestBlock;
import com.automods.tieredironchests.TieredChestBlockEntity;
import com.automods.tieredironchests.TieredChestContent;
import com.automods.tieredironchests.TieredChestMenu;
import com.automods.tieredironchests.TieredIronChests;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.List;

/**
 * Fabric adapter. Registration goes through the vanilla registries (open during mod init thanks to Fabric API's
 * registry-sync module, which also keeps client and server ids in step); Fabric API's resource loader serves the
 * jar's assets and data; creative-tab placement uses {@link ItemGroupEvents}.
 */
public final class TieredIronChestsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        for (ChestTier tier : ChestTier.values()) {
            registerTier(tier);
        }
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(entries -> {
            List<ItemStack> ours = TieredChestContent.creativeTabEntries();
            entries.addAfter(Items.CHEST, ours.toArray(ItemStack[]::new));
        });
        TieredIronChests.logRegistered("(Fabric)");
    }

    private static void registerTier(ChestTier tier) {
        ResourceLocation id = TieredIronChests.id(tier.blockName());
        TieredChestBlock block = Registry.register(BuiltInRegistries.BLOCK, id, TieredChestContent.createBlock(tier));
        BlockItem blockItem = Registry.register(BuiltInRegistries.ITEM, id, TieredChestContent.createBlockItem(tier, block));
        BlockEntityType<TieredChestBlockEntity> blockEntityType =
                Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id, TieredChestContent.createBlockEntityType(tier, block));
        MenuType<TieredChestMenu> menuType = Registry.register(BuiltInRegistries.MENU, id, TieredChestContent.createMenuType(tier));
        ChestUpgradeItem kit = Registry.register(BuiltInRegistries.ITEM, TieredIronChests.id(tier.kitName()), TieredChestContent.createKit(tier));
        TieredChestContent.registerTier(tier, () -> block, () -> blockItem, () -> blockEntityType, () -> menuType, () -> kit);
    }
}
