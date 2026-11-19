/*
Copyright 2017-2026, The Open Group

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.opengroup.osdu.crs.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.core.common.http.HttpConfiguration;
import org.opengroup.osdu.core.common.info.VersionInfoBuilder;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.info.VersionInfo;
import org.opengroup.osdu.core.common.model.storage.Record;
import org.opengroup.osdu.crs.converter.CRSConverter;
import org.opengroup.osdu.crs.converter.PointConverter;
import org.opengroup.osdu.crs.converter.TrajectoryConverter;
import org.opengroup.osdu.crs.middleware.RestExceptionHandler;
import org.opengroup.osdu.crs.osducoreserviceclient.storage.IStorageClient;
import org.opengroup.osdu.crs.util.ConstantsTests;
import org.opengroup.osdu.crs.util.RecordIdNormalizer;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Spring Boot 4 reads and writes the API with the Jackson 3 mapper from os-core-common's {@link HttpConfiguration};
 * before, it was the Jackson 2 mapper configured below. This sends the same requests through the real v2, v3 and v4
 * controllers, converters and exception handler with each mapper and fails on any difference in status or content.
 * Property order is not compared: Jackson 3 sorts alphabetically and JSON objects are unordered.
 */
@SuppressWarnings("removal")
class Jackson3ContractParityTest {

    private static final ObjectMapper JACKSON2 = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .setTimeZone(TimeZone.getDefault());

    private static final String CRS_ID = "test:reference-data--CoordinateReferenceSystem:Projected:EPSG::32611";
    private static final Path ACCEPTANCE_DATA = Path.of("..", "crs-converter_acceptance_test");

    @Test
    void responsesShouldHaveTheSameStatusAndContent() throws Exception {
        MockMvc jackson2 = mvc(new MappingJackson2HttpMessageConverter(JACKSON2));
        MockMvc jackson3 = mvc(new JacksonJsonHttpMessageConverter(new HttpConfiguration().jsonObjectMapper()));

        Map<String, RequestBuilder> requests = requests();
        List<String> differences = new ArrayList<>();
        int successful = 0;
        for (Map.Entry<String, RequestBuilder> request : requests.entrySet()) {
            MvcResult expected = jackson2.perform(request.getValue()).andReturn();
            MvcResult actual = jackson3.perform(request.getValue()).andReturn();
            String expectedBody = expected.getResponse().getContentAsString();
            String actualBody = actual.getResponse().getContentAsString();
            if (expected.getResponse().getStatus() == 200) {
                successful++;
            }
            JsonNode expectedJson = withoutTimestamps(JACKSON2.readTree(expectedBody));
            JsonNode actualJson = withoutTimestamps(JACKSON2.readTree(actualBody));
            if (expected.getResponse().getStatus() != actual.getResponse().getStatus()
                    || !expectedJson.equals(actualJson)) {
                List<String> paths = new ArrayList<>();
                diff("$", expectedJson, actualJson, paths);
                differences.add(request.getKey()
                        + "\n  jackson2: " + expected.getResponse().getStatus() + " " + abbreviate(expectedBody)
                        + "\n  jackson3: " + actual.getResponse().getStatus() + " " + abbreviate(actualBody)
                        + "\n  differences: " + paths.subList(0, Math.min(paths.size(), 20)));
            }
        }
        assertEquals(List.of(), differences, String.join("\n", differences));
        assertTrue(successful >= 12, "Expected most requests to convert successfully, got " + successful);
    }

    @Test
    void trailingContentAfterTheBodyIsNowRejected() throws Exception {
        String convert = "{\"fromCRS\":" + quote(ConstantsTests.LB_WGS84[ConstantsTests.V2])
                + ",\"toCRS\":" + quote(ConstantsTests.LB_WGS84_UTM11N[ConstantsTests.V2])
                + ",\"points\":[{\"x\":-117.0,\"y\":36.0,\"z\":10.0}]}";
        RequestBuilder request = json("/v3/convert", convert + "}");

        // Intentional change: Jackson 3 enables FAIL_ON_TRAILING_TOKENS, Jackson 2 ignored the extra "}"
        assertEquals(200, mvc(new MappingJackson2HttpMessageConverter(JACKSON2)).perform(request).andReturn()
                .getResponse().getStatus());
        assertEquals(400, mvc(new JacksonJsonHttpMessageConverter(new HttpConfiguration().jsonObjectMapper()))
                .perform(request).andReturn().getResponse().getStatus());
    }

