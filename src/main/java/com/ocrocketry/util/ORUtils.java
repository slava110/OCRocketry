package com.ocrocketry.util;

import zmaster587.advancedRocketry.tile.multiblock.TileSpaceLaser;

public class ORUtils {

    /*public static boolean isInRange(int val, int min, int max) {
        return val > min && val < max;
    }*/

    public static boolean isInRangeInc(int val, int min, int max) {
        return val >= min && val <= max;
    }

    public static <T extends Enum<T>> boolean enumContains(Class<T> enumCl, String enumName) {
        for (T val : enumCl.getEnumConstants()) {
            if (val.name().equals(enumName)) {
                return true;
            }
        }

        return false;
    }
}
