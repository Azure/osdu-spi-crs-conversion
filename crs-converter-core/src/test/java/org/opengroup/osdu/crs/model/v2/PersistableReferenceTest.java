package org.opengroup.osdu.crs.model.v2;

import org.junit.Test;
import static org.junit.Assert.*;

public class PersistableReferenceTest {
    private static final String EB1 = "{\"authCode\":{\"auth\":\"SLB\",\"code\":\"4618011\"},\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4618\"},\"name\":\"GCS_South_American_1969\",\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGCS[\\\"GCS_South_American_1969\\\",DATUM[\\\"D_South_American_1969\\\",SPHEROID[\\\"GRS_1967_Truncated\\\",6378160.0,298.25]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433],AUTHORITY[\\\"EPSG\\\",4618]]\"},\"name\":\"SAD69 * DMA-Peru [4618,1874]\",\"singleCT\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"1874\"},\"name\":\"SAD_1969_To_WGS_1984_11\",\"type\":\"ST\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGTRAN[\\\"SAD_1969_To_WGS_1984_11\\\",GEOGCS[\\\"GCS_South_American_1969\\\",DATUM[\\\"D_South_American_1969\\\",SPHEROID[\\\"GRS_1967_Truncated\\\",6378160.0,298.25]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Geocentric_Translation\\\"],PARAMETER[\\\"X_Axis_Translation\\\",-58.0],PARAMETER[\\\"Y_Axis_Translation\\\",0.0],PARAMETER[\\\"Z_Axis_Translation\\\",-44.0],AUTHORITY[\\\"EPSG\\\",1874]]\"},\"type\":\"EBC\",\"ver\":\"PE_10_3_1\"}";
    private static final String EB2 = "{\"authCode\":{\"auth\":\"SLB\",\"code\":\"32065100\"},\"compoundCT\":{\"authCode\":{\"auth\":\"SLB\",\"code\":\"158511693\"},\"cts\":[{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"15851\"},\"name\":\"NAD_1927_To_WGS_1984_79_CONUS\",\"type\":\"ST\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGTRAN[\\\"NAD_1927_To_WGS_1984_79_CONUS\\\",GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.9786982]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"NADCON\\\"],PARAMETER[\\\"Dataset_conus\\\",0.0],AUTHORITY[\\\"EPSG\\\",15851]]\"},{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"1693\"},\"name\":\"NAD_1927_To_WGS_1984_33\",\"type\":\"ST\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGTRAN[\\\"NAD_1927_To_WGS_1984_33\\\",GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.9786982]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"NTv2\\\"],PARAMETER[\\\"Dataset_canada/Ntv2_0\\\",0.0],AUTHORITY[\\\"EPSG\\\",1693]]\"}],\"name\":\"Fallback NAD27 to WGS 84 (79)/NAD27 to WGS 84 (33)\",\"policy\":\"Fallback\",\"type\":\"CT\",\"ver\":\"PE_10_3_1\"},\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"32065\"},\"name\":\"NAD_1927_BLM_Zone_15N\",\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"wkt\":\"PROJCS[\\\"NAD_1927_BLM_Zone_15N\\\",GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.9786982]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],PROJECTION[\\\"Transverse_Mercator\\\"],PARAMETER[\\\"False_Easting\\\",1640416.66666667],PARAMETER[\\\"False_Northing\\\",0.0],PARAMETER[\\\"Central_Meridian\\\",-93.0],PARAMETER[\\\"Scale_Factor\\\",0.9996],PARAMETER[\\\"Latitude_Of_Origin\\\",0.0],UNIT[\\\"Foot_US\\\",0.304800609601219],AUTHORITY[\\\"EPSG\\\",32065]]\"},\"name\":\"NAD27 * SIS: Fallback,15851,1693 / BLM 15N (ftUS) [32065,158511693]\",\"type\":\"EBC\",\"ver\":\"PE_10_3_1\"}";
    private static final String LB1 = "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"2193\"},\"name\":\"NZGD_2000_New_Zealand_Transverse_Mercator\",\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"wkt\":\"PROJCS[\\\"NZGD_2000_New_Zealand_Transverse_Mercator\\\",GEOGCS[\\\"GCS_NZGD_2000\\\",DATUM[\\\"D_NZGD_2000\\\",SPHEROID[\\\"GRS_1980\\\",6378137.0,298.257222101]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],PROJECTION[\\\"Transverse_Mercator\\\"],PARAMETER[\\\"False_Easting\\\",1600000.0],PARAMETER[\\\"False_Northing\\\",10000000.0],PARAMETER[\\\"Central_Meridian\\\",173.0],PARAMETER[\\\"Scale_Factor\\\",0.9996],PARAMETER[\\\"Latitude_Of_Origin\\\",0.0],UNIT[\\\"Meter\\\",1.0],AUTHORITY[\\\"EPSG\\\",2193]]\"}";
    private static final String LB2 = "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4326\"},\"name\":\"GCS_WGS_1984\",\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433],AUTHORITY[\\\"EPSG\\\",4326]]\"}";
    private static final String AOU = "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"2010\"},\"boundBox\":{\"latMax\":84.0,\"latMin\":0.0,\"lonMax\":-144.0,\"lonMin\":-150.0},\"name\":null,\"type\":\"AOU\"}";
    private static final String ST1 = "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"15846\"},\"name\":\"Egypt_Gulf_of_Suez_S-650_TL_To_WGS_1984_2\",\"type\":\"ST\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGTRAN[\\\"Egypt_Gulf_of_Suez_S-650_TL_To_WGS_1984_2\\\",GEOGCS[\\\"GCS_Egypt_Gulf_of_Suez_S-650_TL\\\",DATUM[\\\"D_Egypt_Gulf_of_Suez_S-650_TL\\\",SPHEROID[\\\"Helmert_1906\\\",6378200.0,298.3]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Geocentric_Translation\\\"],PARAMETER[\\\"X_Axis_Translation\\\",-146.21],PARAMETER[\\\"Y_Axis_Translation\\\",112.63],PARAMETER[\\\"Z_Axis_Translation\\\",4.05],AUTHORITY[\\\"EPSG\\\",15846]]\"}";
    private static final String ST2 = "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"1527\"},\"name\":\"Campo_Inchauspe_To_WGS_1984_2\",\"type\":\"ST\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGTRAN[\\\"Campo_Inchauspe_To_WGS_1984_2\\\",GEOGCS[\\\"GCS_Campo_Inchauspe\\\",DATUM[\\\"D_Campo_Inchauspe\\\",SPHEROID[\\\"International_1924\\\",6378388.0,297.0]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Geocentric_Translation\\\"],PARAMETER[\\\"X_Axis_Translation\\\",-154.5],PARAMETER[\\\"Y_Axis_Translation\\\",150.7],PARAMETER[\\\"Z_Axis_Translation\\\",100.4],AUTHORITY[\\\"EPSG\\\",1527]]\"}";
    private static final String CT1 = "{\"authCode\":{\"auth\":\"SLB\",\"code\":\"158511693\"},\"cts\":[{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"15851\"},\"name\":\"NAD_1927_To_WGS_1984_79_CONUS\",\"type\":\"ST\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGTRAN[\\\"NAD_1927_To_WGS_1984_79_CONUS\\\",GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.9786982]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"NADCON\\\"],PARAMETER[\\\"Dataset_conus\\\",0.0],AUTHORITY[\\\"EPSG\\\",15851]]\"},{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"1693\"},\"name\":\"NAD_1927_To_WGS_1984_33\",\"type\":\"ST\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGTRAN[\\\"NAD_1927_To_WGS_1984_33\\\",GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.9786982]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"NTv2\\\"],PARAMETER[\\\"Dataset_canada/Ntv2_0\\\",0.0],AUTHORITY[\\\"EPSG\\\",1693]]\"}],\"name\":\"Fallback NAD27 to WGS 84 (79)/NAD27 to WGS 84 (33)\",\"policy\":\"Fallback\",\"type\":\"CT\",\"ver\":\"PE_10_3_1\"}";
    private static final String CC1 = "{\"authCode\":{\"auth\":\"SLB\",\"code\":\"326355773\"},\"horzLateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"32635\"},\"name\":\"WGS_1984_UTM_Zone_35N\",\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"wkt\":\"PROJCS[\\\"WGS_1984_UTM_Zone_35N\\\",GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],PROJECTION[\\\"Transverse_Mercator\\\"],PARAMETER[\\\"False_Easting\\\",500000.0],PARAMETER[\\\"False_Northing\\\",0.0],PARAMETER[\\\"Central_Meridian\\\",27.0],PARAMETER[\\\"Scale_Factor\\\",0.9996],PARAMETER[\\\"Latitude_Of_Origin\\\",0.0],UNIT[\\\"Meter\\\",1.0],AUTHORITY[\\\"EPSG\\\",32635]]\"},\"name\":\"WGS 84 / UTM zone 35N + EGM96 height\",\"type\":\"CC\",\"ver\":\"PE_10_3_1\",\"vertLateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"5773\"},\"name\":\"EGM96_Geoid\",\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"wkt\":\"VERTCS[\\\"EGM96_Geoid\\\",VDATUM[\\\"EGM96_Geoid\\\"],PARAMETER[\\\"Vertical_Shift\\\",0.0],PARAMETER[\\\"Direction\\\",1.0],UNIT[\\\"Meter\\\",1.0],AUTHORITY[\\\"EPSG\\\",5773]]\"}}";
    private static final String UN1 = "{\"baseMeasurement\":{\"ancestry\":\"Length\",\"type\":\"UM\"},\"scaleOffset\":{\"offset\":0.0,\"scale\":0.3048006096012192},\"symbol\":\"ftUS\",\"type\":\"USO\"}";
    private static final String UN2 = "{\"baseMeasurement\":{\"ancestry\":\"Length\",\"type\":\"UM\"},\"scaleOffset\":{\"offset\":0.0,\"scale\":1.0},\"symbol\":\"m\",\"type\":\"USO\"}";
    private static final String UN3 = "{\"abcd\":{\"a\":2298.35,\"b\":5.0,\"c\":9.0,\"d\":0.0},\"baseMeasurement\":{\"ancestry\":\"K\",\"type\":\"UM\"},\"symbol\":\"degF\",\"type\":\"UAD\"}";
    private static final String UM1 = "{\"ancestry\":\"Plane_Angle\",\"type\":\"UM\"}";

