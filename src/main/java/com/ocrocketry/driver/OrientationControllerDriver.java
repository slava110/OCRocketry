package com.ocrocketry.driver;

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
import org.apache.commons.lang3.reflect.FieldUtils;
import zmaster587.advancedRocketry.api.stations.ISpaceObject;
import zmaster587.advancedRocketry.stations.SpaceObjectManager;
import zmaster587.advancedRocketry.stations.SpaceStationObject;
import zmaster587.advancedRocketry.tile.station.TileStationGravityController;
import zmaster587.advancedRocketry.tile.station.TileStationOrientationControl;

import java.lang.reflect.Field;

public class OrientationControllerDriver extends DriverSidedTileEntity {

    @Override
    public Class<?> getTileEntityClass() {
        return TileStationOrientationControl.class;
    }

    @Override
    public ManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing facing) {
        return new Environment((TileStationOrientationControl) world.getTileEntity(pos), pos);
    }

    public static class Environment extends AbstractManagedEnvironment implements NamedBlock {
        private final TileStationOrientationControl te;
        private final SpaceStationObject station;

        public Environment(final TileStationOrientationControl tile, final BlockPos pos) {
            setNode(Network.newNode(this, Visibility.Network).withComponent(preferredName(), Visibility.Network).create());
            this.te = tile;
            ISpaceObject obj = SpaceObjectManager.getSpaceManager().getSpaceStationFromBlockCoords(pos);
            if(obj instanceof SpaceStationObject) {
                this.station = (SpaceStationObject) obj;
            } else {
                this.station = null;
            }
        }

        @Callback(doc = "function():number -- get current X velocity")
        public Object[] currentVelocityX(Context context, Arguments args) throws Exception {
            if(station != null) {
                return new Object[]{getVelocity(0)};
            } else {
                return new Object[]{null, "not_on_station"};
            }
        }

        @Callback(doc = "function():number -- get current Y velocity")
        public Object[] currentVelocityY(Context context, Arguments args) throws Exception {
            if(station != null) {
                return new Object[]{getVelocity(1)};
            } else {
                return new Object[]{null, "not_on_station"};
            }
        }

        @Callback(doc = "function():number -- get current Z velocity")
        public Object[] currentVelocityZ(Context context, Arguments args) throws Exception {
            if(station != null) {
                return new Object[]{getVelocity(2)};
            } else {
                return new Object[]{null, "not_on_station"};
            }
        }

        private double getVelocity(int id) {
            return te.getProgress(id) - 60;
        }

        @Callback(doc = "function(targetVelocityX:number, targetVelocityY:number, targetVelocityZ:number) -- set target velocity")
        public Object[] setTargetVelocity(Context context, Arguments args) throws Exception {
            if(station != null) {
                te.setProgress(0, args.checkInteger(0) + 60);
                te.setProgress(1, args.checkInteger(1) + 60);
                te.setProgress(2, args.checkInteger(2) + 60);
                te.markDirty();
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