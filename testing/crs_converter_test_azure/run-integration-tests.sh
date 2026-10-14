#!/usr/bin/env bash
# Copyright © Microsoft Corporation
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


# Install venv for python3
which apt-get && sudo apt-get install -y python3 python3-pip python3-venv || echo "Not Ubuntu, skipping"
which yum && sudo yum install -y python3 python3-pip python3-venv || echo "Not RHEL, skipping"

# Setup Python Environment and install requirements
python3 -m venv env
export STORAGE_URL=$STORAGE_URL'records'
export MY_REPLACE_DOMAIN='contoso.com'
export MY_TEST_ID="56789223"
export MY_LEGAL_TAG="opendes-public-usa-dataset"

sed -i 's/$1/${1:-}/' env/bin/activate # Fix deactivation bug '$1 unbound variable'
source env/bin/activate
python3 -m pip install --upgrade pip
python3 -m pip install -r requirements.txt

# Run tests
echo ***RUNNING CRS Converter API TESTS V2***
python3 run_test_api.py
TEST_STATUS_V2=$?
echo ***FINISHED CRS Converter API TESTS V2***
echo ""

# Run tests
echo ***RUNNING CRS Converter API TESTS V3***
python3 run_test_api_v3.py
TEST_STATUS_V3=$?
echo ***FINISHED CRS Converter API TESTS V3***
echo ""

	
# Display Results
echo "-------------------------------"
echo "TEST_STATUS_V2: $TEST_STATUS_V2"
echo "-------------------------------"
echo "-------------------------------"
echo "TEST_STATUS_V3: $TEST_STATUS_V3"
echo "-------------------------------"
# Uninstall Environment if not on ADO Pipelines
if [ -z ${AGENT_POOL+x}  ]; then
  python3 -m pip freeze > requirements.txt
  python3 -m pip uninstall -r requirements.txt -y
  deactivate
  rm -rf env/
fi

if [ $TEST_STATUS_V2 -ne 0 ] || [ $TEST_STATUS_V3 -ne 0 ]
then
  exit 1
else
  exit 0
fi