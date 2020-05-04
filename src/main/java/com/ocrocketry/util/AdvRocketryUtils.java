package com.ocrocketry.util;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeManager;
import zmaster587.advancedRocketry.api.dimension.IDimensionProperties;
import zmaster587.advancedRocketry.api.stations.ISpaceObject;
import zmaster587.advancedRocketry.dimension.DimensionManager;
import zmaster587.advancedRocketry.dimension.DimensionProperties;
import zmaster587.advancedRocketry.stations.SpaceObjectManager;
import zmaster587.advancedRocketry.stations.SpaceStationObject;
import zmaster587.advancedRocketry.tile.multiblock.TileBiomeScanner;
import zmaster587.advancedRocketry.tile.station.TileWarpShipMonitor;

import java.util.*;

public class AdvRocketryUtils {

    public static List<Biome> scanPlanet(TileBiomeScanner te, SpaceStationObject station){
        boolean suitable = true;

        for(int y = te.getPos().getY() - 4; y > 0; --y) {
            if (!te.getWorld().isAirBlock(new BlockPos(te.getPos().getX(), y, te.getPos().getZ()))) {
                suitable = false;
                break;
            }
        }

        if (suitable && -2147483648 != station.getOrbitingPlanetId()) {
            DimensionProperties properties = DimensionManager.getInstance().getDimensionProperties(station.getOrbitingPlanetId());
            if (properties.isGasGiant() || properties.isStar()) {
                return Collections.emptyList();
            } else {
                List<Biome> biomes = new ArrayList<>();
                int i = 0;
                Iterator<?> itr;
                if (properties.getId() == 0) {
                    itr = Biome.REGISTRY.iterator();

                    while(itr.hasNext()) {
                        Biome biome = (Biome)itr.next();
                        if (biome != null) {
                            biomes.add(biome);
                        }
                    }
                } else {
                    itr = properties.getBiomes().iterator();

                    while(itr.hasNext()) {
                        BiomeManager.BiomeEntry biome = (BiomeManager.BiomeEntry)itr.next();
                        biomes.add(biome.biome);
                    }
                }
                return biomes;
            }
        } else {
            return Collections.emptyList();
        }
    }

    public static int getTravelCost(SpaceStationObject station){
        IDimensionProperties properties = station.getProperties().getParentProperties();
        IDimensionProperties destProperties = DimensionManager.getInstance().getDimensionProperties(station.getDestOrbitingBody());
        if (properties == DimensionManager.defaultSpaceDimensionProperties) {
            return 2147483647;
        }

        if (destProperties.getStar() != properties.getStar()) {
            return 500;
        }

        while(destProperties.getParentProperties() != null && destProperties.isMoon()) {
            destProperties = destProperties.getParentProperties();
        }

        if (destProperties.isMoon() && destProperties.getParentPlanet() == properties.getId() || properties.isMoon() && properties.getParentPlanet() == destProperties.getId()) {
            return 1;
        }

        while(true) {
            if (!properties.isMoon()) {
                if (properties.getStar().getId() == destProperties.getStar().getId()) {
                    double x1 = (double)((float)properties.getOrbitalDist() * MathHelper.cos((float)properties.getOrbitTheta()));
                    double y1 = (double)((float)properties.getOrbitalDist() * MathHelper.sin((float)properties.getOrbitTheta()));
                    double x2 = (double)((float)destProperties.getOrbitalDist() * MathHelper.cos((float)destProperties.getOrbitTheta()));
                    double y2 = (double)((float)destProperties.getOrbitalDist() * MathHelper.sin((float)destProperties.getOrbitTheta()));
                    return Math.max((int)Math.sqrt(Math.pow(x1 - x2, 2.0D) + Math.pow(y1 - y2, 2.0D)), 1);
                }
                break;
            }

            properties = properties.getParentProperties();
        }

        return 2147483647;
    }

    public static boolean meetsArtifactReq(TileWarpShipMonitor te, DimensionProperties properties) {
        if (properties.getRequiredArtifacts().isEmpty()) {
            return true;
        } else {
            List<ItemStack> list = new LinkedList<>(properties.getRequiredArtifacts());

            for(int i = 4; i <= 8; ++i) {
                ItemStack stack2 = te.getStackInSlot(i);
                if (stack2 != null) {
                    Iterator<?> itr = list.iterator();

                    while(itr.hasNext()) {
                        ItemStack stackInList = (ItemStack)itr.next();
                        if (stackInList.getItem().equals(stack2.getItem()) && stackInList.getItemDamage() == stack2.getItemDamage() && ItemStack.areItemStackTagsEqual(stackInList, stack2) && stack2.getCount() >= stackInList.getCount()) {
                            itr.remove();
                        }
                    }
                }
            }

            return list.isEmpty();
        }
    }
}
