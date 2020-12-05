package com.ocrocketry.util;

public class ORUtils {

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
