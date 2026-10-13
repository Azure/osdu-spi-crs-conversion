package org.opengroup.osdu.crs.interfaces;

import java.util.List;

import org.opengroup.osdu.crs.BinGrid.AbstractBinGrid;
import org.opengroup.osdu.crs.BinGrid.AbstractFeature;
import org.opengroup.osdu.crs.GeoJson.GeoJsonFeatureCollection;
import org.opengroup.osdu.crs.model.ConvertBinGridResponse;
import org.opengroup.osdu.crs.model.ConvertGeoJsonResponse;
import org.opengroup.osdu.crs.model.ConvertPointsResponse;
import org.opengroup.osdu.crs.model.ConvertPointsResponseV4;

public interface ICRSConverter {

	ConvertPointsResponse convertPoint(String from, String to, double[] xyCoordinates, double[] zCoordinates);

	ConvertGeoJsonResponse convertGeoJson(GeoJsonFeatureCollection featureCollection, String toCrs, String toUnitZ);
	ConvertGeoJsonResponse convertGeoJsonV4(GeoJsonFeatureCollection featureCollection, String toCrs, String toUnitZ,String transform);
	
	ConvertBinGridResponse squaring(String toCrs, AbstractBinGrid inBinGrid, ConvertBinGridResponse outBinGrid);

	GeoJsonFeatureCollection prepareGeoJsonRequest(List<AbstractFeature> abstractFeature, String crsId);

	ConvertPointsResponseV4 convertPointV4(String from, String to, String transform, double[] xyCoordinates, double[] zCoordinates);
}
