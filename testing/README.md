# dps-crs-converter integration tests

## Folder structure

testing/

* crs_converter_test_core/
  * ...
* crs_converter_test_$PROVIDER_NAME/
  * jwt_client.py
  * run_test.py

This integration test uses a swagger generated Python client to test a
deployed crs-converter service. The source is located in this repository
```./api_spec/crs_converter_openapi.json```.

The python client code is automatically generated. The latest online version (May 2018)
created incorrect impost statements for cyclic class references. Therefore the current
code is generated using [swagger-codegen-cli-2.2.3.jar](http://repo1.maven.org/maven2/io/swagger/swagger-codegen-cli/2.2.3/swagger-codegen-cli-2.2.3.jar).
The command to create the python code is:
Linux

```bash
cd testing
java -jar ~/swagger-codegen-cli-2.2.3.jar generate -i crs_converter_test_core/api_spec/crs_converter_openapi.json -l python -o crs_converter_test_core/v2
```

Windows

```bat
cd testing
java -jar %UserProfile%\repositories\azure\swagger-codegen-cli-2.2.3.jar generate -i crs_converter_test_core\api_spec\crs_converter_openapi.json -l python -o crs_converter_test_core\v2
```

## Sanity Test: ```test_crs_converter_v2.py```

This test is intended as a simple sanity test. It is quick but doesn't challenge the conversion engine thoroughly

The following parameters are expected as environment variables:

## Baremetal auth provider (crs_converter_test_baremetal/jwt_client.py)

| Variable | Contents |
|----------|----------|
| TEST_OPENID_PROVIDER_CLIENT_ID | Client id. |
| TEST_OPENID_PROVIDER_URL | Auth url. |
| TEST_OPENID_PROVIDER_CLIENT_SECRET | Client Secret. |

## Tests core (crs_converter_test_core/constants.py)

| Variable | Contents |
|----------|----------|
| BASE_URL | e.g. `/api/crs/converter/v2`  |
| VIRTUAL_SERVICE_HOST_NAME | e.g. open.opendes.cloud.slb-ds.com |
| MY_TENANT | e.g. opendes |
| DATA_DIR | e.g. ```E:\tmp\CloudTestData``` |
| DATA_PATTERN | Test*.0*.json |
| REPORT_PATH | e.g.  ```E:\tmp\CloudTestData\Report\SuiteReport.txt``` |

## Building/running ```test_crs_converter_v2.py```

Go to the provider folder:

```bash
cd crs_converter_test_$PROVIDER_NAME/
```

To set up a virtual environment:

```bash
virtualenv venv
python3 -m pip install -r requirements.txt
```

To activate on Windows:

```bash
venv\Scripts\activate
```

To run:

```bash
python3 run_test.py
```

### Azure Tests

For the python tests to run in Azure, the CI-CD pipeline copies the SIS_DATA folder to the shared storage for the pods to read. If you are setting up the environment manually and not using Azure CI-CD Pipeline, make sure to copy the folder following [this](https://community.opengroup.org/osdu/platform/deployment-and-operations/infra-azure-provisioning/-/blob/master/docs/configuration-data.md#crs-conversion).

**Note:** To simulate a runtime exactly as that of the vsts build agent, you can simply exec into the docker image we use for the build agent, and run the tests from inside it. To know how to do this, please follow [this](https://slb-swt.visualstudio.com/data-at-rest/_git/dps-vsts-build-agent?path=%2FREADME.md&version=GBmaster) documentation.

## Suite Test: ```test_suite.py```

This test requires pre-computed test data with expected values. This test - depending on how many data are sent through - will take a significant amount of time.

### Example format for the test data

```
{
  "CreateDate": "2017-08-07T15:02:51.8424946+00:00",
  "TestData": [
    {
      "FromCrsPersistableReference": "%7B%22WKT%22%3A%22PROJCS%5B%5C%22DGN_1995_UTM_Zone_48N%5C%22%2CGEOGCS%5B%5C%22GCS_DGN_1995%5C%22%2CDATUM%5B%5C%22D_Datum_Geodesi_Nasional_1995%5C%22%2CSPHEROID%5B%5C%22WGS_1984%5C%22%2C6378137.0%2C298.257223563%5D%5D%2CPRIMEM%5B%5C%22Greenwich%5C%22%2C0.0%5D%2CUNIT%5B%5C%22Degree%5C%22%2C0.0174532925199433%5D%5D%2CPROJECTION%5B%5C%22Transverse_Mercator%5C%22%5D%2CPARAMETER%5B%5C%22False_Easting%5C%22%2C500000.0%5D%2CPARAMETER%5B%5C%22False_Northing%5C%22%2C0.0%5D%2CPARAMETER%5B%5C%22Central_Meridian%5C%22%2C105.0%5D%2CPARAMETER%5B%5C%22Scale_Factor%5C%22%2C0.9996%5D%2CPARAMETER%5B%5C%22Latitude_Of_Origin%5C%22%2C0.0%5D%2CUNIT%5B%5C%22Meter%5C%22%2C1.0%5D%2CAUTHORITY%5B%5C%22EPSG%5C%22%2C23868%5D%5D%22%2C%22Type%22%3A%22LBCRS%22%2C%22EngineVersion%22%3A%22PE_10_3_1%22%2C%22Name%22%3A%22DGN_1995_UTM_Zone_48N%22%2C%22AuthorityCode%22%3A%7B%22Authority%22%3A%22EPSG%22%2C%22Code%22%3A%2223868%22%7D%7D",
      "ToCrsPersistableReference": "%7B%22WKT%22%3A%22GEOGCS%5B%5C%22GCS_DGN_1995%5C%22%2CDATUM%5B%5C%22D_Datum_Geodesi_Nasional_1995%5C%22%2CSPHEROID%5B%5C%22WGS_1984%5C%22%2C6378137.0%2C298.257223563%5D%5D%2CPRIMEM%5B%5C%22Greenwich%5C%22%2C0.0%5D%2CUNIT%5B%5C%22Degree%5C%22%2C0.0174532925199433%5D%2CAUTHORITY%5B%5C%22EPSG%5C%22%2C4755%5D%5D%22%2C%22Type%22%3A%22LBCRS%22%2C%22EngineVersion%22%3A%22PE_10_3_1%22%2C%22Name%22%3A%22GCS_DGN_1995%22%2C%22AuthorityCode%22%3A%7B%22Authority%22%3A%22EPSG%22%2C%22Code%22%3A%224755%22%7D%7D",
      "FromInfo": "DGN_1995_UTM_Zone_48N [m]",
      "ToInfo": "GCS_DGN_1995 [dega]",
      "Info": "Late-bound CRS Conversions",
      "FromToPoints": [
        {
          "FromPoint": {
            "X": 166021.4430837048,
            "Y": 0.0,
            "Z": 0.0
          },
          "ToPoint": {
            "X": 102.0,
            "Y": 0.0,
            "Z": 0.0
          }
        },
        {
          "FromPoint": {
            "X": 833978.5569162938,
            "Y": 0.0,
            "Z": 0.0
          },
          "ToPoint": {
            "X": 108.0,
            "Y": 0.0,
            "Z": 0.0
          }
        },
        {
          "FromPoint": {
            "X": 168456.68464518717,
            "Y": 768165.58302924014,
            "Z": 0.0
          },
          "ToPoint": {
            "X": 102.0,
            "Y": 6.94,
            "Z": 0.0
          }
        },
        {
          "FromPoint": {
            "X": 831543.31535481149,
            "Y": 768165.58302924014,
            "Z": 0.0
          },
          "ToPoint": {
            "X": 108.0,
            "Y": 6.94,
            "Z": 0.0
          }
        },
        {
          "FromPoint": {
            "X": 500000.0,
            "Y": 383543.96537060448,
            "Z": 0.0
          },
          "ToPoint": {
            "X": 105.0,
            "Y": 3.47,
            "Z": 0.0
          }
        }
      ]
    }
  ]
}
```

## Building/running ```test_suite.py```

To set up a virtual environment:

```bash
virtualenv venv
python3 -m pip install -r requirements.txt
```

To activate on Windows:

```bash
venv\Scripts\activate
```

To run:

```bash
python test_suite.py
```
