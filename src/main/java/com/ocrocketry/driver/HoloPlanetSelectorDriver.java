package com.ocrocketry.driver;

import java.lang.reflect.Field;

import org.apache.commons.lang3.reflect.FieldUtils;

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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import zmaster587.advancedRocketry.entity.EntityUIPlanet;
import zmaster587.advancedRocketry.tile.station.TileHolographicPlanetSelector;

public class HoloPlanetSelectorDriver extends DriverSidedTileEntity {
    private static final Field selectedPlanetField = FieldUtils.getField(TileHolographicPlanetSelector.class, "selectedPlanet", true);

    @Override
    public Class<?> getTileEntityClass() {
        return TileHolographicPlanetSelector.class;
    }

    @Override
    public ManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing facing) {
        return new Environment((TileHolographicPlanetSelector) world.getTileEntity(pos), pos);
    }

    public static class Environment extends AbstractManagedEnvironment implements NamedBlock {
        private final TileHolographicPlanetSelector te;
        private int prevPlanet = -2147483647;

        public Environment(final TileHolographicPlanetSelector selector, final BlockPos pos) {
            setNode(Network.newNode(this, Visibility.Network).withComponent(preferredName(), Visibility.Network).create());
            this.te = selector;
        }

        @Callback(doc = "function():number -- current planet id. Will return null if no planet found or ship in warp")
        public Object[] currentPlanet(Context context, Arguments args) throws Exception {
            int dimId = getCurrentPlanetId();
            return new Object[]{(dimId != -2147483647 ? dimId : null)};
        }

        @Callback(doc = "function(planetId:number) -- select planet with id")
        public Object[] selectPlanet(Context context, Arguments args) throws Exception {
            te.selectSystem(args.checkInteger(0));
            return new Object[]{true};
        }

        private int getCurrentPlanetId() {
            try {
                EntityUIPlanet planet = (EntityUIPlanet) selectedPlanetField.get(te);
                if (planet == null)
                    return -2147483647;
                return planet.getPlanetID();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return -2147483647;
            }
        }

        @Override
        public void update() {
            int newId = getCurrentPlanetId();
            if (prevPlanet != newId) {
                node().sendToReachable("computer.signal", "planetSelected", newId);
                prevPlanet = newId;
            }
        }

        @Override
        public boolean canUpdate() {
            return true;
        }

        @Override
        public void load(NBTTagCompound nbt) {
            super.load(nbt);
            if (nbt.hasKey("prevPlanet"))
                prevPlanet = nbt.getInteger("prevPlanet");
        }

        @Override
        public void save(NBTTagCompound nbt) {
            super.save(nbt);
            if (prevPlanet != -2147483647)
                nbt.setInteger("prevPlanet", prevPlanet);
        }

        @Override
        public String preferredName() {
            return "planetselector";
        }

        @Override
        public int priority() {
            return 1;
        }
    }
}