    @Test
    public void testUnitScaleOffset() {
        PersistableReference pr1, pr2;
        String prs;
        UnitScaleOffset uso;

        uso = new UnitScaleOffset();
        assertNotNull(uso);
        assertNull(uso.getScaleOffset());
        pr1 = PersistableReference.createInstance(UN1);
        assertNotNull(pr1);
        assertTrue(pr1 instanceof UnitScaleOffset);
        uso = (UnitScaleOffset) pr1;
        assertNotNull(uso.getBaseMeasurement());
        assertNotNull(uso.getUnitSymbol());
        assertEquals("ftUS", uso.getUnitSymbol());
        assertEquals("Length", uso.getBaseMeasurement().getMeasurementAncestry());
        assertEquals(.3048006096012192, uso.getScaleOffset().getScaleFactor(), 1.0e-10);
        assertEquals(0.0, uso.getScaleOffset().getOffset(), 1.0e-16);
        prs = pr1.toJsonString();
        assertEquals(UN1, prs);
        pr2 = PersistableReference.createInstance(prs);
        assertNotNull(pr2);
        assertTrue(pr2 instanceof UnitScaleOffset);
        assertEquals(pr1, pr2);
        pr2 = PersistableReference.createInstance(UN2);
        assertNotNull(pr1);
        assertTrue(pr2 instanceof UnitScaleOffset);
        assertNotEquals(pr1, pr2);
    }

