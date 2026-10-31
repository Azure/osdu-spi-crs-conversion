package org.opengroup.osdu.crs.api;

import jakarta.validation.ValidationException;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.crs.converter.TrajectoryConverterV4Tests;
import org.opengroup.osdu.crs.interfaces.ICRSConverter;
import org.opengroup.osdu.crs.interfaces.IPointConverter;
import org.opengroup.osdu.crs.interfaces.ITrajectoryConverter;
import org.opengroup.osdu.crs.model.v4.ConvertTrajectoryRequestV4;
import org.opengroup.osdu.crs.model.v4.ConvertTrajectoryResponseV4;
import org.opengroup.osdu.crs.model.v4.MinimumDepthInterval;
import org.opengroup.osdu.crs.model.v4.TrajectoryStationInV4;
import org.opengroup.osdu.crs.util.Constants;
import org.springframework.http.HttpHeaders;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

@RunWith(MockitoJUnitRunner.class)
public class CrsConverterApiV4Tests {

    private static final String RES_AZI_PROJ_CRS_GN = "{\r\n    \"trajectoryCRS\": \"{\\\"authCode\\\":{\\\"auth\\\":\\\"EPSG\\\",\\\"code\\\":\\\"32631\\\"},\\\"name\\\":\\\"WGS_1984_UTM_Zone_31N\\\",\\\"type\\\":\\\"LBC\\\",\\\"ver\\\":\\\"PE_10_9_1\\\",\\\"wkt\\\":\\\"PROJCS[\\\\\\\"WGS_1984_UTM_Zone_31N\\\\\\\",GEOGCS[\\\\\\\"GCS_WGS_1984\\\\\\\",DATUM[\\\\\\\"D_WGS_1984\\\\\\\",SPHEROID[\\\\\\\"WGS_1984\\\\\\\",6378137.0,298.257223563]],PRIMEM[\\\\\\\"Greenwich\\\\\\\",0.0],UNIT[\\\\\\\"Degree\\\\\\\",0.0174532925199433]],PROJECTION[\\\\\\\"Transverse_Mercator\\\\\\\"],PARAMETER[\\\\\\\"False_Easting\\\\\\\",500000.0],PARAMETER[\\\\\\\"False_Northing\\\\\\\",0.0],PARAMETER[\\\\\\\"Central_Meridian\\\\\\\",3.0],PARAMETER[\\\\\\\"Scale_Factor\\\\\\\",0.9996],PARAMETER[\\\\\\\"Latitude_Of_Origin\\\\\\\",0.0],UNIT[\\\\\\\"Meter\\\\\\\",1.0],AUTHORITY[\\\\\\\"EPSG\\\\\\\",32631]]\\\"}\",\r\n    \"unitXY\": \"{\\\"abcd\\\":{\\\"a\\\":0.0,\\\"b\\\":1.0,\\\"c\\\":1.0,\\\"d\\\":0.0},\\\"symbol\\\":\\\"m\\\",\\\"baseMeasurement\\\":{\\\"ancestry\\\":\\\"L\\\",\\\"type\\\":\\\"UM\\\"},\\\"type\\\":\\\"UAD\\\"}\",\r\n    \"unitZ\": \"{\\\"abcd\\\":{\\\"a\\\":0.0,\\\"b\\\":1.0,\\\"c\\\":1.0,\\\"d\\\":0.0},\\\"symbol\\\":\\\"m\\\",\\\"baseMeasurement\\\":{\\\"ancestry\\\":\\\"L\\\",\\\"type\\\":\\\"UM\\\"},\\\"type\\\":\\\"UAD\\\"}\",\r\n    \"unitDls\": \"{\\\"scaleOffset\\\":{\\\"scale\\\":5.81776417331443E-4,\\\"offset\\\":0.0},\\\"symbol\\\":\\\"deg/30m\\\",\\\"baseMeasurement\\\":{\\\"ancestry\\\":\\\"Rotation_Per_Length\\\",\\\"type\\\":\\\"UM\\\"},\\\"type\\\":\\\"USO\\\"}\",\r\n    \"stations\": [\r\n        {\r\n            \"md\": 0.0,\r\n            \"inclination\": 90.0,\r\n            \"azimuthTN\": 88.52944953384463,\r\n            \"azimuthGN\": 90.0,\r\n            \"dxTN\": 0.0,\r\n            \"dyTN\": 0.0,\r\n            \"point\": {\r\n                \"x\": 399999.99999999936,\r\n                \"y\": 6499999.999999927,\r\n                \"z\": 0.0\r\n            },\r\n            \"wgs84Longitude\": 1.2778067531835464,\r\n            \"wgs84Latitude\": 58.62877104865894,\r\n            \"dls\": 0.0,\r\n            \"original\": true,\r\n            \"dz\": 0.0\r\n        },\r\n        {\r\n            \"md\": 2000.0,\r\n            \"inclination\": 90.0,\r\n            \"azimuthTN\": 88.52944953384463,\r\n            \"azimuthGN\": 90.0,\r\n            \"dxTN\": 1999.3412953022673,\r\n            \"dyTN\": 51.32625935183246,\r\n            \"point\": {\r\n                \"x\": 401999.4402974973,\r\n                \"y\": 6500000.000000381,\r\n                \"z\": -1.2246467991473532E-13\r\n            },\r\n            \"wgs84Longitude\": 1.312223579337627,\r\n            \"wgs84Latitude\": 58.629227231049995,\r\n            \"dls\": 0.0,\r\n            \"original\": true,\r\n            \"dz\": 1.2246467991473532E-13\r\n        },\r\n        {\r\n            \"md\": 4000.0,\r\n            \"inclination\": 90.0,\r\n            \"azimuthTN\": 88.52944953384463,\r\n            \"azimuthGN\": 90.0,\r\n            \"dxTN\": 3998.6825906045347,\r\n            \"dyTN\": 102.65251870366492,\r\n            \"point\": {\r\n                \"x\": 403998.87098692835,\r\n                \"y\": 6500000.000001728,\r\n                \"z\": -2.4492935982947065E-13\r\n            },\r\n            \"wgs84Longitude\": 1.346641293684646,\r\n            \"wgs84Latitude\": 58.629674207432956,\r\n            \"dls\": 0.0,\r\n            \"original\": true,\r\n            \"dz\": 2.4492935982947065E-13\r\n        },\r\n        {\r\n            \"md\": 6000.0,\r\n            \"inclination\": 90.0,\r\n            \"azimuthTN\": 88.52944953384463,\r\n            \"azimuthGN\": 90.0,\r\n            \"dxTN\": 5998.023885906802,\r\n            \"dyTN\": 153.9787780554974,\r\n            \"point\": {\r\n                \"x\": 405998.2922643785,\r\n                \"y\": 6500000.000003942,\r\n                \"z\": -3.6739403974420595E-13\r\n            },\r\n            \"wgs84Longitude\": 1.381059878154834,\r\n            \"wgs84Latitude\": 58.63011197741336,\r\n            \"dls\": 0.0,\r\n            \"original\": true,\r\n            \"dz\": 3.6739403974420595E-13\r\n        },\r\n        {\r\n            \"md\": 8000.0,\r\n            \"inclination\": 90.0,\r\n            \"azimuthTN\": 88.52944953384463,\r\n            \"azimuthGN\": 90.0,\r\n            \"dxTN\": 7997.365181209069,\r\n            \"dyTN\": 205.30503740732985,\r\n            \"point\": {\r\n                \"x\": 407997.7043259288,\r\n                \"y\": 6500000.000006994,\r\n                \"z\": -4.898587196589413E-13\r\n            },\r\n            \"wgs84Longitude\": 1.4154793146753326,\r\n            \"wgs84Latitude\": 58.63054054060476,\r\n            \"dls\": 0.0,\r\n            \"original\": true,\r\n            \"dz\": 4.898587196589413E-13\r\n        },\r\n        {\r\n            \"md\": 10000.0,\r\n            \"inclination\": 90.0,\r\n            \"azimuthTN\": 88.52944953384463,\r\n            \"azimuthGN\": 90.0,\r\n            \"dxTN\": 9996.706476511337,\r\n            \"dyTN\": 256.6312967591623,\r\n            \"point\": {\r\n                \"x\": 409997.107367656,\r\n                \"y\": 6500000.000010856,\r\n                \"z\": -6.123233995736766E-13\r\n            },\r\n            \"wgs84Longitude\": 1.4498995851702636,\r\n            \"wgs84Latitude\": 58.63095989662879,\r\n            \"dls\": 0.0,\r\n            \"original\": true,\r\n            \"dz\": 6.123233995736766E-13\r\n        }\r\n    ],\r\n    \"localCRS\": \"{\\\"name\\\":\\\"Azimuthal Equidistant\\\",\\\"type\\\":\\\"LBC\\\",\\\"ver\\\":\\\"PE_10_9_1\\\",\\\"wkt\\\":\\\"PROJCS[\\\\\\\"Azimuthal Equidistant Lng=1.27780675;Lat=58.62877105\\\\\\\",GEOGCS[\\\\\\\"GCS_WGS_1984\\\\\\\",DATUM[\\\\\\\"D_WGS_1984\\\\\\\",SPHEROID[\\\\\\\"WGS_1984\\\\\\\",6378137.0,298.257223563]],PRIMEM[\\\\\\\"Greenwich\\\\\\\",0.0],UNIT[\\\\\\\"Degree\\\\\\\",0.0174532925199433]],PROJECTION[\\\\\\\"Modified Azimuthal_Equidistant\\\\\\\"],PARAMETER[\\\\\\\"False_Easting\\\\\\\",0.0],PARAMETER[\\\\\\\"False_Northing\\\\\\\",0.0],PARAMETER[\\\\\\\"Central_Meridian\\\\\\\",1.2778067531835273],PARAMETER[\\\\\\\"Latitude_Of_Origin\\\\\\\",58.62877104865958],UNIT[\\\\\\\"Meter\\\\\\\",1.0]]\\\"}\",\r\n    \"method\": \"AzimuthalEquidistant\",\r\n    \"operationsApplied\": [\r\n        \"derived TN from GN azimuth by grid convergence 358.529450\",\r\n        \"computed deflections via minimum curvature method\",\r\n        \"computation method: AzimuthalEquidistant\",\r\n        \"conversion from 'Azimuthal Equidistant' to 'GCS_WGS_1984'\",\r\n        \"conversion from 'GCS_WGS_1984' to 'WGS_1984_UTM_Zone_31N'\",\r\n        \"to WGS 84: conversion from WGS_1984_UTM_Zone_31N to GCS_WGS_1984; 6 points converted\"\r\n    ],\r\n    \"scaleConvergenceList\": [\r\n        {\r\n            \"scalefactor\": 0.999723,\r\n            \"convergence\": -1.470550000000003,\r\n            \"point\": {\r\n                \"x\": 399999.99999999936,\r\n                \"y\": 6499999.999999927,\r\n                \"z\": 0.0\r\n            }\r\n        },\r\n        {\r\n            \"scalefactor\": 0.999699,\r\n            \"convergence\": -1.3236099999999738,\r\n            \"point\": {\r\n                \"x\": 409997.107367656,\r\n                \"y\": 6500000.000010856,\r\n                \"z\": -6.123233995736766E-13\r\n            }\r\n        }\r\n    ],\r\n    \"inputKind\": \"MD_Incl_Azim\"\r\n}";

