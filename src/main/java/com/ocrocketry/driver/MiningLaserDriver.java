package com.ocrocketry.driver;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

import com.ocrocketry.OCRocketry;
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
import zmaster587.advancedRocketry.tile.multiblock.orbitallaserdrill.TileOrbitalLaserDrill;

public class MiningLaserDriver extends DriverSidedTileEntity {
    private static MethodHandle resetSpiralMethod;

    static {
        try {
            Method resetSpiralMethod1 = TileOrbitalLaserDrill.class.getDeclaredMethod("resetSpiral");
            resetSpiralMethod1.setAccessible(true);
            resetSpiralMethod = MethodHandles.lookup().unreflect(resetSpiralMethod1);
        } catch (IllegalAccessException | NoSuchMethodException e) {
            OCRocketry.logger.error("Error in resetSpiral search", e);
        }
    }

    @Override
    public Class<?> getTileEntityClass() {
        return TileOrbitalLaserDrill.class;
    }

    @Override
    public ManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing facing) {
        return new Environment((TileOrbitalLaserDrill) world.getTileEntity(pos), pos);
    }

    public static class Environment extends AbstractManagedEnvironment implements NamedBlock {
        private final TileOrbitalLaserDrill te;

        public Environment(final TileOrbitalLaserDrill laser, final BlockPos pos) {
            setNode(Network.newNode(this, Visibility.Network).withComponent(preferredName(), Visibility.Network).create());
            this.te = laser;
        }

        @Callback(doc = "function():number,number -- Get mining laser coordinates", getter = true)
        public Object[] getCoords(Context ctx, Arguments args) throws Exception {
            return new Object[]{te.laserX, te.laserZ};
        }

        @Callback(doc = "function(x:number,z:number) -- Set mining laser coordinates", setter = true)
        public Object[] setCoords(Context ctx, Arguments args) throws Exception {
            te.laserX = args.checkInteger(0);
            te.laserZ = args.checkInteger(1);
            te.setFinished(false);
            if (te.getMode() == TileOrbitalLaserDrill.MODE.SPIRAL) {
                try {
                    resetSpiralMethod.invoke(te);
                } catch (Throwable throwable) {
                    OCRocketry.logger.error("Error in setCoords resetSpiral", throwable);
                }
            }
            te.markDirty();
            return null;
        }

        @Callback(doc = "function():boolean -- Check if mining laser is currently working")
        public Object[] isRunning(Context ctx, Arguments args) throws Exception {
            return new Object[]{te.isRunning()};
        }

        @Callback(doc = "function():boolean -- Check if mining laser finished mining")
        public Object[] isFinished(Context ctx, Arguments args) throws Exception {
            return new Object[]{te.isFinished()};
        }

        @Callback(doc = "function():string -- Get mining laser mode")
        public Object[] getMode(Context ctx, Arguments args) throws Exception {
            return new Object[]{te.getMode().name().toLowerCase()};
        }

        @Callback(doc = "function(mode:string) -- Set mining laser mode. Available modes: single, line_x, line_z, spiral")
        public Object[] setMode(Context ctx, Arguments args) throws Exception {
            String modeName = args.checkString(0).toUpperCase();
            if (!ORUtils.enumContains(TileOrbitalLaserDrill.MODE.class, modeName))
                return new Object[]{false, "mode_not_found"};
            te.setMode(TileOrbitalLaserDrill.MODE.valueOf(modeName));
            te.markDirty();
            return new Object[]{true};
        }

        /*@Callback(doc = "function() -- Start mining")
        public Object[] start(Context ctx, Arguments args) throws Exception {
            te.setRunning(true);
            te.markDirty();
            return null;
        }

        @Callback(doc = "function() -- Stop mining")
        public Object[] stop(Context ctx, Arguments args) throws Exception {
            te.setRunning(false);
            te.markDirty();
            return null;
        }*/

        @Override
        public String preferredName() {
            return "mininglaser";
        }

        @Override
        public int priority() {
            return 1;
        }
    }
}
