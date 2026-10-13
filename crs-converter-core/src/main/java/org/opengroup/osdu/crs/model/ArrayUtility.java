package org.opengroup.osdu.crs.model;

import java.lang.Math;

public final class ArrayUtility {
    private ArrayUtility() {
    }

    private static final double EPS = 1.0e-15;

    private static boolean almostEqual(double x, double y) {
        double relDiff = Math.abs(x - y) / (1.0 + 0.5 * (Math.abs(x) + Math.abs(y)));
        return (relDiff < EPS);
    }

    public static void setUnconvertedValuesToNaN(double[] xyCoordinates, double[] zCoordinates, double[] currentPt) {
        for (int i = 0; i < xyCoordinates.length; i += 2) {
            if (almostEqual(xyCoordinates[i], currentPt[i]) && almostEqual(xyCoordinates[i + 1], currentPt[i + 1])) {
                xyCoordinates[i] = Double.NaN;
                xyCoordinates[i + 1] = Double.NaN;
                zCoordinates[i / 2] = Double.NaN;
            }
            else if (Double.isNaN(xyCoordinates[i]) || Double.isNaN(xyCoordinates[i + 1]) || Double.isNaN(zCoordinates[i / 2])) {
                xyCoordinates[i] = Double.NaN;
                xyCoordinates[i + 1] = Double.NaN;
                zCoordinates[i / 2] = Double.NaN;
            }
        }
    }
}
