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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.opengroup.osdu.crs.config.Jackson2HttpConvertersEnvironmentPostProcessor.PREFERRED_JSON_MAPPER;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

class Jackson2HttpConvertersEnvironmentPostProcessorTest {

    private final Jackson2HttpConvertersEnvironmentPostProcessor processor =
            new Jackson2HttpConvertersEnvironmentPostProcessor();

    @Test
    void shouldDefaultToJackson2() {
        StandardEnvironment environment = new StandardEnvironment();
        processor.postProcessEnvironment(environment, new SpringApplication());
        assertEquals("jackson2", environment.getProperty(PREFERRED_JSON_MAPPER));
    }

    @Test
    void shouldNotOverrideExplicitApplicationProperty() {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(
                new MapPropertySource("application", Map.of(PREFERRED_JSON_MAPPER, "jackson")));
        processor.postProcessEnvironment(environment, new SpringApplication());
        assertEquals("jackson", environment.getProperty(PREFERRED_JSON_MAPPER));
    }
}
