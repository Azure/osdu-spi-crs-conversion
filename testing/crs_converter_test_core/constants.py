import os

VENDOR = os.getenv("VENDOR", "NOT_FOUND").lower()
BASE_URL = os.getenv("BASE_URL", '/api/crs/converter')
if VENDOR == 'ibm':
    ROOT_URL = os.getenv("IBM_VIRTUAL_HOST_CRS_CONVERSION")
else:
    ROOT_URL = os.getenv("VIRTUAL_SERVICE_HOST_NAME")
STORAGE_URL=os.getenv("STORAGE_URL", "NOT_FOUND")
DATA_DIR = os.getenv("DATA_DIR")
DATA_PATTERN = os.getenv("DATA_PATTERN")
REPORT_PATH = os.getenv("REPORT_PATH")
MY_TENANT = os.getenv("MY_TENANT")
MY_REPLACE_DOMAIN = os.getenv("MY_REPLACE_DOMAIN", "NOT_FOUND")
MY_LEGAL_TAG = os.getenv("MY_LEGAL_TAG", "NOT_FOUND")
MY_TEST_ID = os.getenv("MY_TEST_ID", "NOT_FOUND")