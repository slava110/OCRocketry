package com.ocrocketry.driver;

import static com.ocrocketry.util.ORUtils.isInRangeInc;

import li.cil.oc.api.Network;
import li.cil.oc.api.driver.NamedBlock;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.AbstractManagedEnvironment;
import li.cil.oc.api.prefab.DriverSidedTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import zmaster587.advancedRocketry.api.stations.ISpaceObject;
import zmaster587.advancedRocketry.stations.SpaceObjectManager;
import zmaster587.advancedRocketry.stations.SpaceStationObject;
import zmaster587.advancedRocketry.tile.station.TileStationOrientationController;

public class OrientationControllerDriver extends DriverSidedTileEntity {

    @Override
    public Class<?> getTileEntityClass() {
        return TileStationOrientationController.class;
    }

    @Override
    public ManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing facing) {
        return new Environment((TileStationOrientationController) world.getTileEntity(pos), pos);
    }

    public static class Environment extends AbstractManagedEnvironment implements NamedBlock {
        private final TileStationOrientationController te;
        private final SpaceStationObject station;

        public Environment(final TileStationOrientationController tile, final BlockPos pos) {
            setNode(Network.newNode(this, Visibility.Network).withComponent(preferredName(), Visibility.Network).create());
            this.te = tile;
            ISpaceObject obj = SpaceObjectManager.getSpaceManager().getSpaceStationFromBlockCoords(pos);
            if (obj instanceof SpaceStationObject) {
                this.station = (SpaceStationObject) obj;
            } else {
                this.station = null;
            }
        }

        @Callback(doc = "function():number, number, number -- get current velocity")
        public Object[] currentVelocity(Context context, Arguments args) throws Exception {
            if (station != null) {
                return new Object[]{getVelocity(0), getVelocity(1), getVelocity(2)};
            } else {
                return new Object[]{null, "not_on_station"};
            }
        }

        @Callback(doc = "function():number, number, number -- get current rotation")
        public Object[] currentRotation(Context context, Arguments args) throws Exception {
            if (station != null) {
                return new Object[]{station.getRotation(EnumFacing.EAST), station.getRotation(EnumFacing.UP), station.getRotation(EnumFacing.NORTH)};
            } else {
                return new Object[]{null, "not_on_station"};
            }
        }

        private double getVelocity(int id) {
            return te.getProgress(id) - 60;
        }

        @Callback(doc = "function(targetVelocityX:number, targetVelocityY:number, targetVelocityZ:number) -- set target velocity")
        public Object[] setTargetVelocity(Context context, Arguments args) throws Exception {
            if (station != null) {
                if (!isInRangeInc(args.checkInteger(0), -60, 60) || !isInRangeInc(args.checkInteger(1), -60, 60) || !isInRangeInc(args.checkInteger(2), -60, 60)) {
                    return new Object[]{null, "parameter_not_in_range"};
                }
                station.targetRotationsPerHour[0] = args.checkInteger(0);
                station.targetRotationsPerHour[1] = args.checkInteger(1);
                station.targetRotationsPerHour[2] = args.checkInteger(2);
                return new Object[]{true};
            } else {
                return new Object[]{false, "not_on_station"};
            }
        }

        @Override
        public String preferredName() {
            return "orientationcontroller";
        }

        @Override
        public int priority() {
            return 1;
        }
    }
}