package org.opengroup.osdu.crs.GeoJson;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotEmpty;

import java.io.IOException;
import java.util.ArrayList;

@Data
@NoArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = GeoJsonPoint.class, name = "Point"),
        @JsonSubTypes.Type(value = GeoJsonPoint.class, name = "AnyCrsPoint"),
        @JsonSubTypes.Type(value = GeoJsonMultiPoint.class, name = "MultiPoint"),
        @JsonSubTypes.Type(value = GeoJsonMultiPoint.class, name = "AnyCrsMultiPoint"),
        @JsonSubTypes.Type(value = GeoJsonLineString.class, name = "LineString"),
        @JsonSubTypes.Type(value = GeoJsonLineString.class, name = "AnyCrsLineString"),
        @JsonSubTypes.Type(value = GeoJsonMultiLineString.class, name = "MultiLineString"),
        @JsonSubTypes.Type(value = GeoJsonMultiLineString.class, name = "AnyCrsMultiLineString"),
        @JsonSubTypes.Type(value = GeoJsonPolygon.class, name = "Polygon"),
        @JsonSubTypes.Type(value = GeoJsonPolygon.class, name = "AnyCrsPolygon"),
        @JsonSubTypes.Type(value = GeoJsonMultiPolygon.class, name = "MultiPolygon"),
        @JsonSubTypes.Type(value = GeoJsonMultiPolygon.class, name = "AnyCrsMultiPolygon"),
        @JsonSubTypes.Type(value = GeoJsonGeometryCollection.class, name = "GeometryCollection"),
        @JsonSubTypes.Type(value = GeoJsonGeometryCollection.class, name = "AnyCrsGeometryCollection"),
        @JsonSubTypes.Type(value = GeoJsonFeature.class, name = "Feature"),
        @JsonSubTypes.Type(value = GeoJsonFeature.class, name = "AnyCrsFeature"),
        @JsonSubTypes.Type(value = GeoJsonFeatureCollection.class, name = "FeatureCollection"),
        @JsonSubTypes.Type(value = GeoJsonFeatureCollection.class, name = "AnyCrsFeatureCollection")
})
@JsonIgnoreProperties({"valid", "dimension", "length", "geoJsonVariant"})
public abstract class GeoJsonBase {
    private static final String ANY_CRS_PREFIX = "AnyCrs";
    public enum GeoJsonVariant { GEO_JSON, ANY_CRS_GEO_JSON }

    @JsonProperty("type")
    @NotEmpty
    private String type;

    @JsonProperty("bbox")
    private double[] bbox;

    private int dimension;
    boolean valid;
    GeoJsonBase(String type) {
        this.type = type;
        this.dimension = -1;
        this.valid = false;
    }

    public int getDimension() {
        this.isValid();
        return this.dimension;
    }

