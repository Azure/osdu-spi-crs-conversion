/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.crs.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.core.common.model.info.ConnectedOuterService;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The Apache SIS version that /info reports must match the expectation used by the acceptance tests
 * (crs-converter_acceptance_test/expected_dependencies.json). Upgrade both in the same change.
 */
class ApacheSisVersionExpectationTest {

  private static final String SIS_NAME = "Apache SIS-Version";
  private static final Path EXPECTATIONS =
      Path.of("..", "crs-converter_acceptance_test", "expected_dependencies.json");

  @Test
  void reportedVersionMatchesAcceptanceTestExpectation() {
    JsonNode entry = JsonMapper.builder().build().readTree(EXPECTATIONS.toFile()).path(SIS_NAME);
    String expected = entry.path("version").asString("");
    assertFalse(expected.isBlank(), "No '" + SIS_NAME + "' version in " + EXPECTATIONS);

    List<String> reported = new ApacheSISConnectedOuterServicesBuilder().buildConnectedOuterServices().stream()
        .filter(service -> SIS_NAME.equals(service.getName()))
        .map(ConnectedOuterService::getVersion)
        .toList();

    assertEquals(List.of(expected), reported,
        "Apache SIS on the classpath doesn't match " + EXPECTATIONS + "; update the expectation with the dependency");
  }
}
