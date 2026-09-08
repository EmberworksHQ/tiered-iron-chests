package com.automods.tieredironchests.mixin;

import com.automods.tieredironchests.TieredChestBlock;
import com.automods.tieredironchests.TieredChestBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.CatSitOnBlockGoal;
import net.minecraft.world.level.LevelReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Vanilla cats only pick {@code Blocks.CHEST} as a sitting spot (hard-coded block check). This lets them treat a
 * tiered chest exactly like a vanilla one: a valid target while nobody has it open. Everything else (the cat
 * blocking the lid while it sits) already works through {@link TieredChestBlock#isChestBlockedAt}.
 */
@Mixin(CatSitOnBlockGoal.class)
public abstract class CatSitOnBlockGoalMixin {
    @Inject(method = "isValidTarget", at = @At("HEAD"), cancellable = true)
    private void tieredironchests$sitOnTieredChests(LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!level.isEmptyBlock(pos.above())) {
            return; // vanilla rejects it anyway
        }
        if (level.getBlockState(pos).getBlock() instanceof TieredChestBlock
                && level.getBlockEntity(pos) instanceof TieredChestBlockEntity chest) {
            cir.setReturnValue(chest.getOpenCount() < 1);
        }
    }
}
