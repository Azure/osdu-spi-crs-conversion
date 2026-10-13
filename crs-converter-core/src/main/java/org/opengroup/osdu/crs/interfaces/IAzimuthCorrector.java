package org.opengroup.osdu.crs.interfaces;

public interface IAzimuthCorrector {
	int correctAzimuth(String crs, String azimuthReference, double[] xyCoordinates, double[] azimuths);
}
