package com.martmists.ynet.network;

import com.martmists.ynet.YNetMod;
import com.martmists.ynet.api.BaseProvider;
import com.martmists.ynet.blockentities.ControllerBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

import java.util.*;

public class Network {
    private BlockPos controller;
    public Set<BlockPos> cables;
    public Set<BlockPos> connectors;

    public void reloadAllNodes(BlockView world) {
        cables = new HashSet<>();
        connectors = new HashSet<>();
        ControllerBlockEntity be = ((ControllerBlockEntity)world.getBlockEntity(controller));
        getConnectedBlocks(be.getWorld(), controller, cables, connectors);
    }

    public static void getConnectedBlocks(BlockView world, BlockPos origin, Set<BlockPos> cables, Set<BlockPos> connectors) {
        getConnectedBlocks(world, origin, new HashSet<>(), cables, connectors);
    }

    public static void getConnectedBlocks(BlockView world, BlockPos origin, Set<BlockPos> exclude, Set<BlockPos> cables, Set<BlockPos> connectors) {
        ArrayDeque<BlockPos> toSearch = new ArrayDeque<>();
        toSearch.push(origin);
        exclude.add(origin);
        // Search origin first
        Block bo = world.getBlockState(origin).getBlock();
        if (bo == YNetMod.CABLE) {
            cables.add(origin);
        } else if (bo == YNetMod.CONNECTOR) {
            connectors.add(origin);
        }

        // Search connected
        while (!toSearch.isEmpty()){
            BlockPos p = toSearch.removeFirst();
            for (BlockPos p2 : Arrays.asList(p.up(), p.down(), p.north(), p.south(), p.east(), p.west())) {
                if (exclude.contains(p2)) {
                    continue;
                }
                exclude.add(p2);
                Block b = world.getBlockState(p2).getBlock();
                if (b == YNetMod.CABLE) {
                    toSearch.add(p2);
                    cables.add(p2);
                } else if (b == YNetMod.CONNECTOR) {
                    toSearch.add(p2);
                    connectors.add(p2);
                }
            }
        }
    }

    public static void getConnectedControllers(BlockView world, BlockPos origin, Set<BlockPos> controllers) {
        ArrayDeque<BlockPos> toSearch = new ArrayDeque<>();
        toSearch.push(origin);
        List<BlockPos> searched = new ArrayList<>();
        while (!toSearch.isEmpty()){
            BlockPos p = toSearch.removeFirst();
            for (BlockPos p2 : Arrays.asList(p.up(), p.down(), p.north(), p.south(), p.east(), p.west())) {
                if (searched.contains(p2)) {
                    continue;
                }
                searched.add(p2);
                Block b = world.getBlockState(p2).getBlock();
                if (b == YNetMod.CABLE || b == YNetMod.CONNECTOR) {
                    toSearch.add(p2);
                } else if (b == YNetMod.CONTROLLER) {
                    controllers.add(p2);
                }
            }
        }
    }

    public void setController(BlockPos pos) {
        controller = pos;
    }

    public Set<BlockPos> getProviders(BlockView world) {
        return getProviders(world, BaseProvider.class);
    }

    public Set<BlockPos> getProviders(BlockView world, Class<BaseProvider> type) {
        Set<BlockPos> providers = new HashSet<>();
        connectors.forEach((c) -> {
            for (BlockPos p : Arrays.asList(c.up(), c.down(), c.north(), c.south(), c.east(), c.west())) {
                Block b = world.getBlockState(p).getBlock();
                Set<Class<? extends BaseProvider>> providerTypes = new HashSet<>();
                findProviderTypes(b.getClass(), providerTypes);
                if (providerTypes.contains(type) || (type == BaseProvider.class && b instanceof BaseProvider)) {
                    providers.add(p);
                }
            }
        });
        return providers;
    }


    // Ugly hack by Pyrofab
    private static Map<Class<?>, Set<Class<? extends BaseProvider>>> tMap = new HashMap<>();
    private static void findProviderTypes(Class<?> cls, Set<Class<? extends BaseProvider>> ret) {
        tMap.putIfAbsent(cls, ret);
        if (cls != Object.class && cls != null) {
            if (BaseProvider.class.isAssignableFrom(cls)) {
                ret.add((Class<? extends BaseProvider>) cls);
            }
            findProviderTypes(cls.getSuperclass(), ret);
            for (Class<?> itf : cls.getInterfaces()) {
                findProviderTypes(itf, ret);
            }
        }
    }
}
