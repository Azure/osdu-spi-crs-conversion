package org.opengroup.osdu.crs.swagger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opengroup.osdu.core.common.info.VersionInfoBuilder;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.crs.api.CrsConverterApiV4;
import org.opengroup.osdu.crs.api.InfoApi;
import org.opengroup.osdu.crs.interfaces.ICRSConverter;
import org.opengroup.osdu.crs.interfaces.IPointConverter;
import org.opengroup.osdu.crs.interfaces.ITrajectoryConverter;
import org.opengroup.osdu.crs.osducoreserviceclient.storage.IStorageClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = InfoOpenApiTest.Application.class,
    properties = "springdoc.api-docs.path=/api-docs")
@AutoConfigureMockMvc(addFilters = false)
@MockitoBean(types = {
    VersionInfoBuilder.class, JaxRsDpsLog.class, IStorageClient.class,
    ITrajectoryConverter.class, IPointConverter.class, ICRSConverter.class
})
public class InfoOpenApiTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  public void shouldDescribeInfoAsPublicAndTenantIndependentInEveryVersion() throws Exception {
    for (String version : new String[] {"v2", "v3", "v4"}) {
      mockMvc.perform(get("/api-docs/" + version))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.paths['/" + version + "/info'].get.security").isEmpty())
          .andExpect(jsonPath("$.paths['/" + version + "/info'].get.parameters").doesNotExist())
          .andExpect(jsonPath("$.security[0].Authorization").isArray());
    }
  }

  @Test
  public void shouldKeepTenantHeaderAndBearerSchemeForConversionOperations() throws Exception {
    mockMvc.perform(get("/api-docs/v4"))
        .andExpect(status().isOk())
        .andExpect(jsonPath(
            "$.paths['/v4/convert'].post.parameters[?(@.name == 'data-partition-id')].required")
            .value(org.hamcrest.Matchers.contains(true)))
        .andExpect(jsonPath("$.security[0].Authorization").isArray())
        .andExpect(jsonPath("$.components.securitySchemes.Authorization.scheme").value("bearer"));
  }

  @SpringBootConfiguration
  @EnableAutoConfiguration
  @Import({
      InfoApi.class, CrsConverterApiV4.class,
      SwaggerConfiguration.class, SwaggerConfigurationProperties.class
  })
  static class Application {
  }
}
