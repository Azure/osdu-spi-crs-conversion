package org.opengroup.osdu.crs.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Arrays;

@Configuration
@Profile("!noswagger")
public class SwaggerConfiguration {
    
    @Autowired
    private SwaggerConfigurationProperties configurationProperties;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "Authorization";
        OpenAPI openAPI = new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components().addSecuritySchemes(securitySchemeName, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("Authorization")
                        .in(SecurityScheme.In.HEADER)
                        .name("Authorization")))
                .info(apiInfo());
        if (configurationProperties.isApiServerFullUrlEnabled())
            return openAPI;
        return openAPI
                .servers(Arrays.asList(new Server().url("/api/crs/converter/")));

    }

    @Bean
    public OperationCustomizer operationCustomizer() {
        return (operation, handlerMethod) -> {
            Parameter dataPartitionId = new Parameter()
                    .name(DpsHeaders.DATA_PARTITION_ID)
                    .description("Tenant Id")
                    .in("header") // Set the 'in' field to 'header'
                    .required(true)
                    .schema(new StringSchema());
            operation.addParametersItem(dataPartitionId);
            return operation;
        };
    }

    @Bean
    public GroupedOpenApi apiV2() {
        String[] paths = {"/v2/**"};
        return GroupedOpenApi.builder()
                .group("v2")
                .pathsToMatch(paths)
                .addOpenApiCustomizer(buildV2OpenAPI())
                .addOperationCustomizer(operationCustomizer())
                .build();
    }

    @Bean
    public GroupedOpenApi apiV3() {
        String[] paths = {"/v3/**"};
        return GroupedOpenApi.builder()
                .group("v3")
                .pathsToMatch(paths)
                .addOpenApiCustomizer(buildV3OpenAPI())
                .addOperationCustomizer(operationCustomizer())
                .build();
    }

    @Bean
    public GroupedOpenApi apiV4() {
        String[] paths = {"/v4/**"};
        return GroupedOpenApi.builder()
                .group("v4")
                .pathsToMatch(paths)
                .addOpenApiCustomizer(buildV4OpenAPI())
                .addOperationCustomizer(operationCustomizer())
                .build();
    }

    public OpenApiCustomizer buildV2OpenAPI() {
        return openApi -> {
            openApi.info(openApi.getInfo().version("2.0.0"));
            openApi.addTagsItem(new Tag().name("health-check-api").description("Health related endpoints"));
            openApi.addTagsItem(new Tag().name("info-api").description("Version info endpoint"));
            openApi.addTagsItem(new Tag().name("crs-converter-api-v2").description("Converter related endpoints"));
            openApi.addTagsItem(new Tag().name("convert-trajectory-api-v2").description("Convert trajectory stations"));
        };
    }

    public OpenApiCustomizer buildV3OpenAPI() {
        return openApi -> {
            openApi.info(openApi.getInfo().version("3.0.0"));
            openApi.addTagsItem(new Tag().name("health-check-api").description("Health related endpoints"));
            openApi.addTagsItem(new Tag().name("info-api").description("Version info endpoint"));
            openApi.addTagsItem(new Tag().name("crs-converter-api-v3").description("Converter related endpoints"));
            openApi.addTagsItem(new Tag().name("convert-trajectory-api-v3").description("Convert trajectory stations"));

        };
    }

    public OpenApiCustomizer buildV4OpenAPI() {
        return openApi -> {
            openApi.info(openApi.getInfo().version("4.0.0"));
            openApi.addTagsItem(new Tag().name("info-api").description("Version info endpoint"));
            openApi.addTagsItem(new Tag().name("Convert").description("Coordinate conversion endpoints"));

        };
    }
    
    // Describe the apis
    private Info apiInfo() {
        return new Info()
                .title(configurationProperties.getApiTitle())
                .description(configurationProperties.getApiDescription())
                .license(new License().name(configurationProperties.getApiLicenseName()).url(configurationProperties.getApiLicenseUrl()))
                .contact(new Contact().name(configurationProperties.getApiContactName()).email(configurationProperties.getApiContactEmail()));
    }
}