    @Test
    public void testUnitEnergistics() {
        PersistableReference pr1, pr2;
        String prs;
        UnitEnergistics uen;

        uen = new UnitEnergistics();
        assertNotNull(uen);
        assertNull(uen.getAbcd());
        pr1 = PersistableReference.createInstance(UN3);

        assertNotNull(pr1);
        assertTrue(pr1 instanceof UnitEnergistics);
        uen = (UnitEnergistics)pr1;
        assertNotNull(uen.getBaseMeasurement());
        assertNotNull(uen.getUnitSymbol());
        assertEquals("degF", uen.getUnitSymbol());
        assertEquals("K", uen.getBaseMeasurement().getMeasurementAncestry());
        assertEquals(2298.35, uen.getAbcd().getA(), 1.0e-10);
        assertEquals(5, uen.getAbcd().getB(), 1.0e-10);
        assertEquals(9, uen.getAbcd().getC(), 1.0e-10);
        assertEquals(0, uen.getAbcd().getD(), 1.0e-10);
        prs = pr1.toJsonString();
        pr2 = PersistableReference.createInstance(prs);
        assertNotNull(pr2);
        assertTrue(pr2 instanceof UnitEnergistics);
        assertEquals(pr1, pr2);
        assertEquals(UN3, prs);
    }