    private static final String MISSING_MD_MESSAGE =
            "The first and last inputStations must include an md value when md_i or md_interval is provided.";

    @Mock
    private ITrajectoryConverter crsTrajectoryConverter;

    @Mock
    private CrsConverterApiV4 crsConverterApiV4;

    @Test
    public void convertTrajectoryForAzimuthalEquidistantProjectedCRS_GN_WithSuccess() {
        ConvertTrajectoryResponseV4 convertTrajectoryResponseV4 = TrajectoryConverterV4Tests.createResponse(RES_AZI_PROJ_CRS_GN);
        lenient().when(crsConverterApiV4.getPersistableReferenceFromID(Mockito.any(), Mockito.anyBoolean())).thenReturn("PersistableReference");
        lenient().when(crsConverterApiV4.checkCRSType(Mockito.anyString())).thenReturn(Boolean.TRUE.booleanValue());
        lenient().when(crsConverterApiV4.getPersistableReferenceFromID(Mockito.any(), Mockito.anyBoolean())).thenReturn("PersistableReference");
        lenient().when(crsConverterApiV4.computeMinimumDepthPointsUsingInterval(Mockito.anyDouble(), Mockito.anyDouble(), Mockito.anyDouble())).thenReturn(new ArrayList<>());
        lenient().when(crsConverterApiV4.checkMdiListForRange(Mockito.anyDouble(), Mockito.anyDouble(), Mockito.anyList())).thenReturn(Boolean.TRUE.booleanValue());
        lenient().when(crsTrajectoryConverter.convertTrajectoryV4(Mockito.any(), Mockito.any(), Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(convertTrajectoryResponseV4);
        lenient().when(crsConverterApiV4.convertTrajectory(Mockito.any(MultiValueMap.class), Mockito.any(ConvertTrajectoryRequestV4.class))).thenReturn(convertTrajectoryResponseV4);
        assertEquals(6, convertTrajectoryResponseV4.getStations().size());
    }

    @Test
    public void convertTrajectoryForAzimuthalEquidistantProjectedCRS_GN_Failure_mdi_md_interval_present() {
        CrsConverterApiV4 api = createTrajectoryApi();
        ConvertTrajectoryRequestV4 request = createMdiRequest(null, null,
                new MinimumDepthInterval(Arrays.asList(50.0), 25.0));

        ValidationException exception = Assertions.assertThrows(ValidationException.class,
                () -> api.convertTrajectory(new HttpHeaders(), request));

        assertEquals("Both md_i array and md_interval values are provided in the input.", exception.getMessage());
        Mockito.verifyNoInteractions(crsTrajectoryConverter);
    }

    @Test
    public void convertTrajectoryForAzimuthalEquidistantProjectedCRS_GN_Failure_mdi_not_in_range() {
        for (double md : new double[]{-1.0, 101.0}) {
            CrsConverterApiV4 api = createTrajectoryApi();
            ConvertTrajectoryRequestV4 request = createMdiRequest(0.0, 100.0,
                    new MinimumDepthInterval(Arrays.asList(md), null));

            ValidationException exception = Assertions.assertThrows(ValidationException.class,
                    () -> api.convertTrajectory(new HttpHeaders(), request));

            assertEquals("md_i array values provided are not in range of MD stations.", exception.getMessage());
        }
        Mockito.verifyNoInteractions(crsTrajectoryConverter);
    }

    @Test
    public void convertTrajectory_mdiWithNullFirstStationMd_throwsValidationException() {
        CrsConverterApiV4 api = createTrajectoryApi();
        ConvertTrajectoryRequestV4 request = createMdiRequest(null, 100.0, new MinimumDepthInterval(Arrays.asList(50.0), null));

        ValidationException exception = Assertions.assertThrows(ValidationException.class,
                () -> api.convertTrajectory(new HttpHeaders(), request));

        assertEquals(MISSING_MD_MESSAGE, exception.getMessage());
        Mockito.verify(crsTrajectoryConverter, Mockito.never()).convertTrajectoryV4(any(), any(), anyBoolean(), anyBoolean(), anyBoolean());
    }

    @Test
    public void convertTrajectory_mdIntervalWithNullLastStationMd_throwsValidationException() {
        CrsConverterApiV4 api = createTrajectoryApi();
        ConvertTrajectoryRequestV4 request = createMdiRequest(0.0, null, new MinimumDepthInterval(null, 25.0));

        ValidationException exception = Assertions.assertThrows(ValidationException.class,
                () -> api.convertTrajectory(new HttpHeaders(), request));

        assertEquals(MISSING_MD_MESSAGE, exception.getMessage());
        Mockito.verify(crsTrajectoryConverter, Mockito.never()).convertTrajectoryV4(any(), any(), anyBoolean(), anyBoolean(), anyBoolean());
    }

    @Test
    public void convertTrajectory_mdIntervalWithEmptyInputStations_throwsValidationException() {
        CrsConverterApiV4 api = createTrajectoryApi();
        ConvertTrajectoryRequestV4 request = createMdiRequest(0.0, 100.0, new MinimumDepthInterval(null, 25.0));
        request.setInputStations(new ArrayList<>());

        ValidationException exception = Assertions.assertThrows(ValidationException.class,
                () -> api.convertTrajectory(new HttpHeaders(), request));

        assertEquals(MISSING_MD_MESSAGE, exception.getMessage());
        Mockito.verify(crsTrajectoryConverter, Mockito.never()).convertTrajectoryV4(any(), any(), anyBoolean(), anyBoolean(), anyBoolean());
    }

    @Test
    public void convertTrajectory_interpolationWithNullInputStations_throwsValidationException() {
        for (MinimumDepthInterval interval : Arrays.asList(
                new MinimumDepthInterval(Arrays.asList(50.0), null),
                new MinimumDepthInterval(null, 25.0))) {
            CrsConverterApiV4 api = createTrajectoryApi();
            ConvertTrajectoryRequestV4 request = createMdiRequest(0.0, 100.0, interval);
            request.setInputStations(null);

            ValidationException exception = Assertions.assertThrows(ValidationException.class,
                    () -> api.convertTrajectory(new HttpHeaders(), request));

            assertEquals(MISSING_MD_MESSAGE, exception.getMessage());
        }
        Mockito.verifyNoInteractions(crsTrajectoryConverter);
    }

    @Test
    public void convertTrajectory_mdiWithNullLastStationMd_throwsValidationException() {
        CrsConverterApiV4 api = createTrajectoryApi();
        ConvertTrajectoryRequestV4 request = createMdiRequest(0.0, null,
                new MinimumDepthInterval(Arrays.asList(50.0), 0.0));

        ValidationException exception = Assertions.assertThrows(ValidationException.class,
                () -> api.convertTrajectory(new HttpHeaders(), request));

        assertEquals(MISSING_MD_MESSAGE, exception.getMessage());
        Mockito.verifyNoInteractions(crsTrajectoryConverter);
    }

    @Test
    public void convertTrajectory_withoutActiveInterpolation_allowsMissingStationMd() {
        for (MinimumDepthInterval interval : Arrays.asList(
                null,
                new MinimumDepthInterval(null, null),
                new MinimumDepthInterval(new ArrayList<>(), null),
                new MinimumDepthInterval(null, 0.0),
                new MinimumDepthInterval(new ArrayList<>(), 0.0),
                new MinimumDepthInterval(null, -25.0),
                new MinimumDepthInterval(new ArrayList<>(), -25.0))) {
            CrsConverterApiV4 api = createTrajectoryApi();
            ConvertTrajectoryRequestV4 request = createMdiRequest(null, null, interval);
            ConvertTrajectoryResponseV4 response = new ConvertTrajectoryResponseV4();
            Mockito.when(crsTrajectoryConverter.convertTrajectoryV4(any(), Mockito.same(request),
                    Mockito.eq(false), Mockito.eq(true), Mockito.eq(false))).thenReturn(response);

            Assertions.assertSame(response, api.convertTrajectory(new HttpHeaders(), request));

            if (interval != null) {
                Assertions.assertTrue(interval.getMd_i() == null || interval.getMd_i().isEmpty());
            }
        }
    }

    @Test
    public void convertTrajectory_mdiWithValidStationMd_preservesMdiAndConverts() {
        for (Double interval : Arrays.asList(null, 0.0, -25.0)) {
            CrsConverterApiV4 api = createTrajectoryApi();
            MinimumDepthInterval minimumDepthInterval = new MinimumDepthInterval(
                    Arrays.asList(0.0, 50.0, 100.0), interval);
            ConvertTrajectoryRequestV4 request = createMdiRequest(0.0, 100.0, minimumDepthInterval);
            ConvertTrajectoryResponseV4 response = new ConvertTrajectoryResponseV4();
            Mockito.when(crsTrajectoryConverter.convertTrajectoryV4(any(), Mockito.same(request),
                    Mockito.eq(false), Mockito.eq(true), Mockito.eq(false))).thenReturn(response);

            Assertions.assertSame(response, api.convertTrajectory(new HttpHeaders(), request));

            assertEquals(Arrays.asList(0.0, 50.0, 100.0), minimumDepthInterval.getMd_i());
        }
    }

    @Test
    public void convertTrajectory_mdIntervalWithEmptyMdi_expandsMdiAndConverts() {
        CrsConverterApiV4 api = createTrajectoryApi();
        MinimumDepthInterval minimumDepthInterval = new MinimumDepthInterval(new ArrayList<>(), 25.0);
        ConvertTrajectoryRequestV4 request = createMdiRequest(0.0, 100.0, minimumDepthInterval);
        ConvertTrajectoryResponseV4 response = new ConvertTrajectoryResponseV4();
        Mockito.when(crsTrajectoryConverter.convertTrajectoryV4(any(), Mockito.same(request),
                Mockito.eq(false), Mockito.eq(true), Mockito.eq(false))).thenReturn(response);

        Assertions.assertSame(response, api.convertTrajectory(new HttpHeaders(), request));

        assertEquals(Arrays.asList(0.0, 25.0, 50.0, 75.0, 100.0), minimumDepthInterval.getMd_i());
    }

    @Test
    public void convertTrajectory_mdIntervalWithValidStationMd_expandsMdiAndConverts() {
        CrsConverterApiV4 api = createTrajectoryApi();
        MinimumDepthInterval minimumDepthInterval = new MinimumDepthInterval(null, 25.0);
        Mockito.when(crsTrajectoryConverter.convertTrajectoryV4(any(), any(), anyBoolean(), anyBoolean(), anyBoolean()))
                .thenReturn(new ConvertTrajectoryResponseV4());

        api.convertTrajectory(new HttpHeaders(), createMdiRequest(0.0, 100.0, minimumDepthInterval));

        assertEquals(Arrays.asList(0.0, 25.0, 50.0, 75.0, 100.0), minimumDepthInterval.getMd_i());
        Mockito.verify(crsTrajectoryConverter).convertTrajectoryV4(any(), any(), anyBoolean(), anyBoolean(), anyBoolean());
    }

    @Test
    public void convertTrajectory_nullMdiValue_throwsValidationException() {
        CrsConverterApiV4 api = createTrajectoryApi();
        ConvertTrajectoryRequestV4 request = createMdiRequest(0.0, 100.0,
                new MinimumDepthInterval(Arrays.asList(50.0, null), null));

        ValidationException exception = Assertions.assertThrows(ValidationException.class,
                () -> api.convertTrajectory(new HttpHeaders(), request));

        assertEquals("md_i array must not contain null values.", exception.getMessage());
        Mockito.verifyNoInteractions(crsTrajectoryConverter);
    }

    @Test
    public void convertTrajectory_nullInputStation_throwsValidationException() {
        CrsConverterApiV4 api = createTrajectoryApi();
        ConvertTrajectoryRequestV4 request = createMdiRequest(0.0, 100.0, null);
        request.setInputStations(Arrays.asList(null, createStation(100.0)));

        ValidationException exception = Assertions.assertThrows(ValidationException.class,
                () -> api.convertTrajectory(new HttpHeaders(), request));

        assertEquals("inputStations must not contain null stations.", exception.getMessage());
        Mockito.verifyNoInteractions(crsTrajectoryConverter);
    }

    @Test
    public void convertTrajectory_interpolateFalse_ignoresMdi() {
        CrsConverterApiV4 api = createTrajectoryApi();
        MinimumDepthInterval interval = new MinimumDepthInterval(null, 25.0);
        ConvertTrajectoryRequestV4 request = createMdiRequest(null, null, interval);
        request.setInterpolate(false);
        ConvertTrajectoryResponseV4 response = new ConvertTrajectoryResponseV4();
        Mockito.when(crsTrajectoryConverter.convertTrajectoryV4(any(), Mockito.same(request),
                anyBoolean(), anyBoolean(), anyBoolean())).thenReturn(response);

        Assertions.assertSame(response, api.convertTrajectory(new HttpHeaders(), request));
        Assertions.assertNull(interval.getMd_i());
    }

    @Test
    public void convertTrajectory_displacementsWithMdi_isNotRejectedForMissingStationMd() {
        CrsConverterApiV4 api = createTrajectoryApi();
        ConvertTrajectoryRequestV4 request = createMdiRequest(null, null,
                new MinimumDepthInterval(Arrays.asList(50.0), null));
        request.setInputKind(Constants.DX_DY_DZ);
        Mockito.when(crsTrajectoryConverter.populateMdInclAziFromRequestV4ForInverseMinimumCurvature(request))
                .thenReturn(Arrays.asList(createStation(0.0), createStation(100.0)));
        ConvertTrajectoryResponseV4 response = new ConvertTrajectoryResponseV4();
        response.setStations(new ArrayList<>());
        response.setOperationsApplied(new ArrayList<>());
        Mockito.when(crsTrajectoryConverter.convertTrajectoryV4(any(), Mockito.same(request),
                anyBoolean(), anyBoolean(), anyBoolean())).thenReturn(response);

        Assertions.assertSame(response, api.convertTrajectory(new HttpHeaders(), request));
    }

    private CrsConverterApiV4 createTrajectoryApi() {
        CrsConverterApiV4 api = Mockito.spy(new CrsConverterApiV4(crsTrajectoryConverter,
                Mockito.mock(IPointConverter.class), Mockito.mock(ICRSConverter.class)));
        ReflectionTestUtils.setField(api, "logger", Mockito.mock(JaxRsDpsLog.class));
        Mockito.doReturn(false).when(api).checkCRSType(anyString());
        Mockito.doReturn("PersistableReference").when(api).getPersistableReferenceFromID(anyString(), anyBoolean());
        return api;
    }

    private ConvertTrajectoryRequestV4 createMdiRequest(Double firstMd, Double lastMd, MinimumDepthInterval minimumDepthInterval) {
        ConvertTrajectoryRequestV4 request = new ConvertTrajectoryRequestV4();
        request.setTrajectoryCRS("trajectoryCRS");
        request.setUnitZ("unitZ");
        request.setInputKind(Constants.MD_INCL_AZIM);
        request.setInputStations(Arrays.asList(createStation(firstMd), createStation(lastMd)));
        request.setMD_i(minimumDepthInterval);
        return request;
    }

    private TrajectoryStationInV4 createStation(Double md) {
        TrajectoryStationInV4 station = new TrajectoryStationInV4();
        station.setMd(md);
        return station;
    }

}
