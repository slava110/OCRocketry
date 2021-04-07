package com.ocrocketry.driver;

import com.ocrocketry.util.ORUtils;

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
import zmaster587.advancedRocketry.tile.station.TileStationAltitudeController;

public class AltitudeControllerDriver extends DriverSidedTileEntity {

    @Override
    public Class<?> getTileEntityClass() {
        return TileStationAltitudeController.class;
    }

    @Override
    public ManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing facing) {
        return new Environment((TileStationAltitudeController) world.getTileEntity(pos), pos);
    }

    public static class Environment extends AbstractManagedEnvironment implements NamedBlock {
        private final TileStationAltitudeController te;
        private final SpaceStationObject station;

        public Environment(final TileStationAltitudeController tile, final BlockPos pos) {
            setNode(Network.newNode(this, Visibility.Network).withComponent(preferredName(), Visibility.Network).create());
            this.te = tile;
            ISpaceObject obj = SpaceObjectManager.getSpaceManager().getSpaceStationFromBlockCoords(pos);
            if (obj instanceof SpaceStationObject) {
                this.station = (SpaceStationObject) obj;
            } else {
                this.station = null;
            }
        }

        @Callback(doc = "function():number -- get current altitude")
        public Object[] currentAltitude(Context context, Arguments args) throws Exception {
            if (station != null) {
                return new Object[]{station.getOrbitalDistance() * 200 + 100};
            } else {
                return new Object[]{null, "not_on_station"};
            }
        }

        @Callback(doc = "function(targetGravity:number) -- set target altitude")
        public Object[] setTargetAltitude(Context context, Arguments args) throws Exception {
            if (station != null) {
                int alt = (args.checkInteger(0) - 100) / 200;
                if (!ORUtils.isInRangeInc(alt, 4, 190)) {
                    return new Object[]{null, "parameter_not_in_range"};
                }
                station.targetOrbitalDistance = alt;
                return new Object[]{true};
            } else {
                return new Object[]{false, "not_on_station"};
            }
        }

        @Override
        public String preferredName() {
            return "altitudecontroller";
        }

        @Override
        public int priority() {
            return 1;
        }
    }
}