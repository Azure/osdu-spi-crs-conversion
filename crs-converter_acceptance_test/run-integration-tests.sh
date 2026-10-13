#!/bin/bash -eu
#
#  Copyright 2026 EPAM Systems, Inc
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#

# Setup Python virtual environment
python3 -m venv venv
source venv/bin/activate

# Install Python dependencies
pip install -q --upgrade pip
pip install -q -r requirements.txt
pip install -q -r v2/requirements.txt

echo ""
echo "***RUNNING CRS CONVERTER TESTS WITH ALLURE REPORTING***"
echo ""

# Run all tests with pytest (handles v2, v3, v4, both pytest and unittest tests)
pytest test_crs_converter_v2.py test_crs_converter_v3.py test_crs_converter_v4.py \
    --alluredir=allure-results \
    --clean-alluredir \
    -v

TEST_STATUS=$?

echo ""
echo "***FINISHED CRS CONVERTER TESTS***"
echo ""

deactivate

exit $TEST_STATUS
