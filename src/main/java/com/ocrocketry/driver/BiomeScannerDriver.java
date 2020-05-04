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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import zmaster587.advancedRocketry.api.stations.ISpaceObject;
import zmaster587.advancedRocketry.stations.SpaceObjectManager;
import zmaster587.advancedRocketry.stations.SpaceStationObject;
import zmaster587.advancedRocketry.tile.multiblock.TileBiomeScanner;

public class BiomeScannerDriver extends DriverSidedTileEntity {

    @Override
    public Class<?> getTileEntityClass() {
        return TileBiomeScanner.class;
    }

    @Override
    public ManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing facing) {
        return new Environment((TileBiomeScanner) world.getTileEntity(pos), pos);
    }

    public static class Environment extends AbstractManagedEnvironment implements NamedBlock {
        private final TileBiomeScanner te;
        private final SpaceStationObject station;

        public Environment(final TileBiomeScanner scanner, final BlockPos pos) {
            setNode(Network.newNode(this, Visibility.Network).withComponent(preferredName(), Visibility.Network).create());
            this.te = scanner;
            ISpaceObject obj = SpaceObjectManager.getSpaceManager().getSpaceStationFromBlockCoords(pos);
            if(obj instanceof SpaceStationObject) {
                this.station = (SpaceStationObject) obj;
            } else {
                this.station = null;
            }
        }

        @Callback(doc = "function(withModId:boolean):table -- scan planet and get list of biomes")
        public Object[] scan(Context context, Arguments args) throws Exception {
            if(station != null) {
                boolean withModId = args.optBoolean(0, false);
                return AdvRocketryUtils.scanPlanet(te, station).stream()
                        .map(biome -> (withModId ? biome.getRegistryName().toString() : biome.getRegistryName().getResourcePath()))
                        .toArray();
            } else {
                return new Object[]{null, "not_on_station"};
            }
        }

        @Override
        public String preferredName() {
            return "biomescanner";
        }

        @Override
        public int priority() {
            return 1;
        }
    }
}
