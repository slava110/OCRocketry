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
import zmaster587.advancedRocketry.api.ARConfiguration;
import zmaster587.advancedRocketry.api.EntityRocketBase;
import zmaster587.advancedRocketry.api.IMission;
import zmaster587.advancedRocketry.api.fuel.FuelRegistry;
import zmaster587.advancedRocketry.tile.infrastructure.TileEntityMoniteringStation;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

public class MonitoringStationDriver extends DriverSidedTileEntity {
    private static final Field linkedRocketField = FieldUtils.getField(TileEntityMoniteringStation.class, "linkedRocket", true);
    private static final Field missionField = FieldUtils.getField(TileEntityMoniteringStation.class, "mission", true);

    @Override
    public Class<?> getTileEntityClass() {
        return TileEntityMoniteringStation.class;
    }

    @Override
    public ManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing facing) {
        return new Environment((TileEntityMoniteringStation) world.getTileEntity(pos), pos);
    }

    public static class Environment extends AbstractManagedEnvironment implements NamedBlock {
        private final TileEntityMoniteringStation te;

        public Environment(final TileEntityMoniteringStation station, final BlockPos pos) {
            setNode(Network.newNode(this, Visibility.Network).withComponent(preferredName(), Visibility.Network).create());
            this.te = station;
        }

        @Callback(doc = "function() -- Start launch cooldown")
        public Object[] prepareLaunch(Context context, Arguments args) throws Exception {
            EntityRocketBase rocket = getRocket();
            if(rocket == null)
                return new Object[]{false, "rocket_not_found"};
            rocket.prepareLaunch();
            return new Object[]{true};
        }

        @Callback(doc = "function() -- Launch rocket without cooldown")
        public Object[] launch(Context context, Arguments args) throws Exception {
            EntityRocketBase rocket = getRocket();
            if(rocket == null)
                return new Object[]{false, "rocket_not_found"};
            rocket.launch();
            return new Object[]{true};
        }

        @Callback(doc = "function() -- Deconstruct rocket")
        public Object[] deconstruct(Context context, Arguments args) throws Exception {
            EntityRocketBase rocket = getRocket();
            if(rocket == null)
                return new Object[]{false, "rocket_not_found"};
            te.unlinkRocket();
            rocket.deconstructRocket();
            return new Object[]{true};
        }

        @Callback(doc = "function():number -- mission progress. Will return null if no mission started")
        public Object[] getMissionProgress(Context context, Arguments args) throws Exception {
            IMission mission = getMission();
            return new Object[]{mission != null ? mission.getProgress(te.getWorld()) : null};
        }

        @Callback(doc = "function():number -- get mission remaining time in seconds. Will return null if no mission started")
        public Object[] getMissionRemainingTime(Context context, Arguments args) throws Exception {
            IMission mission = getMission();
            return new Object[]{mission != null ? mission.getTimeRemainingInSeconds() : null};
        }

        @Callback(doc = "function():number -- get rocket height, null if no rocket linked")
        public Object[] getRocketHeight(Context context, Arguments args) throws Exception {
            EntityRocketBase rocket = getRocket();
            return new Object[]{rocket != null ? getMission() != null ? ARConfiguration.getCurrentConfig().orbit : rocket.posY : null};
        }

        @Callback(doc = "function():number -- get rocket thrust, null if no rocket linked")
        public Object[] getRocketThrust(Context context, Arguments args) throws Exception {
            EntityRocketBase rocket = getRocket();
            return new Object[]{rocket != null ? rocket.stats.getThrust() : null};
        }

        @Callback(doc = "function():number -- get rocket weight, null if no rocket linked")
        public Object[] getRocketWeight(Context context, Arguments args) throws Exception {
            EntityRocketBase rocket = getRocket();
            return new Object[]{rocket != null ? rocket.stats.getWeight() : null};
        }

        @Callback(doc = "function():number -- get rocket drilling power, null if no rocket linked")
        public Object[] getDrillingPower(Context context, Arguments args) throws Exception {
            EntityRocketBase rocket = getRocket();
            return new Object[]{rocket != null ? rocket.stats.getDrillingPower() : null};
        }

        @Callback(doc = "function():number -- get rocket acceleration, null if no rocket linked")
        public Object[] getAcceleration(Context context, Arguments args) throws Exception {
            EntityRocketBase rocket = getRocket();
            return new Object[]{rocket != null ? rocket.stats.getAcceleration() : null};
        }

        @Callback(doc = "function():number -- get current fuel amount, null if no rocket linked")
        public Object[] getFuelAmount(Context context, Arguments args) throws Exception {
            EntityRocketBase rocket = getRocket();
            return new Object[]{rocket != null ? rocket.stats.getFuelAmount(FuelRegistry.FuelType.LIQUID) : null};
        }

        @Callback(doc = "function():number -- get fuel capacity, null if no rocket linked")
        public Object[] getFuelCapacity(Context context, Arguments args) throws Exception {
            EntityRocketBase rocket = getRocket();
            return new Object[]{rocket != null ? rocket.stats.getFuelCapacity(FuelRegistry.FuelType.LIQUID) : null};
        }

        @Callback(doc = "function():number -- get fuel consume rate, null if no rocket linked")
        public Object[] getFuelRate(Context context, Arguments args) throws Exception {
            EntityRocketBase rocket = getRocket();
            return new Object[]{rocket != null ? rocket.stats.getFuelRate(FuelRegistry.FuelType.LIQUID) : null};
        }

        @Callback(doc = "function():boolean -- check if there's seat on the rocket, null if no rocket linked")
        public Object[] hasSeat(Context context, Arguments args) throws Exception {
            EntityRocketBase rocket = getRocket();
            return new Object[]{rocket != null ? rocket.stats.hasSeat() : null};
        }

        @Nullable
        private IMission getMission() throws IllegalAccessException {
            return (IMission) missionField.get(te);
        }

        @Nullable
        private EntityRocketBase getRocket() throws IllegalAccessException {
            return (EntityRocketBase) linkedRocketField.get(te);
        }

        @Override
        public String preferredName() {
            return "monitoringstation";
        }

        @Override
        public int priority() {
            return 1;
        }
    }
}
