package org.opengroup.osdu.crs.swagger;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.crs.api.HealthCheck;
import org.opengroup.osdu.crs.api.InfoApi;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
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
    public GlobalOpenApiCustomizer nullableBoundingBoxes() {
        return SwaggerConfiguration::allowNullBoundingBox;
    }

    @Bean
    public GlobalOpenApiCustomizer computedPointResponses() {
        return SwaggerConfiguration::useComputedPointSchema;
    }

    @Bean
    public GlobalOpenApiCustomizer nonFiniteVerticalDepth() {
        return SwaggerConfiguration::allowNonFiniteVerticalDepth;
    }

    /**
     * Jackson serializes {@code dZ} as {@code dz}, and springdoc then emits a bare double.
     * The other station displacements already allow the string {@code NaN}.
     */
    static void allowNonFiniteVerticalDepth(OpenAPI openApi) {
        if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) {
            return;
        }
        Schema<?> station = openApi.getComponents().getSchemas().get("TrajectoryStationOut");
        if (station == null || station.getProperties() == null) {
            return;
        }
        Object vertical = station.getProperties().get("dz");
        if (!(vertical instanceof Schema<?> depth) || depth.getOneOf() != null) {
            return;
        }
        depth.addType("number");
        depth.addType("string");
        depth.setFormat("double");
        depth.setDescription(org.opengroup.osdu.crs.util.Constants.SWAGGER_DZ);
        depth.setExample(org.opengroup.osdu.crs.util.Constants.SWAGGER_DZ_EXAMPLE);
        Schema<Object> number = new Schema<>();
        number.addType("number");
        number.setFormat("double");
        Schema<Object> marker = new Schema<>();
        marker.addType("string");
        marker.setEnum(Arrays.asList("NaN", "Infinity", "-Infinity"));
        depth.setOneOf(Arrays.asList(number, marker));
    }

    /**
     * Failed conversions are returned as the string {@code NaN}. Springdoc sometimes resolves
     * the response array from the Java field type {@code Point}, which only allows numbers.
     * The published contract uses {@code PointResponse}.
     */
    static void useComputedPointSchema(OpenAPI openApi) {
        if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) {
            return;
        }
        java.util.Map<String, Schema> schemas = openApi.getComponents().getSchemas();
        if (!schemas.containsKey("PointResponse")) {
            ModelConverters.getInstance(true).readAll(PointResponse.class).forEach(schemas::putIfAbsent);
        }
        for (String name : Arrays.asList("ConvertPointsResponse", "ConvertPointsResponseV4")) {
            Schema<?> schema = schemas.get(name);
            if (schema == null || schema.getProperties() == null) {
                continue;
            }
            Object points = schema.getProperties().get("points");
            if (!(points instanceof Schema<?> pointsSchema)) {
                continue;
            }
            Schema<Object> items = new Schema<>();
            items.set$ref("#/components/schemas/PointResponse");
            pointsSchema.setItems(items);
        }
    }

    /**
     * GeoJSON bounding boxes are optional. An explicit JSON null is the same as omitting the member,
     * so the published contract must allow null or contract tests treat a successful response as a violation.
     */
    static void allowNullBoundingBox(OpenAPI openApi) {
        if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) {
            return;
        }
        for (Schema<?> schema : openApi.getComponents().getSchemas().values()) {
            markBoundingBoxNullable(schema);
        }
    }

    private static void markBoundingBoxNullable(Schema<?> schema) {
        if (schema == null) {
            return;
        }
        if (schema.getProperties() != null) {
            Object bbox = schema.getProperties().get("bbox");
            if (bbox instanceof Schema<?> bboxSchema
                    && (bboxSchema.getTypes() == null || !bboxSchema.getTypes().contains("null"))) {
                bboxSchema.addType("array");
                bboxSchema.addType("null");
            }
        }
        if (schema.getAllOf() != null) {
            for (Schema<?> part : schema.getAllOf()) {
                markBoundingBoxNullable(part);
            }
        }
    }

    @Bean
    public GlobalOpenApiCustomizer exclusiveMeasuredDepthInterpolation() {
        return SwaggerConfiguration::forbidBothMeasuredDepthInputs;
    }

    /**
     * Interpolation takes an explicit depth list or a regular interval.
     * The service rejects a request that supplies both, so the contract must too.
     */
    static void forbidBothMeasuredDepthInputs(OpenAPI openApi) {
        if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) {
            return;
        }
        Schema<?> interval = openApi.getComponents().getSchemas().get("MinimumDepthInterval");
        if (interval == null) {
            return;
        }
        Schema<Object> both = new Schema<>();
        both.setRequired(Arrays.asList("md_i", "md_interval"));
        interval.setNot(both);
    }

    @Bean
    public GlobalOpenApiCustomizer trajectoryGeographicBounds() {
        return SwaggerConfiguration::applyTrajectoryRequestConstraints;
    }

    static final String PERSISTABLE_CRS_PATTERN = "(^\\s*\\{)|(\\{\")";

    /**
     * Geographic reference points stay inside longitude and latitude limits.
     * Inclination-only surveys cannot also carry azimuth, and deviation-only surveys
     * cannot carry azimuth, inclination, or measured depth. The service rejects both.
     * A persistable-reference trajectory CRS must include unitXY.
     */
    static void applyTrajectoryRequestConstraints(OpenAPI openApi) {
        if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) {
            return;
        }
        for (String name : Arrays.asList("ConvertTrajectoryRequest", "ConvertTrajectoryRequestV4")) {
            Schema<?> request = openApi.getComponents().getSchemas().get(name);
            if (request == null) {
                continue;
            }
            Schema<Object> condition = new Schema<>();
            condition.addProperty("trajectoryCRS", new StringSchema()
                    .pattern(org.opengroup.osdu.crs.util.Constants.SWAGGER_GEOGRAPHIC_CRS_PATTERN));
            Schema<Object> referencePoint = new Schema<>();
            referencePoint.addProperty("x", new io.swagger.v3.oas.models.media.NumberSchema()
                    .minimum(java.math.BigDecimal.valueOf(-180)).maximum(java.math.BigDecimal.valueOf(180)));
            referencePoint.addProperty("y", new io.swagger.v3.oas.models.media.NumberSchema()
                    .minimum(java.math.BigDecimal.valueOf(-90)).maximum(java.math.BigDecimal.valueOf(90)));
            Schema<Object> bounds = new Schema<>();
            bounds.addProperty("referencePoint", referencePoint);
            addConditional(request, condition, bounds);
        }
        Schema<?> v2v3 = openApi.getComponents().getSchemas().get("ConvertTrajectoryRequest");
        if (v2v3 != null) {
            Schema<Object> persistableCrs = new Schema<>();
            persistableCrs.addProperty("trajectoryCRS", new StringSchema().pattern(PERSISTABLE_CRS_PATTERN));
            Schema<Object> horizontalUnit = new Schema<>();
            horizontalUnit.setRequired(Arrays.asList("unitXY"));
            addConditional(v2v3, persistableCrs, horizontalUnit);
        }
        Schema<?> v4 = openApi.getComponents().getSchemas().get("ConvertTrajectoryRequestV4");
        if (v4 == null) {
            return;
        }
        Schema<Object> inclinationOnly = new Schema<>();
        inclinationOnly.addProperty("inputKind", new StringSchema()._const("MD_Incl"));
        addConditional(v4, inclinationOnly, stationsWithout("azimuth"));
        Schema<Object> deviationsOnly = new Schema<>();
        deviationsOnly.addProperty("inputKind", new StringSchema()._const("dX_dY_dZ"));
        addConditional(v4, deviationsOnly, stationsWithout("azimuth", "inclination", "md"));
        Schema<Object> persistableCrs = new Schema<>();
        persistableCrs.addProperty("trajectoryCRS", new StringSchema().pattern(PERSISTABLE_CRS_PATTERN));
        Schema<Object> horizontalUnit = new Schema<>();
        horizontalUnit.setRequired(Arrays.asList("unitXY"));
        addConditional(v4, persistableCrs, horizontalUnit);
    }

    private static Schema<Object> stationsWithout(String... forbidden) {
        Schema<Object> present = new Schema<>();
        for (String name : forbidden) {
            Schema<Object> required = new Schema<>();
            required.setRequired(Arrays.asList(name));
            present.addAnyOfItem(required);
        }
        Schema<Object> station = new Schema<>();
        station.setNot(present);
        Schema<Object> stations = new Schema<>();
        stations.addProperty("inputStations", new ArraySchema().items(station));
        return stations;
    }

    private static void addConditional(Schema<?> schema, Schema<?> when, Schema<?> thenSchema) {
        Schema<Object> rule = new Schema<>();
        rule.setIf(when);
        rule.setThen(thenSchema);
        schema.addAllOfItem(rule);
    }

    @Bean
    public OperationCustomizer operationCustomizer() {
        return (operation, handlerMethod) -> {
            if (InfoApi.class.isAssignableFrom(handlerMethod.getBeanType())
                    || HealthCheck.class.isAssignableFrom(handlerMethod.getBeanType())) {
                operation.setSecurity(java.util.Collections.emptyList());
                return operation;
            }
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