    private static MockMvc mvc(HttpMessageConverter<?> json) {
        CRSConverter crsConverter = new CRSConverter();
        ReflectionTestUtils.setField(crsConverter, "logger", mock(JaxRsDpsLog.class));
        TrajectoryConverter trajectoryConverter = new TrajectoryConverter();
        PointConverter pointConverter = new PointConverter();
        IStorageClient storage = mock(IStorageClient.class);
        Record crsRecord = mock(Record.class);
        when(crsRecord.getData()).thenReturn(Map.of(
                "PersistableReference", ConstantsTests.LB_WGS84_UTM11N[ConstantsTests.V1]));
        when(storage.getRecord(RecordIdNormalizer.normalizeRecordID(CRS_ID))).thenReturn(crsRecord);

        CrsConverterApiV2 v2 = new CrsConverterApiV2(crsConverter, trajectoryConverter, pointConverter);
        ReflectionTestUtils.setField(v2, "logger", mock(JaxRsDpsLog.class));
        CrsConverterApiV3 v3 = new CrsConverterApiV3(crsConverter, trajectoryConverter, pointConverter);
        ReflectionTestUtils.setField(v3, "logger", mock(JaxRsDpsLog.class));
        ReflectionTestUtils.setField(v3, "StorageClient", storage);
        CrsConverterApiV4 v4 = new CrsConverterApiV4(trajectoryConverter, pointConverter, crsConverter);
        ReflectionTestUtils.setField(v4, "logger", mock(JaxRsDpsLog.class));
        ReflectionTestUtils.setField(v4, "storageClient", storage);
        InfoApi info = new InfoApi();
        VersionInfoBuilder versionInfoBuilder = mock(VersionInfoBuilder.class);
        try {
            when(versionInfoBuilder.buildVersionInfo()).thenReturn(VersionInfo.builder().groupId("group")
                    .artifactId("artifact").version("0.1.0").buildTime("1000").branch("master").commitId("7777")
                    .commitMessage("Test commit").build());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        ReflectionTestUtils.setField(info, "versionInfoBuilder", versionInfoBuilder);
        RestExceptionHandler handler = new RestExceptionHandler();
        ReflectionTestUtils.setField(handler, "jaxRsDpsLog", mock(JaxRsDpsLog.class));

        return MockMvcBuilders.standaloneSetup(v2, v3, v4, info)
                .setControllerAdvice(handler)
                .setMessageConverters(new StringHttpMessageConverter(), json)
                .build();
    }

    private static Map<String, RequestBuilder> requests() throws Exception {
        String points = "[{\"x\":-117.0,\"y\":36.0,\"z\":10.0},{\"x\":-116.5,\"y\":36.5,\"z\":0.0}]";
        String convert = "{\"fromCRS\":" + quote(ConstantsTests.LB_WGS84[ConstantsTests.V2])
                + ",\"toCRS\":" + quote(ConstantsTests.LB_WGS84_UTM11N[ConstantsTests.V2])
                + ",\"points\":" + points + ",\"unknown\":1}";
        String convertLegacyReference = "{\"fromCRS\":" + quote(ConstantsTests.LB_WGS84[ConstantsTests.V1])
                + ",\"toCRS\":" + quote(ConstantsTests.EB_NAD83_UTM11N_1702[ConstantsTests.V1])
                + ",\"points\":" + points + "}";
        String convertOutOfRange = "{\"fromCRS\":" + quote(ConstantsTests.LB_WGS84_UTM11N[ConstantsTests.V2])
                + ",\"toCRS\":" + quote(ConstantsTests.LB_WGS84[ConstantsTests.V2])
                + ",\"points\":[{\"x\":500000.0,\"y\":4000000.0},{\"x\":1.0E30,\"y\":1.0E30}]}";
        String geoJson = "{\"featureCollection\":" + ConstantsTests.GEO_JSON_02
                + ",\"toCRS\":" + quote(ConstantsTests.EB_ED5023031024)
                + ",\"toUnitZ\":" + quote(ConstantsTests.UNIT_FT_v2) + "}";
        String anyCrsGeoJson = "{\"featureCollection\":" + ConstantsTests.GEO_JSON_03
                + ",\"toCRS\":" + quote(ConstantsTests.LB_WGS84[ConstantsTests.V2])
                + ",\"toUnitZ\":" + quote(ConstantsTests.UNIT_FT) + "}";
        String trajectoryLmp = acceptanceData("v2/data/Trajectory/32631-LMP-request.json");
        String trajectoryTrueNorth = acceptanceData("v2/data/Trajectory/32613-TN-request.json");

        Map<String, RequestBuilder> requests = new LinkedHashMap<>();
        for (String version : List.of("v2", "v3")) {
            requests.put(version + " convert", json("/" + version + "/convert", convert));
            requests.put(version + " convert legacy reference", json("/" + version + "/convert", convertLegacyReference));
            requests.put(version + " convert out of range without z", json("/" + version + "/convert", convertOutOfRange));
            requests.put(version + " convertGeoJson", json("/" + version + "/convertGeoJson", geoJson));
            requests.put(version + " convertGeoJson AnyCrs", json("/" + version + "/convertGeoJson", anyCrsGeoJson));
            requests.put(version + " convertTrajectory LMP", json("/" + version + "/convertTrajectory", trajectoryLmp));
            requests.put(version + " convertTrajectory TN", json("/" + version + "/convertTrajectory", trajectoryTrueNorth));
            requests.put(version + " info", get("/" + version + "/info"));
        }
        requests.put("v3 convertBinGrid", json("/v3/convertBinGrid", binGrid(null)).header("data-partition-id", "test"));
        requests.put("v3 convertBinGrid toCRS", json("/v3/convertBinGrid", binGrid(CRS_ID))
                .header("data-partition-id", "test"));
        requests.put("v4 convert", json("/v4/convert", convert));
        requests.put("v4 convertGeoJson", json("/v4/convertGeoJson", geoJson));
        requests.put("v4 convertTrajectory", json("/v4/convertTrajectory", trajectoryV4("\"md_i\":[50.0,150.0]", true)));
        requests.put("v4 convertTrajectory interval", json("/v4/convertTrajectory", trajectoryV4("\"md_interval\":25.0", true)));
        requests.put("v4 convertTrajectory no interpolation", json("/v4/convertTrajectory", trajectoryV4("\"md_i\":[50.0]", false)));
        requests.put("v4 info", get("/v4/info"));

        requests.put("error malformed body", json("/v4/convert", "{\"fromCRS\":"));
        requests.put("error validation", json("/v3/convert", "{\"fromCRS\":\"x\"}"));
        requests.put("error invalid CRS", json("/v3/convert", "{\"fromCRS\":\"{}\",\"toCRS\":\"{}\",\"points\":" + points + "}"));
        requests.put("error trajectory md", json("/v4/convertTrajectory", trajectoryV4("\"md_i\":[50.0],\"md_interval\":25.0", true)));
        requests.put("error bin grid size", json("/v3/convertBinGrid", "{\"inBinGrid\":{\"ABCDBinGridSpatialLocation\":"
                + "{\"AsIngestedCoordinates\":{\"type\":\"AnyCrsFeatureCollection\",\"features\":[]}}}}")
                .header("data-partition-id", "test"));
        requests.put("error method", get("/v4/convert"));
        return requests;
    }

    private static String trajectoryV4(String depths, boolean interpolate) {
        String crs = "{\"wkt\":\"PROJCS[\\\"WGS_1984_UTM_Zone_31N\\\",GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\","
                + "SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\","
                + "0.0174532925199433]],PROJECTION[\\\"Transverse_Mercator\\\"],PARAMETER[\\\"False_Easting\\\",500000.0],"
                + "PARAMETER[\\\"False_Northing\\\",0.0],PARAMETER[\\\"Central_Meridian\\\",3.0],PARAMETER[\\\"Scale_Factor\\\","
                + "0.9996],PARAMETER[\\\"Latitude_Of_Origin\\\",0.0],UNIT[\\\"Meter\\\",1.0],AUTHORITY[\\\"EPSG\\\",32631]]\","
                + "\"ver\":\"PE_10_3_1\",\"name\":\"WGS_1984_UTM_Zone_31N\",\"authCode\":{\"auth\":\"EPSG\",\"code\":\"32631\"},"
                + "\"type\":\"LBC\"}";
        String unit = "{\"scaleOffset\":{\"scale\":1.0,\"offset\":0.0},\"symbol\":\"m\","
                + "\"baseMeasurement\":{\"ancestry\":\"Length\",\"type\":\"UM\"},\"type\":\"USO\"}";
        return "{\"trajectoryCRS\":" + quote(crs) + ",\"unitXY\":" + quote(unit) + ",\"unitZ\":" + quote(unit)
                + ",\"azimuthReference\":\"GN\",\"referencePoint\":{\"x\":400000.0,\"y\":6500000.0,\"z\":0.0}"
                + ",\"method\":\"AzimuthalEquidistant\",\"inputKind\":\"MD_Incl_Azim\",\"interpolate\":" + interpolate
                + ",\"inputStations\":[{\"md\":0.0,\"inclination\":0.0,\"azimuth\":0.0},"
                + "{\"md\":100.0,\"inclination\":10.0,\"azimuth\":45.0},{\"md\":200.0,\"inclination\":20.0,\"azimuth\":45.0}]"
                + ",\"MD_i\":{" + depths + "}}";
    }

    private static String binGrid(String toCrs) {
        return "{" + (toCrs == null ? "" : "\"toCRS\":" + quote(toCrs) + ",")
                + "\"inBinGrid\":{\"P6ScaleFactorOfBinGrid\":1,\"P6BinNodeIncrementOnIaxis\":1,"
                + "\"P6BinNodeIncrementOnJaxis\":1,\"ABCDBinGridSpatialLocation\":{\"AsIngestedCoordinates\":{"
                + "\"type\":\"AnyCrsFeatureCollection\",\"CoordinateReferenceSystemID\":" + quote(CRS_ID) + ",\"features\":["
                + feature(1, 1000, 500000, 4000000) + "," + feature(1, 2000, 500000, 4000100) + ","
                + feature(101, 1000, 500100, 4000000) + "," + feature(101, 2000, 500100, 4000100) + "]}}}}";
    }

    private static String feature(int inline, int crossline, int x, int y) {
        return "{\"type\":\"AnyCrsFeature\",\"properties\":{\"PointProperties\":[{\"Inline\":" + inline
                + ",\"Crossline\":" + crossline + "}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":["
                + x + "," + y + "]}}";
    }

    private static org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder json(
            String path, String body) {
        return post(path).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(body);
    }

    private static String acceptanceData(String file) throws Exception {
        return Files.readString(ACCEPTANCE_DATA.resolve(file));
    }

    private static String quote(String value) {
        try {
            return JACKSON2.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    // The bin grid response stamps the conversion time
    private static JsonNode withoutTimestamps(JsonNode node) {
        if (node instanceof ObjectNode object) {
            object.remove(List.of("SpatialLocationCoordinatesDate", "CoordinateQualityCheckDateTime"));
        }
        node.forEach(Jackson3ContractParityTest::withoutTimestamps);
        return node;
    }

    private static void diff(String path, JsonNode expected, JsonNode actual, List<String> out) {
        if (expected == null || actual == null || expected.isObject() != actual.isObject()
                || expected.isArray() != actual.isArray()) {
            out.add(path + ": " + abbreviate(String.valueOf(expected)) + " -> " + abbreviate(String.valueOf(actual)));
        } else if (expected.isObject()) {
            Set<String> names = new TreeSet<>();
            expected.fieldNames().forEachRemaining(names::add);
            actual.fieldNames().forEachRemaining(names::add);
            for (String name : names) {
                if (!Objects.equals(expected.get(name), actual.get(name))) {
                    diff(path + "." + name, expected.get(name), actual.get(name), out);
                }
            }
        } else if (expected.isArray() && expected.size() == actual.size()) {
            for (int i = 0; i < expected.size(); i++) {
                if (!expected.get(i).equals(actual.get(i))) {
                    diff(path + "[" + i + "]", expected.get(i), actual.get(i), out);
                }
            }
        } else if (!expected.equals(actual)) {
            out.add(path + ": " + abbreviate(expected.toString()) + " -> " + abbreviate(actual.toString()));
        }
    }

    private static String abbreviate(String json) {
        return json.length() > 300 ? json.substring(0, 300) + "..." : json;
    }
}
