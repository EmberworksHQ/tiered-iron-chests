package com.automods.tieredironchests;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A single-block chest of one {@link ChestTier}. Behaviour mirrors vanilla {@code ChestBlock} line for line
 * (facing + waterlogged, 14-high hitbox, block-above / sitting-cat obstruction, open-chest statistic, piglin anger,
 * comparator output, contents dropped on removal, lid re-check tick) with double-chest pairing removed.
 *
 * <p><b>Why no double chests:</b> this class extends {@link BaseEntityBlock} rather than {@code ChestBlock} /
 * {@code AbstractChestBlock}, has no {@code ChestType} block-state property and never consults neighbours in
 * {@link #getStateForPlacement} or {@link #updateShape}. Vanilla's pairing lives entirely in {@code ChestBlock}
 * ({@code DoubleBlockCombiner}, {@code TYPE} property, {@code candidatePartnerFacing}), none of which is inherited,
 * so two tiered chests placed side by side stay independent single chests.
 */
public class TieredChestBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    /** Same hitbox as a single vanilla chest. */
    private static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 14.0, 15.0);

    private final ChestTier tier;

    public TieredChestBlock(ChestTier tier, BlockBehaviour.Properties properties) {
        super(properties);
        this.tier = tier;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(WATERLOGGED, Boolean.FALSE));
    }

    public ChestTier tier() {
        return tier;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluid = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(WATERLOGGED, fluid.getType() == Fluids.WATER);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level,
                                  BlockPos currentPos, BlockPos facingPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }

    // ---- block entity ----

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TieredChestBlockEntity(tier, pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide
                ? createTickerHelper(type, TieredChestContent.blockEntityType(tier), TieredChestBlockEntity::lidAnimateTick)
                : null;
    }

    /** Scheduled by the openers counter (vanilla: 5 ticks after every open/close) to re-count viewers. */
    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof TieredChestBlockEntity chest) {
            chest.recheckOpen();
        }
    }

    /** 1.20.1: the custom name of the placed item stack is applied here (1.21 does it through data components). */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (stack.hasCustomHoverName() && level.getBlockEntity(pos) instanceof TieredChestBlockEntity chest) {
            chest.setCustomName(stack.getHoverName());
        }
    }

    // ---- interaction ----

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        MenuProvider provider = this.getMenuProvider(state, level, pos);
        if (provider != null) {
            player.openMenu(provider);
            player.awardStat(Stats.CUSTOM.get(Stats.OPEN_CHEST));
            PiglinAi.angerNearbyPiglins(player, true);
        }
        return InteractionResult.CONSUME;
    }

    /** Null (cannot open) when a solid block or a sitting cat is on top - identical to vanilla's obstruction rule. */
    @Override
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof TieredChestBlockEntity chest && !isChestBlockedAt(level, pos)
                ? chest
                : null;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof Container container) {
                Containers.dropContents(level, pos, container);
                level.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    // ---- redstone ----

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    /** Vanilla parity: an obstructed chest reads as empty (its container resolves to nothing), otherwise fill level. */
    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof TieredChestBlockEntity chest && !isChestBlockedAt(level, pos)
                ? AbstractContainerMenu.getRedstoneSignalFromContainer(chest)
                : 0;
    }

    // ---- obstruction (copied from ChestBlock) ----

    public static boolean isChestBlockedAt(LevelAccessor level, BlockPos pos) {
        return isBlockedByBlockAbove(level, pos) || isCatSittingOnChest(level, pos);
    }

    private static boolean isBlockedByBlockAbove(BlockGetter level, BlockPos pos) {
        BlockPos above = pos.above();
        return level.getBlockState(above).isRedstoneConductor(level, above);
    }

    private static boolean isCatSittingOnChest(LevelAccessor level, BlockPos pos) {
        AABB above = new AABB(pos.getX(), pos.getY() + 1, pos.getZ(), pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1);
        for (Cat cat : level.getEntitiesOfClass(Cat.class, above)) {
            if (cat.isInSittingPose()) {
                return true;
            }
        }
        return false;
    }
}
