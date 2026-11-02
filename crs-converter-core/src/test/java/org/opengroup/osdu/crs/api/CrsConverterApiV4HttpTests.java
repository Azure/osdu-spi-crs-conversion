package org.opengroup.osdu.crs.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.crs.converter.PointConverter;
import org.opengroup.osdu.crs.interfaces.ICRSConverter;
import org.opengroup.osdu.crs.interfaces.ITrajectoryConverter;
import org.opengroup.osdu.crs.middleware.RestExceptionHandler;
import org.opengroup.osdu.crs.model.v4.ConvertTrajectoryResponseV4;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CrsConverterApiV4HttpTests {
    private ITrajectoryConverter trajectory;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        trajectory = mock(ITrajectoryConverter.class);
        CrsConverterApiV4 api = new CrsConverterApiV4(trajectory, new PointConverter(), mock(ICRSConverter.class));
        ReflectionTestUtils.setField(api, "logger", mock(JaxRsDpsLog.class));
        RestExceptionHandler handler = new RestExceptionHandler();
        ReflectionTestUtils.setField(handler, "jaxRsDpsLog", mock(JaxRsDpsLog.class));
        mvc = MockMvcBuilders.standaloneSetup(api).setControllerAdvice(handler).build();
    }

    @ParameterizedTest
    @CsvSource({"true,true", "true,false", "false,true", "false,false"})
    void missingEndpointMdReturns400RatherThan500(boolean explicitDepths, boolean firstMissing) throws Exception {
        String depths = explicitDepths ? "\"md_i\":[50.0]" : "\"md_interval\":25.0";
        String stations = firstMissing ? "[{}, {\"md\":100.0}]" : "[{\"md\":0.0}, {}]";

        mvc.perform(post("/v4/convertTrajectory").contentType(MediaType.APPLICATION_JSON)
                        .content(request(stations, depths)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(
                        "The first and last inputStations must include an md value when md_i or md_interval is provided."));

        verifyNoInteractions(trajectory);
    }

    @Test
    void validIntervalExpandsDepthsBeforeConversion() throws Exception {
        when(trajectory.convertTrajectoryV4(any(), argThat(r -> r.getMD_i().getMd_i()
                        .equals(Arrays.asList(0.0, 25.0, 50.0, 75.0, 100.0))),
                eq(true), eq(true), eq(false))).thenReturn(new ConvertTrajectoryResponseV4());

        mvc.perform(post("/v4/convertTrajectory").contentType(MediaType.APPLICATION_JSON)
                        .content(request("[{\"md\":0.0}, {\"md\":100.0}]", "\"md_interval\":25.0")))
                .andExpect(status().isOk());

        verify(trajectory).convertTrajectoryV4(any(), any(), eq(true), eq(true), eq(false));
    }

    private String request(String stations, String depths) {
        return "{\"trajectoryCRS\":\"{\\\"type\\\":\\\"LBC\\\"}\","
                + "\"unitXY\":\"{\\\"type\\\":\\\"USO\\\"}\","
                + "\"unitZ\":\"{\\\"type\\\":\\\"USO\\\"}\","
                + "\"method\":\"AzimuthalEquidistant\",\"inputKind\":\"Md_Incl_Azim\","
                + "\"interpolate\":true,\"inputStations\":" + stations + ",\"MD_i\":{" + depths + "}}";
    }
}
