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

public final class RecordIdNormalizer {

    /*
    Record ID provided as input can be in the `record:` or `record:version` format
    But to make a query to the storage API, we need to normalize the ID as you can't have a
    trailing `:` or `/` in the API Call
    */
    public static String normalizeRecordID(String recordId) {
        int lastColumnIndex = recordId.lastIndexOf(":");
        boolean withTrailingColumn = lastColumnIndex == recordId.length() - 1;
        if(withTrailingColumn) {
            return recordId.substring(0, recordId.length() - 1);
        }
        else {
            return recordId.substring(0, lastColumnIndex) + "/" + recordId.substring(lastColumnIndex + 1);
        }
    }
}
