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

package org.opengroup.osdu.crs.config;

import java.util.Map;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.DefaultPropertiesPropertySource;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Spring Boot 4 prefers Jackson 3 for HTTP message conversion when both Jackson lines are present.
 * The CRS conversion models are Jackson 2 annotated, so every application (core-plus and all
 * providers) defaults to Jackson 2 to keep the JSON API contract unchanged.
 * Registered as a default property, so an explicit application property still takes precedence.
 */
public class Jackson2HttpConvertersEnvironmentPostProcessor implements EnvironmentPostProcessor {

    static final String PREFERRED_JSON_MAPPER = "spring.http.converters.preferred-json-mapper";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        DefaultPropertiesPropertySource.addOrMerge(
                Map.of(PREFERRED_JSON_MAPPER, "jackson2"), environment.getPropertySources());
    }
}
