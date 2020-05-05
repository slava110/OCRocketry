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
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.reflect.FieldUtils;
import zmaster587.advancedRocketry.api.ARConfiguration;
import zmaster587.advancedRocketry.api.stations.ISpaceObject;
import zmaster587.advancedRocketry.stations.SpaceObjectManager;
import zmaster587.advancedRocketry.stations.SpaceStationObject;
import zmaster587.advancedRocketry.tile.station.TilePlanetaryHologram;
import zmaster587.advancedRocketry.tile.station.TileStationGravityController;

import java.lang.reflect.Field;

public class GravityControllerDriver extends DriverSidedTileEntity {
    private static final Field targetGravityField = FieldUtils.getField(TileStationGravityController.class, "gravity", true);

    @Override
    public Class<?> getTileEntityClass() {
        return TileStationGravityController.class;
    }

    @Override
    public ManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing facing) {
        return new Environment((TileStationGravityController) world.getTileEntity(pos), pos);
    }

    public static class Environment extends AbstractManagedEnvironment implements NamedBlock {
        private final TileStationGravityController te;
        private final SpaceStationObject station;

        public Environment(final TileStationGravityController tile, final BlockPos pos) {
            setNode(Network.newNode(this, Visibility.Network).withComponent(preferredName(), Visibility.Network).create());
            this.te = tile;
            ISpaceObject obj = SpaceObjectManager.getSpaceManager().getSpaceStationFromBlockCoords(pos);
            if(obj instanceof SpaceStationObject) {
                this.station = (SpaceStationObject) obj;
            } else {
                this.station = null;
            }
        }

        @Callback(doc = "function():number -- get current gravity multipiler")
        public Object[] currentGravity(Context context, Arguments args) throws Exception {
            if(station != null) {
                return new Object[]{Math.round(station.getProperties().getGravitationalMultiplier() * 100)};
            } else {
                return new Object[]{null, "not_on_station"};
            }
        }

        @Callback(doc = "function(targetGravity:number) -- set target gravity")
        public Object[] setTargetGravity(Context context, Arguments args) throws Exception {
            if(station != null) {
                int grav = args.checkInteger(0);
                if(!ORUtils.isInRangeInc(grav, (ARConfiguration.getCurrentConfig().allowZeroGSpacestations ? 11 : 0), 100)) {
                    return new Object[] {null, "parameter_not_in_range"};
                }
                targetGravityField.set(te, grav);
                te.markDirty();
                return new Object[]{true};
            } else {
                return new Object[]{false, "not_on_station"};
            }
        }

        @Override
        public String preferredName() {
            return "gravitycontroller";
        }

        @Override
        public int priority() {
            return 1;
        }
    }
}