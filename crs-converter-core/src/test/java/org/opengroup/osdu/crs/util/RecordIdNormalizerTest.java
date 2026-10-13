/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
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

import org.junit.Test;
import static org.junit.Assert.*;

public class RecordIdNormalizerTest {

    @Test
    public void testNormalization() {
        String testWithTrailingColumn = "osdu:reference-data--CoordinateReferenceSystem:BoundProjected:EPSG::29193_EPSG::1867:";
        String expectedId = "osdu:reference-data--CoordinateReferenceSystem:BoundProjected:EPSG::29193_EPSG::1867";
        assertEquals(expectedId, RecordIdNormalizer.normalizeRecordID(testWithTrailingColumn));

        String testWithVersionDefined = "osdu:reference-data--CoordinateReferenceSystem:BoundProjected:EPSG::29193_EPSG::1867:123456";
        expectedId = "osdu:reference-data--CoordinateReferenceSystem:BoundProjected:EPSG::29193_EPSG::1867/123456";
        assertEquals(expectedId, RecordIdNormalizer.normalizeRecordID(testWithVersionDefined));
    }
}
