package com.automods.tieredironchests.client;

import com.automods.tieredironchests.ChestTier;
import com.automods.tieredironchests.TieredChestBlock;
import com.automods.tieredironchests.TieredChestBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumMap;
import java.util.Map;

/**
 * Renders every tier with the vanilla single-chest model ({@link ModelLayers#CHEST}: bottom / lid / lock) and a
 * per-tier texture on the vanilla chest atlas. Lid animation is the vanilla ease-out of
 * {@link TieredChestBlockEntity#getOpenNess}. Client-only: constructed from the loader client entrypoints only.
 */
public class TieredChestRenderer implements BlockEntityRenderer<TieredChestBlockEntity> {
    private static final Map<ChestTier, Material> MATERIALS = new EnumMap<>(ChestTier.class);

    static {
        for (ChestTier tier : ChestTier.values()) {
            MATERIALS.put(tier, new Material(Sheets.CHEST_SHEET, tier.entityTexture()));
        }
    }

    private final ModelPart lid;
    private final ModelPart bottom;
    private final ModelPart lock;

    public TieredChestRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart root = context.bakeLayer(ModelLayers.CHEST);
        this.bottom = root.getChild("bottom");
        this.lid = root.getChild("lid");
        this.lock = root.getChild("lock");
    }

    @Override
    public void render(TieredChestBlockEntity chest, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        BlockState state = chest.getBlockState();
        Direction facing = state.hasProperty(TieredChestBlock.FACING) ? state.getValue(TieredChestBlock.FACING) : Direction.SOUTH;

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        poseStack.translate(-0.5F, -0.5F, -0.5F);

        float openness = 1.0F - chest.getOpenNess(partialTick);
        openness = 1.0F - openness * openness * openness;
        VertexConsumer consumer = MATERIALS.get(chest.tier()).buffer(buffer, RenderType::entityCutout);

        lid.xRot = -(openness * (float) (Math.PI / 2));
        lock.xRot = lid.xRot;
        lid.render(poseStack, consumer, packedLight, packedOverlay);
        lock.render(poseStack, consumer, packedLight, packedOverlay);
        bottom.render(poseStack, consumer, packedLight, packedOverlay);

        poseStack.popPose();
    }
}
