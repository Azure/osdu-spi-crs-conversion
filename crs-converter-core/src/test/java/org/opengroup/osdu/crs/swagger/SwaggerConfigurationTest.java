package org.opengroup.osdu.crs.swagger;

import io.swagger.v3.oas.models.Operation;
import org.junit.Test;
import org.opengroup.osdu.crs.api.HealthCheck;
import org.opengroup.osdu.crs.api.InfoApi;
import org.opengroup.osdu.crs.api.CrsConverterApiV2;
import org.opengroup.osdu.crs.model.ConvertPointsRequest;
import org.springframework.web.method.HandlerMethod;

import static org.junit.Assert.*;

public class SwaggerConfigurationTest {
    @Test
    public void publicEndpointsOverrideGlobalSecurityAndNeedNoPartition() throws Exception {
        assertPublicEndpoint(new InfoApi(), "info");
        assertPublicEndpoint(new HealthCheck(), "livenessCheck");
        assertPublicEndpoint(new HealthCheck(), "readinessCheck");
    }

    private void assertPublicEndpoint(Object controller, String method) throws Exception {
        Operation operation = new Operation();
        operation.addSecurityItem(new io.swagger.v3.oas.models.security.SecurityRequirement().addList("Authorization"));
        new SwaggerConfiguration().operationCustomizer().customize(operation,
                new HandlerMethod(controller, controller.getClass().getMethod(method)));
        assertNotNull(operation.getSecurity());
        assertTrue(operation.getSecurity().isEmpty());
        assertNull(operation.getParameters());
    }

    @Test
    public void conversionStillRequiresPartitionAndKeepsSecurity() throws Exception {
        Operation operation = new Operation();
        operation.addSecurityItem(new io.swagger.v3.oas.models.security.SecurityRequirement().addList("Authorization"));
        new SwaggerConfiguration().operationCustomizer().customize(operation,
                new HandlerMethod(new CrsConverterApiV2(null, null, null),
                        CrsConverterApiV2.class.getMethod("convertPoint", ConvertPointsRequest.class)));
        assertEquals(1, operation.getSecurity().size());
        assertEquals("data-partition-id", operation.getParameters().get(0).getName());
        assertTrue(operation.getParameters().get(0).getRequired());
    }
}
