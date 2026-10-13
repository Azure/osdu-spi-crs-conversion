#!/usr/bin/env bash

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
    --alluredir=cimpl/allure-results \
    --clean-alluredir \
    -v

TEST_STATUS=$?

echo ""
echo "***FINISHED CRS CONVERTER TESTS***"
echo ""

deactivate

exit $TEST_STATUS