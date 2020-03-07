package com.martmists.ynet.blocks;

import com.martmists.ynet.YNetMod;
import com.martmists.ynet.api.BaseProvider;
import com.martmists.ynet.blockentities.ControllerBlockEntity;
import com.martmists.ynet.network.Network;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ConnectingBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;

public class CableBlock extends ConnectingBlock {
    public CableBlock(Settings settings) {
        super(0.1875F, settings);
        setDefaultState(getStateManager().getDefaultState()
                .with(NORTH, false)
                .with(EAST, false)
                .with(SOUTH, false)
                .with(WEST, false)
                .with(UP, false)
                .with(DOWN, false)
        );
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.withConnectionProperties(ctx.getWorld(), ctx.getBlockPos());
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction facing, BlockState neighborState, IWorld world, BlockPos pos, BlockPos neighborPos) {
        if (!state.canPlaceAt(world, pos)) {
            world.getBlockTickScheduler().schedule(pos, this, 1);
            return super.getStateForNeighborUpdate(state, facing, neighborState, world, pos, neighborPos);
        } else {
            Block block = neighborState.getBlock();
            return state.with(FACING_PROPERTIES.get(facing), block == this || block == YNetMod.CONNECTOR || block instanceof BaseProvider);
        }
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        super.onBreak(world, pos, state, player);
        Set<BlockPos> controllers = new HashSet<>();

        // No longer connected, check all neighbors
        Network.getConnectedControllers(world, pos, controllers);

        System.out.println("Controllers: " + controllers);  // Empty?
        for (BlockPos p : controllers){
            ControllerBlockEntity be = (ControllerBlockEntity)world.getBlockEntity(p);
            be.updateNetwork();
        }
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        Set<BlockPos> controllers = new HashSet<>();

        // TODO: Find a better way to do this instead of a BFS through the world
        Network.getConnectedControllers(world, pos, controllers);

        System.out.println("Controllers: " + controllers);
        for (BlockPos p : controllers){
            ControllerBlockEntity be = (ControllerBlockEntity)world.getBlockEntity(p);
            // be.network.cables.add(p);
            Set<BlockPos> known = new HashSet<>();
            known.addAll(be.network.cables);
            known.addAll(be.network.connectors);
            Network.getConnectedBlocks(world, pos, known, be.network.cables, be.network.connectors);
        }
    }

    public BlockState withConnectionProperties(BlockView world, BlockPos pos) {
        Block block = world.getBlockState(pos.down()).getBlock();
        Block block2 = world.getBlockState(pos.up()).getBlock();
        Block block3 = world.getBlockState(pos.north()).getBlock();
        Block block4 = world.getBlockState(pos.east()).getBlock();
        Block block5 = world.getBlockState(pos.south()).getBlock();
        Block block6 = world.getBlockState(pos.west()).getBlock();
        return this.getDefaultState()
                .with(DOWN, block == this || block == YNetMod.CONNECTOR)
                .with(UP, block2 == this || block2 == YNetMod.CONNECTOR)
                .with(NORTH, block3 == this || block3 == YNetMod.CONNECTOR)
                .with(EAST, block4 == this || block4 == YNetMod.CONNECTOR)
                .with(SOUTH, block5 == this || block5 == YNetMod.CONNECTOR)
                .with(WEST, block6 == this || block6 == YNetMod.CONNECTOR);
    }
}
