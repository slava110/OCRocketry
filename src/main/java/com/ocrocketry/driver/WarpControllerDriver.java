package com.ocrocketry.driver;

import com.ocrocketry.util.AdvRocketryUtils;
import li.cil.oc.api.Network;
import li.cil.oc.api.driver.NamedBlock;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.AbstractManagedEnvironment;
import li.cil.oc.api.prefab.DriverSidedTileEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import zmaster587.advancedRocketry.api.stations.ISpaceObject;
import zmaster587.advancedRocketry.dimension.DimensionManager;
import zmaster587.advancedRocketry.stations.SpaceObjectManager;
import zmaster587.advancedRocketry.stations.SpaceStationObject;
import zmaster587.advancedRocketry.tile.multiblock.TileWarpCore;
import zmaster587.advancedRocketry.tile.station.TileWarpShipMonitor;
import zmaster587.libVulpes.util.HashedBlockPosition;

import java.util.Iterator;

public class WarpControllerDriver extends DriverSidedTileEntity {

    @Override
    public Class<?> getTileEntityClass() {
        return TileWarpShipMonitor.class;
    }

    @Override
    public ManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing facing) {
        return new Environment((TileWarpShipMonitor) world.getTileEntity(pos), pos);
    }

    public static class Environment extends AbstractManagedEnvironment implements NamedBlock {
        private final TileWarpShipMonitor te;
        private final SpaceStationObject station;
        private boolean inWarp = false;

        public Environment(final TileWarpShipMonitor monitor, final BlockPos pos) {
            setNode(Network.newNode(this, Visibility.Network).withComponent(preferredName(), Visibility.Network).create());
            this.te = monitor;
            ISpaceObject obj = SpaceObjectManager.getSpaceManager().getSpaceStationFromBlockCoords(pos);
            if(obj instanceof SpaceStationObject) {
                this.station = (SpaceStationObject) obj;
            } else {
                this.station = null;
            }
        }

        @Callback(doc = "function():number -- get warp destination")
        public Object[] getDestination(Context context, Arguments args) throws Exception {
            if(station != null) {
                return new Object[]{station.getDestOrbitingBody()};
            } else {
                return new Object[]{null, "not_on_station"};
            }
        }

        @Callback(doc = "function(dimId:number) -- set warp destination")
        public Object[] setDestination(Context context, Arguments args) throws Exception {
            if(station != null) {
                int destDim = args.checkInteger(0);
                if(station.getOrbitingPlanetId() != destDim && station.isPlanetKnown(DimensionManager.getInstance().getDimensionProperties(destDim))) {
                    station.setDestOrbitingBody(destDim);
                }
                return new Object[]{true};
            } else {
                return new Object[]{false, "not_on_station"};
            }
        }

        @Callback(doc = "function():number -- get travel cost to destination")
        public Object[] getTravelCost(Context context, Arguments args) throws Exception {
            if(station != null) {
                return new Object[]{AdvRocketryUtils.getTravelCost(station)};
            } else {
                return new Object[]{null, "not_on_station"};
            }
        }

        @Callback(doc = "function() -- begin warp")
        public Object[] warp(Context context, Arguments args) throws Exception {
            if(station != null) {
                if(!station.hasUsableWarpCore()) {
                    return new Object[]{false, "no_usable_warpcores"};
                } else if(station.useFuel(AdvRocketryUtils.getTravelCost(station)) == 0) {
                    return new Object[]{false, "no_fuel"};
                } else if(!AdvRocketryUtils.meetsArtifactReq(te, DimensionManager.getInstance().getDimensionProperties(station.getDestOrbitingBody()))) {
                    return new Object[]{false, "no_required_artifacts"};
                } else {
                    SpaceObjectManager.getSpaceManager().moveStationToBody(station, station.getDestOrbitingBody(), Math.max(Math.min(AdvRocketryUtils.getTravelCost(station) * 5, 5000), 0));
                    Iterator<?> iter = station.getWarpCoreLocations().iterator();

                    while(iter.hasNext()) {
                        HashedBlockPosition vec = (HashedBlockPosition)iter.next();
                        TileEntity tile = te.getWorld().getTileEntity(vec.getBlockPos());
                        if (tile != null && tile instanceof TileWarpCore) {
                            ((TileWarpCore)tile).onInventoryUpdated();
                        }
                    }
                    return new Object[]{true};
                }
            } else {
                return new Object[]{false, "not_on_station"};
            }
        }

        @Callback(doc = "function():boolean -- is in warp")
        public Object[] isInWarp(Context context, Arguments args) throws Exception {
            if(station != null) {
                return new Object[]{inWarp};
            } else {
                return new Object[]{null, "not_on_station"};
            }
        }

        @Callback(doc = "function():number -- current planet id")
        public Object[] currentPlanet(Context context, Arguments args) throws Exception {
            if(station != null) {
                return new Object[]{station.getOrbitingPlanetId()};
            } else {
                return new Object[]{null, "not_on_station"};
            }
        }

        @Override
        public void update() {
            if (!inWarp && station.getOrbitingPlanetId() == -2147483648) {
                inWarp = true;
                node().sendToReachable("computer.signal", "warpStarted", station.getDestOrbitingBody());
            } else if(inWarp && station.getOrbitingPlanetId() != -2147483648) {
                inWarp = false;
                node().sendToReachable("computer.signal", "warpFinished", station.getOrbitingPlanetId());
            }
        }

        @Override
        public void load(NBTTagCompound nbt) {
            super.load(nbt);
            inWarp = nbt.getBoolean("inWarp");
        }

        @Override
        public void save(NBTTagCompound nbt) {
            super.save(nbt);
            nbt.setBoolean("inWarp", inWarp);
        }

        @Override
        public boolean canUpdate() {
            return station != null;
        }

        @Override
        public String preferredName() {
            return "warpcontroller";
        }

        @Override
        public int priority() {
            return 1;
        }
    }
}
