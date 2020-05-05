package com.ocrocketry.util;

public class ORUtils {

    /*public static boolean isInRange(int val, int min, int max) {
        return val > min && val < max;
    }*/

    public static boolean isInRangeInc(int val, int min, int max) {
        return val >= min && val <= max;
    }
}