    @Test
    public void testMeasurement(){
        PersistableReference pr1, pr2;
        String prs;
        Measurement u;
        pr1 = PersistableReference.createInstance(UM1);
        assertNotNull(pr1);
        assertTrue(pr1 instanceof Measurement);
        u = (Measurement) pr1;
        assertNotNull(u.getMeasurementAncestry());
        prs = pr1.toJsonString();
        assertEquals(UM1, prs);
        pr2 = PersistableReference.createInstance(prs);
        assertNotNull(pr1);
        assertTrue(pr2 instanceof Measurement);
        assertEquals(pr1, pr2);

    }

    @Test
    public void testAreaOfUse(){
        PersistableReference pr1, pr2;
        String prs;
        AreaOfUse aou;

        pr1 = PersistableReference.createInstance(AOU);
        assertNotNull(pr1);
        assertTrue(pr1 instanceof AreaOfUse);
        aou = (AreaOfUse)pr1;
        prs = pr1.toJsonString();
        String compare = AOU;
        compare = compare.replace("\"name\":null,", "");
        assertEquals(compare, prs);
        pr2 = PersistableReference.createInstance(prs);
        assertNotNull(pr1);
        assertTrue(pr2 instanceof AreaOfUse);
        assertEquals(pr1, pr2);
        assertNotNull(aou.getBoundingBox());
        assertEquals(0.0, aou.getBoundingBox().getLatitudeLower(), 1.0e-16);
        assertEquals(84.0, aou.getBoundingBox().getLatitudeUpper(), 1.0e-16);
        assertEquals(-150.0, aou.getBoundingBox().getLongitudeLeft(), 1.0e-16);
        assertEquals(-144.0, aou.getBoundingBox().getLongitudeRight(), 1.0e-16);
    }

    @Test
    public void testEarlyBound() {
        PersistableReference pr1, pr2;
        EarlyBoundCrs eb;
        String prs;

        pr1 = PersistableReference.createInstance(EB1);
        assertNotNull(pr1);
        assertTrue(pr1 instanceof EarlyBoundCrs);
        eb = (EarlyBoundCrs)pr1;
        assertNotNull(eb.getAuthorityCode());
        assertNotNull(eb.getCrsName());
        assertNotNull(eb.getVersion());
        assertNotNull(eb.getLateBoundCrs());
        assertNotNull(eb.getSingleTransformation());
        assertNull(eb.getCompoundTransformation());
        prs = pr1.toJsonString();
        assertEquals(EB1, prs);
        pr2 = PersistableReference.createInstance(prs);
        assertNotNull(pr2);
        assertEquals(pr1, pr2);
        pr2 = PersistableReference.createInstance(EB2);
        assertNotNull(pr2);
        assertEquals(pr2, PersistableReference.createInstance(pr2.toJsonString()));
        assertEquals(EB2, pr2.toJsonString());
        assertNotEquals(pr1, pr2);
        eb = (EarlyBoundCrs)pr2;
        assertNotNull(eb.getLateBoundCrs());
        assertNotNull(eb.getCompoundTransformation());
        assertNull(eb.getSingleTransformation());
    }

