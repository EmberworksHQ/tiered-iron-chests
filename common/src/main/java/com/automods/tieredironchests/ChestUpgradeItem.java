package com.automods.tieredironchests;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Upgrade kit for one target tier. Pure crafting ingredient in v1: kit + previous-tier chest (shapeless) yields the
 * target chest (recipes in {@code data/tieredironchests/recipe/<tier>_chest_from_upgrade.json}). The tooltip spells
 * that out in-game.
 */
public class ChestUpgradeItem extends Item {
    public static final String TOOLTIP_KEY = "item." + TieredIronChests.MOD_ID + ".chest_upgrade.tooltip";

    private final ChestTier target;

    public ChestUpgradeItem(ChestTier target, Item.Properties properties) {
        super(properties);
        this.target = target;
    }

    /** The tier this kit produces. */
    public ChestTier target() {
        return target;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(TOOLTIP_KEY,
                TieredChestContent.previousChestBlock(target).getName(),
                TieredChestContent.block(target).getName()).withStyle(ChatFormatting.GRAY));
    }
}