    public static GeoJsonBase createInstance(String json) {
        GeoJsonBase result;
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            JsonNode node = mapper.readTree(json);
            result = mapper.treeToValue(node, GeoJsonBase.class);
            result.setValid(result.isValid());
        } catch (IOException e) {
            return null;
        }
        return result;
    }

    public String toJsonString() {
        String result;
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        mapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        try {
            result = mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return null;
        }
        return result;
    }

    abstract public void updateBbox();

    abstract public boolean isValid();

    abstract int getLength();

    public GeoJsonVariant getGeoJsonVariant(){
        if (this.getType().startsWith(ANY_CRS_PREFIX)) return GeoJsonVariant.ANY_CRS_GEO_JSON;
        return GeoJsonVariant.GEO_JSON;
    }

    private void setGeoJsonVariantInternal(GeoJsonVariant gj_variant){
        if (gj_variant == GeoJsonVariant.ANY_CRS_GEO_JSON) {
            if (!this.getType().startsWith(ANY_CRS_PREFIX)) {
                this.setType(ANY_CRS_PREFIX+this.getType());
            }
        } else {
            if (this.getType().startsWith(ANY_CRS_PREFIX)) {
                this.setType(this.getType().replace(ANY_CRS_PREFIX, ""));
            }
        }
    }

    public void setGeoJsonVariant(GeoJsonVariant gj_variant){
        ArrayList<GeoJsonBase> components = new ArrayList<>();
        this.appendParts(components);
        for (GeoJsonBase gb : components){
            gb.setGeoJsonVariantInternal(gj_variant);
        }
    }

    abstract void appendParts(ArrayList<GeoJsonBase> components);

    public void replaceCoordinates(GeoJsonCoordinates coordinates) {
        replaceCoordinateArray(coordinates, this);
    }

    public GeoJsonCoordinates extractCoordinates() {
        GeoJsonCoordinates gc = null;
        if (this.isValid()){
            ArrayList<GeoJsonBase> components = new ArrayList<>();
            this.appendParts(components);
            gc = new GeoJsonCoordinates(this.getLength());
            for (GeoJsonBase gb : components){
                gc.addAnyCoordinates(gb.getCoordinatesArray());
            }
        }
        return gc;
    }

    abstract Object getCoordinatesArray();

    private static void replaceCoordinateArray(GeoJsonCoordinates coordinates, GeoJsonBase g){
        if (g instanceof GeoJsonPoint) {
            replaceCoordinateArray(coordinates, ((GeoJsonPoint)g).getCoordinates());
        } else if (g instanceof GeoJsonMultiPoint) {
            replaceCoordinateArray(coordinates, ((GeoJsonMultiPoint)g).getCoordinates());
        } else if (g instanceof GeoJsonLineString) {
            replaceCoordinateArray(coordinates, ((GeoJsonLineString)g).getCoordinates());
        } else if (g instanceof GeoJsonMultiLineString) {
            replaceCoordinateArray(coordinates, ((GeoJsonMultiLineString)g).getCoordinates());
        } else if (g instanceof GeoJsonPolygon) {
            replaceCoordinateArray(coordinates, ((GeoJsonPolygon)g).getCoordinates());
        } else if (g instanceof GeoJsonMultiPolygon) {
            replaceCoordinateArray(coordinates, ((GeoJsonMultiPolygon)g).getCoordinates());
        } else if (g instanceof GeoJsonGeometryCollection) {
            for (GeoJsonBase gb : ((GeoJsonGeometryCollection) g).getGeometries()) {
                replaceCoordinateArray(coordinates, gb);
            }
        } else if (g instanceof GeoJsonFeature) {
            GeoJsonBase gb = ((GeoJsonFeature) g).getGeometry();
            replaceCoordinateArray(coordinates, gb);
        } else if (g instanceof GeoJsonFeatureCollection) {
            for (GeoJsonBase gb : ((GeoJsonFeatureCollection) g).getFeatures()) {
                replaceCoordinateArray(coordinates, gb);
            }
        }
    }

    private static void replaceCoordinateArray(GeoJsonCoordinates coordinates, double[] pt) {
        if (pt.length >= 2) {
            int idx = coordinates.getIndex();
            pt[0] = coordinates.getXys()[2 * idx];
            pt[1] = coordinates.getXys()[2 * idx + 1];
            if (pt.length >= 3) {
                pt[2] = coordinates.getZ_s()[idx];
            }
            coordinates.setIndex(idx + 1);
        }
    }

    private static void replaceCoordinateArray(GeoJsonCoordinates coordinates, double[][] pts) {
        int idx = coordinates.getIndex();
        for (double[] pt : pts) {
            if (pt.length >= 2) {
                pt[0] = coordinates.getXys()[2 * idx];
                pt[1] = coordinates.getXys()[2 * idx + 1];
                if (pt.length >= 3) {
                    pt[2] = coordinates.getZ_s()[idx];
                }
                idx++;
            }
        }
        coordinates.setIndex(idx);
    }

    private static void replaceCoordinateArray(GeoJsonCoordinates coordinates, double[][][] array_pts) {
        int idx = coordinates.getIndex();
        for (double[][] pts : array_pts) {
            for (double[] pt : pts) {
                if (pt.length >= 2) {
                    pt[0] = coordinates.getXys()[2 * idx];
                    pt[1] = coordinates.getXys()[2 * idx + 1];
                    if (pt.length >= 3) {
                        pt[2] = coordinates.getZ_s()[idx];
                    }
                    idx++;
                }
            }
        }
        coordinates.setIndex(idx);
    }

    private static void replaceCoordinateArray(GeoJsonCoordinates coordinates, double[][][][] array_array_pts) {
        int idx = coordinates.getIndex();
        for (double[][][] array_pts : array_array_pts) {
            for (double[][] pts : array_pts) {
                for (double[] pt : pts) {
                    if (pt.length >= 2) {
                        pt[0] = coordinates.getXys()[2 * idx];
                        pt[1] = coordinates.getXys()[2 * idx + 1];
                        if (pt.length >= 3) {
                            pt[2] = coordinates.getZ_s()[idx];
                        }
                        idx++;
                    }
                }
            }
        }
        coordinates.setIndex(idx);
    }

    static double[] getMinMax(double[] coordinates, int dimension) {
        double[] bbox = new double[dimension * 2];
        for (int i = 0; i < dimension; i++) {
            bbox[i] = coordinates[i];
            bbox[i + dimension] = bbox[i];
        }
        return bbox;
    }

    static double[] getMinMax(double[][] coordinates, int dimension){
        double[] bbox = new double[dimension * 2];
        for (int i = 0; i < dimension; i++) {
            bbox[i] = coordinates[0][i];
            bbox[i + dimension] = coordinates[0][i];
        }
        for (int j = 1; j < coordinates.length; j++) {
            for (int i = 0; i < dimension; i++) {
                bbox[i] = Double.min(bbox[i], coordinates[j][i]);
                bbox[i + dimension] = Double.max(bbox[i + dimension], coordinates[j][i]);
            }
        }
        return bbox;
    }

    static double[] getMinMax(double[][][] coordinates, int dimension){
        double[] bbox = new double[dimension * 2];
        for (int i = 0; i < dimension; i++) {
            bbox[i] = coordinates[0][0][i];
            bbox[i + dimension] = coordinates[0][0][i];
        }
        for (double[][] coordinate : coordinates) {
            for (int j = 1; j < coordinate.length; j++) {
                for (int i = 0; i < dimension; i++) {
                    bbox[i] = Double.min(bbox[i], coordinate[j][i]);
                    bbox[i + dimension] = Double.max(bbox[i + dimension], coordinate[j][i]);
                }
            }
        }
        return bbox;
    }

    static double[] getMinMax(double[][][][] coordinates, int dimension) {
        double[] bbox = new double[dimension * 2];
        for (int i = 0; i < dimension; i++) {
            bbox[i] = coordinates[0][0][0][i];
            bbox[i + dimension] = coordinates[0][0][0][i];
        }
        for (double[][][] coordinate : coordinates) {
            for (int k = 1; k < coordinate.length; k++) {
                for (int j = 1; j < coordinate[k].length; j++) {
                    for (int i = 0; i < dimension; i++) {
                        bbox[i] = Double.min(bbox[i], coordinate[k][j][i]);
                        bbox[i + dimension] = Double.max(bbox[i + dimension], coordinate[k][j][i]);
                    }
                }
            }
        }
        return bbox;
    }
}