    @Test
    public void testLateBound() {
        PersistableReference pr1, pr2;
        LateBoundCrs lb;
        String prs;

        pr1 = PersistableReference.createInstance(LB1);
        assertNotNull(pr1);
        assertTrue(pr1 instanceof LateBoundCrs);
        lb = (LateBoundCrs)pr1;
        assertNotNull(lb.getLateBoundCrsWkt());
        assertNotNull(lb.getAuthorityCode());
        assertNotNull(lb.getCrsName());
        assertNotNull(lb.getVersion());
        prs = pr1.toJsonString();
        assertEquals(LB1, prs);
        pr2 = PersistableReference.createInstance(prs);
        assertNotNull(pr2);
        assertEquals(pr1, pr2);
        pr2 = PersistableReference.createInstance(LB2);
        assertNotNull(pr2);
        assertEquals(pr2, PersistableReference.createInstance(pr2.toJsonString()));
        assertEquals(LB2, pr2.toJsonString());
        assertNotEquals(pr1, pr2);
        lb = (LateBoundCrs)pr2;
        assertNotNull(lb.getLateBoundCrsWkt());
        assertNotNull(lb.getAuthorityCode());
        assertNotNull(lb.getCrsName());
        assertNotNull(lb.getVersion());
        assertNotEquals(pr1, pr2);
    }

    @Test
    public void testSingleTrf() {
        PersistableReference pr1, pr2;
        SingleTrf st;
        String prs;

        pr1 = PersistableReference.createInstance(ST1);
        assertNotNull(pr1);
        assertTrue(pr1 instanceof SingleTrf);
        st = (SingleTrf)pr1;
        assertNotNull(st.getWellKnownText());
        assertNotNull(st.getAuthorityCode());
        assertNotNull(st.getTrfName());
        assertNotNull(st.getVersion());
        prs = pr1.toJsonString();
        assertEquals(ST1, prs);
        pr2 = PersistableReference.createInstance(prs);
        assertNotNull(pr2);
        assertEquals(pr1, pr2);
        pr2 = PersistableReference.createInstance(ST2);
        assertNotNull(pr2);
        assertEquals(pr2, PersistableReference.createInstance(pr2.toJsonString()));
        assertEquals(ST2, pr2.toJsonString());
        assertNotEquals(pr1, pr2);
        st = (SingleTrf)pr2;
        assertNotNull(st.getWellKnownText());
        assertNotNull(st.getAuthorityCode());
        assertNotNull(st.getTrfName());
        assertNotNull(st.getVersion());
        assertNotEquals(pr1, pr2);
    }

    @Test
    public void testCompoundTrf() {
        PersistableReference pr1, pr2;
        CompoundTrf ct;
        String prs;
        pr1 = PersistableReference.createInstance(CT1);
        assertNotNull(pr1);
        assertTrue(pr1 instanceof CompoundTrf);
        ct = (CompoundTrf)pr1;
        assertNotNull(ct.getTransformationList());
        assertEquals(2, ct.getTransformationList().size());
        for (SingleTrf st : ct.getTransformationList()){
            assertNotNull(st);
        }
        assertNotNull(ct.getAuthorityCode());
        assertNotNull(ct.getTrfName());
        assertNotNull(ct.getVersion());
        prs = pr1.toJsonString();
        assertEquals(CT1, prs);
    }

    @Test
    public void testCompoundCrs() {
        PersistableReference pr1, pr2;
        CompoundCrs cc;
        String prs;

        pr1 = PersistableReference.createInstance(CC1);
        assertNotNull(pr1);
        assertTrue(pr1 instanceof CompoundCrs);
        cc = (CompoundCrs)pr1;
        assertNotNull(cc.getAuthorityCode());
        assertNotNull(cc.getCrsName());
        assertNotNull(cc.getVersion());
        assertNotNull(cc.getHorizontalLateBoundCrs());
        assertNotNull(cc.getVerticalLateBoundCrs());
        prs = pr1.toJsonString();
        assertEquals(CC1, prs);
        pr2 = PersistableReference.createInstance(prs);
        assertNotNull(pr2);
        assertEquals(pr1, pr2);
    }

    @Test
    public void testFailures(){
        PersistableReference pr1;
        String corrupt = LB1.replace("{", "[");
        pr1 = PersistableReference.createInstance(corrupt);
        assertNull(pr1);
        pr1 = new PersistableReference();
        corrupt = pr1.toJsonString();
        assertEquals("{}", corrupt);
    }
}

