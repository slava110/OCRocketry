package com.ocrocketry.driver;

import javax.annotation.Nonnull;

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
import zmaster587.advancedRocketry.api.IAtmosphere;
import zmaster587.advancedRocketry.atmosphere.AtmosphereHandler;
import zmaster587.advancedRocketry.atmosphere.AtmosphereType;
import zmaster587.advancedRocketry.tile.atmosphere.TileAtmosphereDetector;

public class AtmosphereDetectorDriver extends DriverSidedTileEntity {

    @Override
    public Class<?> getTileEntityClass() {
        return TileAtmosphereDetector.class;
    }

    @Override
    public ManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing facing) {
        return new Environment(pos, world.provider.getDimension());
    }

    public static class Environment extends AbstractManagedEnvironment implements NamedBlock {
        private final BlockPos pos;
        private final AtmosphereHandler handler;

        public Environment(final BlockPos pos, final int dimId) {
            setNode(Network.newNode(this, Visibility.Network)
                    .withComponent(preferredName(), Visibility.Network)
                    .create()
            );
            this.pos = pos;
            this.handler = AtmosphereHandler.getOxygenHandler(dimId);
        }

        @Callback(doc = "function(side:number):string -- Get atmosphere type")
        public Object[] getAtmosphereType(Context ctx, Arguments args) throws Exception {
            return new Object[]{getAtmosphere(args.checkInteger(0)).getUnlocalizedName()};
        }

        @Callback(doc = "function(side:number):boolean -- Check if atmosphere is breathable")
        public Object[] isBreathable(Context ctx, Arguments args) throws Exception {
            return new Object[]{getAtmosphere(args.checkInteger(0)).isBreathable()};
        }

        @Callback(doc = "function(side:number):boolean -- Check if atmosphere allows combustion")
        public Object[] allowsCombustion(Context ctx, Arguments args) throws Exception {
            return new Object[]{getAtmosphere(args.checkInteger(0)).allowsCombustion()};
        }

        @Override
        public String preferredName() {
            return "atmospheredetector";
        }

        @Override
        public int priority() {
            return 1;
        }

        @Nonnull
        private IAtmosphere getAtmosphere(int side) {
            return handler != null ? handler.getAtmosphereType(pos.offset(getSide(side))) : AtmosphereType.AIR;
        }

        @Nonnull
        private EnumFacing getSide(int index) throws IllegalArgumentException {
            if (index < 0 || index > 5)
                throw new IllegalArgumentException("invalid side");
            return EnumFacing.getFront(index);
        }
    }
}
