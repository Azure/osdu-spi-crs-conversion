# coding: utf-8
"""Integration unit tests for crs-converter"""
import unittest
import os
import math
import json

from http import HTTPStatus
from os import listdir
from os.path import isfile, join
from tenacity import retry, retry_if_exception, stop_after_attempt, wait_exponential

import urllib3
import uuid

urllib3.disable_warnings()
import warnings
import logging
import time
logging.basicConfig(
    level=os.environ.get("LOG_LEVEL", "INFO"),
    format="%(asctime)s %(levelname)s %(name)s %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S"
)
logging.Formatter.converter = time.gmtime
logger = logging.getLogger(__name__)

from crs_converter_test_core.v3.swagger_client import ApiClient, CRSConversionApi, \
    TrajectoryComputationAndConversionApi, \
    TrajectoryStationIn, TrajectoryStationOut, Configuration, ConvertTrajectoryResponse, InfoApiApi, \
    AbstractAnyCrsFeatureCollection, ConvertBinGridRequest
from crs_converter_test_core.v3.swagger_client.models import ConvertPointsResponse, ConvertPointsRequest, Point, \
    ConvertTrajectoryRequest, \
    ConvertGeoJsonRequest, AnyCrsGeoJsonFeatureCollection, \
    AnyCrsGeoJsonFeature, AnyCrsGeoJsonPolygon, VersionInfo
from crs_converter_test_core.v3.swagger_client.rest import ApiException
from pprint import pprint
from crs_converter_test_core.utility import TestEnvironment
import jwt_client


FROM_CRS = "{\"authCode\":{\"auth\":\"SLB\",\"code\":\"4248009\"},\"type\":\"EBC\",\"ver\":\"PE_10_3_1\",\"name\":\"PSAD56 * DMA-Ven [4248,1209]\",\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4248\"},\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"name\":\"GCS_Provisional_S_American_1956\",\"wkt\":\"GEOGCS[\\\"GCS_Provisional_S_American_1956\\\",DATUM[\\\"D_Provisional_S_American_1956\\\",SPHEROID[\\\"International_1924\\\",6378388.0,297.0]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433],AUTHORITY[\\\"EPSG\\\",4248]]\"},\"singleCT\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"1209\"},\"type\":\"ST\",\"ver\":\"PE_10_3_1\",\"name\":\"PSAD_1956_To_WGS_1984_9\",\"wkt\":\"GEOGTRAN[\\\"PSAD_1956_To_WGS_1984_9\\\",GEOGCS[\\\"GCS_Provisional_S_American_1956\\\",DATUM[\\\"D_Provisional_S_American_1956\\\",SPHEROID[\\\"International_1924\\\",6378388.0,297.0]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Geocentric_Translation\\\"],PARAMETER[\\\"X_Axis_Translation\\\",-295.0],PARAMETER[\\\"Y_Axis_Translation\\\",173.0],PARAMETER[\\\"Z_Axis_Translation\\\",-371.0],AUTHORITY[\\\"EPSG\\\",1209]]\"}}"

TO___CRS = "{\"authCode\":{\"auth\":\"SLB\",\"code\":\"30200002\"},\"type\":\"EBC\",\"ver\":\"PE_10_3_1\",\"name\":\"Trinidad 1903 * EOG-Tto Trin / Trinidad Grid [30200,10085]\",\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"30200\"},\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"name\":\"Trinidad_1903_Trinidad_Grid\",\"wkt\":\"PROJCS[\\\"Trinidad_1903_Trinidad_Grid\\\",GEOGCS[\\\"GCS_Trinidad_1903\\\",DATUM[\\\"D_Trinidad_1903\\\",SPHEROID[\\\"Clarke_1858\\\",6378293.64520876,294.260676369]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],PROJECTION[\\\"Cassini\\\"],PARAMETER[\\\"False_Easting\\\",430000.0],PARAMETER[\\\"False_Northing\\\",325000.0],PARAMETER[\\\"Central_Meridian\\\",-61.3333333333333],PARAMETER[\\\"Scale_Factor\\\",1.0],PARAMETER[\\\"Latitude_Of_Origin\\\",10.4416666666667],UNIT[\\\"Link_Clarke\\\",0.201166195164],AUTHORITY[\\\"EPSG\\\",30200]]\"},\"singleCT\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"10085\"},\"type\":\"ST\",\"ver\":\"PE_10_3_1\",\"name\":\"Trinidad_1903_To_WGS_1984_2\",\"wkt\":\"GEOGTRAN[\\\"Trinidad_1903_To_WGS_1984_2\\\",GEOGCS[\\\"GCS_Trinidad_1903\\\",DATUM[\\\"D_Trinidad_1903\\\",SPHEROID[\\\"Clarke_1858\\\",6378293.64520876,294.260676369]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Geocentric_Translation\\\"],PARAMETER[\\\"X_Axis_Translation\\\",-61.0],PARAMETER[\\\"Y_Axis_Translation\\\",285.2],PARAMETER[\\\"Z_Axis_Translation\\\",471.6],AUTHORITY[\\\"EPSG\\\",10085]]\"}}"

WGS84 = "{\"wkt\":\"GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433],AUTHORITY[\\\"EPSG\\\",4326]]\",\"ver\":\"PE_10_3_1\",\"name\":\"GCS_WGS_1984\",\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4326\"},\"type\":\"LBC\"}"
LAS = "{\"lateBoundCRS\":{\"wkt\":\"PROJCS[\\\"NAD_1927_StatePlane_Louisiana_South_FIPS_1702\\\",GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.9786982]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],PROJECTION[\\\"Lambert_Conformal_Conic\\\"],PARAMETER[\\\"False_Easting\\\",2000000.0],PARAMETER[\\\"False_Northing\\\",0.0],PARAMETER[\\\"Central_Meridian\\\",-91.3333333333333],PARAMETER[\\\"Standard_Parallel_1\\\",29.3],PARAMETER[\\\"Standard_Parallel_2\\\",30.7],PARAMETER[\\\"Latitude_Of_Origin\\\",28.6666666666667],UNIT[\\\"Foot_US\\\",0.304800609601219],AUTHORITY[\\\"EPSG\\\",26782]]\",\"ver\":\"PE_10_3_1\",\"name\":\"NAD_1927_StatePlane_Louisiana_South_FIPS_1702\",\"authCode\":{\"auth\":\"EPSG\",\"code\":\"26782\"},\"type\":\"LBC\"},\"singleCT\":{\"wkt\":\"GEOGTRAN[\\\"NAD_1927_To_WGS_1984_79_CONUS\\\",GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.9786982]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"NADCON\\\"],PARAMETER[\\\"Dataset_conus\\\",0.0],AUTHORITY[\\\"EPSG\\\",15851]]\",\"ver\":\"PE_10_3_1\",\"name\":\"NAD_1927_To_WGS_1984_79_CONUS\",\"authCode\":{\"auth\":\"EPSG\",\"code\":\"15851\"},\"type\":\"ST\"},\"ver\":\"PE_10_3_1\",\"name\":\"NAD27 * OGP-Usa Conus / Louisiana South [26782,15851]\",\"authCode\":{\"auth\":\"SLB\",\"code\":\"26782079\"},\"type\":\"EBC\"}"
FROM_CRS_7844003 = "{\"authCode\":{\"auth\":\"OSDU\",\"code\":\"7844003\"},\"lateBoundCRS\":{\"authCode\":{" \
                   "\"auth\":\"EPSG\",\"code\":\"7844\"},\"name\":\"GDA2020\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\"," \
                   "\"wkt\":\"GEOGCS[\\\"GDA2020\\\",DATUM[\\\"GDA2020\\\",SPHEROID[\\\"GRS_1980\\\",6378137.0," \
                   "298.257222101]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]," \
                   "AUTHORITY[\\\"EPSG\\\",7844]]\"},\"name\":\"GDA2020 * ICSM-Aus Conf [7844,9690]\",\"singleCT\":{" \
                   "\"authCode\":{\"auth\":\"EPSG\",\"code\":\"9690\"},\"name\":\"WGS_1984_To_GDA2020_3\"," \
                   "\"type\":\"ST\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGTRAN[\\\"WGS_1984_To_GDA2020_3\\\"," \
                   "GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0," \
                   "298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]]," \
                   "GEOGCS[\\\"GDA2020\\\",DATUM[\\\"GDA2020\\\",SPHEROID[\\\"GRS_1980\\\",6378137.0,298.257222101]]," \
                   "PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]]," \
                   "METHOD[\\\"Coordinate_Frame\\\"],PARAMETER[\\\"X_Axis_Translation\\\",0.06155],PARAMETER[" \
                   "\\\"Y_Axis_Translation\\\",-0.01087],PARAMETER[\\\"Z_Axis_Translation\\\",-0.04019]," \
                   "PARAMETER[\\\"X_Axis_Rotation\\\",-0.0394924],PARAMETER[\\\"Y_Axis_Rotation\\\",-0.0327221]," \
                   "PARAMETER[\\\"Z_Axis_Rotation\\\",-0.0328979],PARAMETER[\\\"Scale_Difference\\\",-0.009994]," \
                   "OPERATIONACCURACY[3.1],AUTHORITY[\\\"EPSG\\\",9690]]\"},\"type\":\"EBC\",\"ver\":\"PE_10_9_1\"} "

STORAGE_DATA_RECORDS_FILE_PATH = os.path.join(os.path.dirname(__file__), "v3/data/Storagerecords/")
INGEST_PREREQ_DATA_RECORD_FILE_NAME = "coordinateReferenceSystemRecord.json"
INGEST_PREREQ_DATA_RECORD_FILE_PATH = os.path.join(os.path.dirname(STORAGE_DATA_RECORDS_FILE_PATH), INGEST_PREREQ_DATA_RECORD_FILE_NAME)

MAX_RETRY_ATTEMPTS = 3
MIN_RETRY_WAIT_IN_SECONDS = 5
MAX_RETRY_WAIT_IN_SECONDS = 30


def is_close(a, b, rel_tol=1e-09, abs_tol=0.0):
    """Compare a double
    https://stackoverflow.com/questions/5595425/what-is-the-best-way-to-compare-floats-for-almost-equality-in-python"""
    if math.isnan(float(a)) and math.isnan(float(b)):
        return True  # we treat NaN as values here
    if math.isnan(float(a)) or math.isnan(float(b)):
        return False
    return abs(a-b) <= max(rel_tol * max(abs(a), abs(b)), abs_tol)

def convertString(str):
    DATA_PARTITION_TO_REPLACE = '{{DATA_PARTITION_ID}}'
    DOMAIN_TO_REPLACE = '{{DOMAIN}}'
    TAG_TO_REPLACE = '{{LEGAL_TAG}}'
    TEST_ID_REPLACE = '{{TEST_ID}}'
    env = TestEnvironment()
    body_str = str
    body_str = body_str.replace(DATA_PARTITION_TO_REPLACE, env.data_partition_id)
    body_str = body_str.replace(DOMAIN_TO_REPLACE, env.my_replace_domain)
    body_str = body_str.replace(TAG_TO_REPLACE, env.my_legal_tag)
    body_str = body_str.replace(TEST_ID_REPLACE, env.my_test_id)
    return body_str

def retry_if_status_code(exception):
    """Retry if the exception contains one of the following status codes"""
    if isinstance(exception, ApiException):
        # List of status codes that should trigger retries
        retry_status_codes = [
            HTTPStatus.TOO_MANY_REQUESTS,       # 429
            HTTPStatus.BAD_GATEWAY,             # 502
            HTTPStatus.SERVICE_UNAVAILABLE,     # 503
            HTTPStatus.GATEWAY_TIMEOUT,         # 504
        ]
        return exception.status in retry_status_codes
    return False

class TestDataReader(object):
    """Read a test data set and return a dictionary"""
    def __init__(self, filename):
        self.__data_set = None
        if filename is not None:
            dir_path = os.path.dirname(__file__)
            dir_path = os.path.join(dir_path,  'v3', 'data', filename)
            try:
                with open(dir_path) as json_file:
                    string = json_file.read()
                    string = convertString(string)
                    self.__data_set = json.loads(string)
            except IOError as err:
                print(err)

    def number_of_sets(self):
        """returns the number of data sets in this file"""
        result = -1
        if self.is_ok():
            if 'TestData' in self.__data_set:
                return len(self.__data_set['TestData'])
        return result

    def is_ok(self):
        """Returns true of the data set is usable"""
        return self.__data_set is not None

    def __get_data_by_index(self, index=0):
        if index < self.number_of_sets():
            return self.__data_set['TestData'][index]

    def get_request_response(self, index=0):
        if self.is_ok():
            ds = self.__get_data_by_index(index)
            if ds is not None:
                from_crs = ds['FromCrsPersistableReference']
                to___crs = ds['ToCrsPersistableReference']
                points = list()
                expected = list()
                success_count = 0
                for pt_pair in ds['FromToPoints']:
                    p_from = pt_pair['FromPoint']
                    points.append(Point(p_from['X'], p_from['Y'], p_from['Z']))
                    p_to = pt_pair['ToPoint']
                    ep = Point(p_to['X'], p_to['Y'], p_to['Z'])
                    expected.append(ep)
                    if not math.isnan(float(ep.x)) and not math.isnan(float(ep.y)) and not math.isnan(float(ep.z)):
                        success_count += 1
                request = ConvertPointsRequest(from_crs=from_crs, to_crs=to___crs, points=points)
                response = ConvertPointsResponse(success_count=success_count, points=expected)
                return request, response


class CompareResponseWithExpectation(object):
    """Helper class to compare an actual conversion response to an expected"""
    def __init__(self, actual, expected):
        self.actual = actual
        self.expected = expected

    def compare_convert_points_response(self):
        if isinstance(self.actual, ConvertPointsResponse) and isinstance(self.expected, ConvertPointsResponse):
            same = self.actual.success_count == self.expected.success_count
            same = same and len(self.actual.points) == len(self.expected.points)
            if same:
                for i in range(0, len(self.actual.points)):
                    a = self.actual.points[i]
                    e = self.expected.points[i]
                    close = is_close(a.x, e.x) and is_close(a.y, e.y) and is_close(a.z, e.z)
                    if not close:
                        print('Different point values at index ' + str(i) + '; actual, expected')
                        pprint(a)
                        pprint(e)
                    same = same and close
            return same
        return False

    def compare_convert_trajectory_response(self):
        if isinstance(self.actual, ConvertTrajectoryResponse) and isinstance(self.expected, ConvertTrajectoryResponse):
            same = len(self.actual.stations) >= len(self.expected.stations)
            same = same and len(self.expected.operations_applied) <= len(self.actual.operations_applied)
            for i in range(0, len(self.expected.operations_applied)):
                a = self.actual.operations_applied[i]
                e = self.expected.operations_applied[i]
                #  same = same and e == a  # different messages for proper traj engine
            if same:
                for i in range(0, len(self.actual.stations)):
                    a = self.actual.stations[i].point
                    if self.actual.stations[i].original:
                        j = self.__find_expected_original_station_index_for_md(self.actual.stations[i].md)
                        if j is not None:
                            e = self.expected.stations[j].point
                            close = is_close(a.x, e.x) and is_close(a.y, e.y) and is_close(a.z, e.z)
                            if not close:
                                print('Different point values at index ' + str(i) + '; actual, expected')
                                pprint(a)
                                pprint(e)
                            close_d = is_close(self.expected.stations[j].dls, self.actual.stations[i].dls)
                            if not close_d:
                                print("DLS mismatch actual: {} expected: {}".format(self.expected.stations[j].dls, self.actual.stations[i].dls))
                                if i == 0:
                                    close_d = True  # in old versions we expected NaN for the first sample
                            same = same and close and close_d
                        else:
                            same = False  # we must find all original MD values - that's an error
            same = self.__compare_strings(self.actual.method, self.expected.method) and same
            same = self.__compare_strings(self.actual.unit_xy, self.expected.unit_xy) and same
            same = self.__compare_strings(self.actual.unit_z, self.expected.unit_z) and same
            same = self.__compare_strings(self.actual.unit_dls, self.expected.unit_dls) and same
            same = self.__compare_strings(self.actual.trajectory_crs, self.expected.trajectory_crs) and same
            return same
        return False

    @staticmethod
    def __compare_strings(actual, expected):
        if actual == expected:  # lucky, strings are equal
            return True
        else:  # strings might be serialized JSON
            try:
                parsed_actual = json.loads(actual)  # this may fail if it is a simple string - it is an error anyway
                parsed_expected = json.loads(expected)
                same = True
                for key, value in parsed_expected.items():
                    same = same and value == parsed_actual[key]
                if same:
                    return True
            except ValueError:
                pass
            print('Mismatch, actual: {}, expected: {}'.format(actual, expected))
            return False

    def __find_expected_original_station_index_for_md(self, md):
        i = 0
        for station in self.expected.stations:
            if station.original and is_close(md, station.md):
                return i
            i += 1
        return None

    def compare_feature_collections(self):
        # works for polygon only
        same = True
        for f in range(0, len(self.actual.features)):
            same = same and self.actual.features[f].type == self.expected.features[f].type
            for p in range(0, len(self.actual.features[f].geometry.coordinates)):
                for j in range(0, len(self.actual.features[f].geometry.coordinates[p])):
                    for i in range(0, len(self.actual.features[f].geometry.coordinates[p][j])):
                        same = same and is_close(self.actual.features[f].geometry.coordinates[p][j][i],
                                                 self.expected.features[f].geometry.coordinates[p][j][i])
        if same and self.expected.bbox:
            if self.actual.bbox is None:
                same = False
            else:
                for i in range(0, len(self.actual.bbox)):
                    same = same and is_close(self.actual.bbox[i],
                                             self.expected.bbox[i])

        return same


class TestCrsConverterIntegration(unittest.TestCase):
    """Post deployment tests for crs-converter service"""

    @classmethod
    def setUpClass(cls):
        warnings.filterwarnings("ignore", category=ResourceWarning, message="unclosed.*<ssl.SSLSocket.*>")
        warnings.filterwarnings("ignore", category=ResourceWarning, message="unclosed.*<socket.socket.*>")
        urllib3.disable_warnings()

        cls.env = TestEnvironment()
        if not cls.env.is_ok():
            raise Exception('Test environment is not properly set up; BASE_URL, ROOT_URL, MY_TENANT not set.')
        configuration = Configuration()
        # Set the bearer token; use a service account to do this
        bearer = jwt_client.get_id_token()
        configuration.api_key['Authorization'] = 'Bearer ' + bearer
        configuration.access_token = bearer
        configuration.verify_ssl = False
        if 'localhost' in cls.env.root_url:
            url = 'http://' + cls.env.root_url + cls.env.base_url
        else:
            url = 'https://' + cls.env.root_url + cls.env.base_url
        data_partition_header_name = 'data_partition_id'
        data_partition_header_value = cls.env.data_partition_id
        client = ApiClient(host=url)
        client.set_default_header(header_name=data_partition_header_name, header_value=data_partition_header_value)
        client.user_agent = 'IntegrationTest'
        cls.api_instance = CRSConversionApi(client)
        cls.test_records = TestRecords()
        cls.test_records.setup()

    @classmethod
    def tearDownClass(cls):
        cls.test_records.teardown()

    @unittest.SkipTest
    def test_transformation_with_partial_failure(self):
        """Read from data/PartialFail.json and convert/transform"""
        reader = TestDataReader('PartialFail.json')
        self.assertIsNotNone(reader)
        request, response = reader.get_request_response(0)
        data_partition_header = self.api_instance.api_client.default_headers['data_partition_id']
        self.assertIsInstance(request, ConvertPointsRequest)
        self.assertIsInstance(response, ConvertPointsResponse)
        try:
            # Convert a list of points
            api_response = self.api_instance.convert_point(body=request, data_partition_id=data_partition_header, _request_timeout=180)
            self.assertIsNotNone(api_response)
            self.assertIsInstance(api_response, ConvertPointsResponse)
            self.assertIsNotNone(api_response.operations_applied)
            self.assertEqual(2, len(api_response.operations_applied))
            c = CompareResponseWithExpectation(api_response, expected=response)
            ok = c.compare_convert_points_response()
            self.assertTrue(ok, 'Actual response is different from expected response.')
        except ApiException as e:
            self.fail(str(e))

    def test_conversion_only(self):
        """Read from data/ConversionOnly.json and convert/transform"""
        reader = TestDataReader('ConversionOnly.json')
        self.assertIsNotNone(reader)
        request, response = reader.get_request_response(0)
        data_partition_header = self.api_instance.api_client.default_headers['data_partition_id']
        self.assertIsInstance(request, ConvertPointsRequest)
        self.assertIsInstance(response, ConvertPointsResponse)
        try:
            # Convert a list of points
            api_response = self.api_instance.convert_point(body=request, data_partition_id=data_partition_header, _request_timeout=180)
            self.assertIsNotNone(api_response)
            self.assertIsNotNone(api_response.operations_applied)
            self.assertEqual(1, len(api_response.operations_applied))
            self.assertIsInstance(api_response, ConvertPointsResponse)
            c = CompareResponseWithExpectation(api_response, expected=response)
            ok = c.compare_convert_points_response()
            self.assertTrue(ok, 'Actual response is different from expected response.')
        except ApiException as e:
            self.fail(str(e))

    def test_conversion_only_ID(self):
        """Read from data/ConversionOnly.json and convert/transform"""
        reader = TestDataReader('ConversionOnly_ID.json')
        self.assertIsNotNone(reader)
        request, response = reader.get_request_response(0)
        data_partition_header = self.api_instance.api_client.default_headers['data_partition_id']
        self.assertIsInstance(request, ConvertPointsRequest)
        self.assertIsInstance(response, ConvertPointsResponse)
        try:
            # Convert a list of points
            api_response = self.api_instance.convert_point(body=request, data_partition_id=data_partition_header, _request_timeout=180)
            self.assertIsNotNone(api_response)
            self.assertIsNotNone(api_response.operations_applied)
            self.assertEqual(1, len(api_response.operations_applied))
            self.assertIsInstance(api_response, ConvertPointsResponse)
            c = CompareResponseWithExpectation(api_response, expected=response)
            ok = c.compare_convert_points_response()
            self.assertTrue(ok, 'Actual response is different from expected response.')
        except ApiException as e:
            self.fail(str(e))


    def test_transformation_with_duplicate_points(self):
        """Read from data/DuplicatePoints.json and convert/transform"""
        reader = TestDataReader('DuplicatePoints.json')
        self.assertIsNotNone(reader)
        request, response = reader.get_request_response(0)
        data_partition_header = self.api_instance.api_client.default_headers['data_partition_id']
        self.assertIsInstance(request, ConvertPointsRequest)
        self.assertIsInstance(response, ConvertPointsResponse)
        try:
            # Convert a list of points
            api_response = self.api_instance.convert_point(body=request, data_partition_id=data_partition_header, _request_timeout=180)
            self.assertIsNotNone(api_response)
            self.assertIsInstance(api_response, ConvertPointsResponse)
            self.assertIsNotNone(api_response.operations_applied)
            self.assertEqual(3, len(api_response.operations_applied))
            c = CompareResponseWithExpectation(api_response, expected=response)
            ok = c.compare_convert_points_response()
            self.assertTrue(ok, 'Actual response is different from expected response.')
        except ApiException as e:
            self.fail(str(e))

    @unittest.SkipTest
    def test_convert_demo(self):
        """Simple point conversion request like in the Swagger default data"""
        data_partition_header = self.api_instance.api_client.default_headers['data_partition_id']
        points = list()
        points.append(Point(-61.04340628871454, 10.673103179456877, 0.0))
        body = ConvertPointsRequest(from_crs=FROM_CRS, to_crs=TO___CRS, points=points)
        try:
            # Convert a list of points
            api_response = self.api_instance.convert_point(body=body, data_partition_id=data_partition_header)
            self.assertIsNotNone(api_response)
            self.assertIsInstance(api_response, ConvertPointsResponse)
            self.assertEquals(api_response.success_count, 1)
            self.assertTrue(is_close(api_response.points[0].x, 586399.4230309083))
            self.assertTrue(is_close(api_response.points[0].y, 448578.26031172264))
            self.assertTrue(is_close(api_response.points[0].z, 0))
            # pprint(api_response)
        except ApiException as e:
            self.fail(str(e))

    def test_convert_check_WGS84_to_case(self):
        """Simple point conversion request like in the Swagger default data"""
        data_partition_header = self.api_instance.api_client.default_headers['data_partition_id']
        points = list()
        points.append(Point(130.0, -30.0, 0.0))
        body = ConvertPointsRequest(from_crs=FROM_CRS_7844003, to_crs=WGS84, points=points)
        try:
            # Convert a list of points
            api_response = self.api_instance.convert_point(body=body, data_partition_id=data_partition_header)
            self.assertIsNotNone(api_response)
            self.assertIsInstance(api_response, ConvertPointsResponse)
            self.assertEquals(api_response.success_count, 1)
            self.assertTrue(is_close(api_response.points[0].x, 129.99999132871014))
            self.assertTrue(is_close(api_response.points[0].y, -30.000013761762506))
            self.assertTrue(is_close(api_response.points[0].z, 0))
            # pprint(api_response)
        except ApiException as e:
            self.fail(str(e))

    def test_geo_json_to_any_crs(self):
        request = self.__read_request('v3/data/GeoJsonPolygon.json')
        self.assertIsNotNone(request)
        data_partition_header = self.api_instance.api_client.default_headers['data_partition_id']
        try:
            # Convert a GeoJSON or AnyCrsGeoJson structure
            api_response = self.api_instance.convert_geo_json(body=request, data_partition_id=data_partition_header)
            self.assertIsNotNone(api_response)
            self.assertEquals(api_response.feature_collection.type, 'AnyCrsFeatureCollection')
            #  prepare round-trip
            n_request = ConvertGeoJsonRequest(to_crs=WGS84, feature_collection=api_response.feature_collection)
            api_response = self.api_instance.convert_geo_json(body=n_request, data_partition_id=data_partition_header)
            self.assertIsNotNone(api_response)
            self.assertEquals(api_response.feature_collection.type, 'FeatureCollection')
            c = CompareResponseWithExpectation(api_response.feature_collection, expected=request.feature_collection)
            ok = c.compare_feature_collections()
            self.assertTrue(ok, 'Actual response is different from expected response.')
        except ApiException as e:
            self.fail(str(e))

    def test_any_crs_to_geo_json(self):
        request = self.__read_request('v3/data/AnyCrsGeoJsonPolygon.json')
        data_partition_header = self.api_instance.api_client.default_headers['data_partition_id']
        self.assertIsNotNone(request)
        #try:
            # Convert a GeoJSON or AnyCrsGeoJson structure
        api_response = self.api_instance.convert_geo_json(body=request, data_partition_id=data_partition_header)
        self.assertIsNotNone(api_response)
        self.assertEquals(api_response.feature_collection.type, 'FeatureCollection')
        #  prepare round-trip
        n_request = ConvertGeoJsonRequest(to_crs=LAS, feature_collection=api_response.feature_collection)
        api_response = self.api_instance.convert_geo_json(body=n_request, data_partition_id=data_partition_header)
        self.assertIsNotNone(api_response)
        self.assertEquals(api_response.feature_collection.type, 'AnyCrsFeatureCollection')
        c = CompareResponseWithExpectation(api_response.feature_collection, expected=request.feature_collection)
        ok = c.compare_feature_collections()
        self.assertTrue(ok, 'Actual response is different from expected response.')
        #except ApiException as e:
        #    self.fail(str(e))

    def test_any_crs_to_geo_json_ID(self):
        request = self.__read_request('v3/data/AnyCrsGeoJsonPolygon_ID.json')
        data_partition_header = self.api_instance.api_client.default_headers['data_partition_id']
        self.assertIsNotNone(request)
        #try:
            # Convert a GeoJSON or AnyCrsGeoJson structure
        api_response = self.api_instance.convert_geo_json(body=request, data_partition_id=data_partition_header)
        self.assertIsNotNone(api_response)
        self.assertEquals(api_response.feature_collection.type, 'FeatureCollection')
        #  prepare round-trip
        n_request = ConvertGeoJsonRequest(to_crs=LAS, feature_collection=api_response.feature_collection)
        api_response = self.api_instance.convert_geo_json(body=n_request, data_partition_id=data_partition_header)
        self.assertIsNotNone(api_response)
        self.assertEquals(api_response.feature_collection.type, 'AnyCrsFeatureCollection')
        c = CompareResponseWithExpectation(api_response.feature_collection, expected=request.feature_collection)
        ok = c.compare_feature_collections()
        self.assertTrue(ok, 'Actual response is different from expected response.')
        #except ApiException as e:
        #    self.fail(str(e))

    @staticmethod
    def __read_request(file_name):
        dir_path = os.path.dirname(__file__)
        dir_path = os.path.join(dir_path, file_name)
        with open(dir_path) as json_file:
            r_dict = json.loads(convertString(json_file.read()))
        p = AnyCrsGeoJsonPolygon(type=r_dict['featureCollection']['features'][0]['geometry']['type'],
                                 coordinates=r_dict['featureCollection']['features'][0]['geometry']['coordinates'])
        f = AnyCrsGeoJsonFeature(type=r_dict['featureCollection']['features'][0]['type'], geometry=p,
                                 properties=r_dict['featureCollection']['features'][0]['properties'])
        fc = AnyCrsGeoJsonFeatureCollection(type=r_dict['featureCollection']['type'], features=[f])
        if 'persistableReferenceCrs' in r_dict['featureCollection']:
            fc.persistable_reference_crs = r_dict['featureCollection']['persistableReferenceCrs']
        if 'CoordinateReferenceSystemID' in r_dict['featureCollection']:
            fc.coordinate_reference_system_id = r_dict['featureCollection']['CoordinateReferenceSystemID']
        if 'persistableReferenceUnitZ' in r_dict['featureCollection']:
            fc.persistable_reference_unit_z = r_dict['featureCollection']['persistableReferenceUnitZ']
        if 'bbox' in r_dict['featureCollection']:
            fc.bbox = r_dict['featureCollection']['bbox']
        return ConvertGeoJsonRequest(to_crs=r_dict['toCRS'], feature_collection=fc)

    def test_bin_grid_without_TOCRS(self):
        request = self.__read_binGrid_request('v3/data/Convert_BinGrid_WithoutToCRS.json')
        data_partition_header = self.api_instance.api_client.default_headers['data_partition_id']
        self.assertIsNotNone(request)
        try:
            # Convert a BinGrid
            api_response = self.api_instance.convert_bin_grid(body=request, data_partition_id=data_partition_header)
            self.assertIsNotNone(api_response)
            self.assertEquals(api_response.max_mis_location.d_i, 0.0)
            self.assertEquals(api_response.max_mis_location.d_j, 0.0)
        except ApiException as e:
            self.fail(str(e))

    def test_bin_grid_with_TOCRS(self):
        request = self.__read_binGrid_request('v3/data/Convert_BinGrid_With_ToCRS.json')
        data_partition_header = self.api_instance.api_client.default_headers['data_partition_id']
        self.assertIsNotNone(request)
        try:
            # Convert a BinGrid
            api_response = self.api_instance.convert_bin_grid(body=request, data_partition_id=data_partition_header)
            self.assertIsNotNone(api_response)
            self.assertEquals(api_response.max_mis_location.d_i, 0.0)
            self.assertEquals(api_response.max_mis_location.d_j, 0.38)
        except ApiException as e:
            self.fail(str(e))

    def test_bin_grid_with_InvalidRequest(self):
        request = self.__read_binGrid_request('v3/data/Convert_BinGrid_With_InvalidRequest.json')
        data_partition_header = self.api_instance.api_client.default_headers['data_partition_id']
        self.assertIsNotNone(request)
        try:
            # Convert a BinGrid
            api_response = self.api_instance.convert_bin_grid(body=request, data_partition_id=data_partition_header)
            self.assertIsNotNone(api_response)

        except ApiException as e:
            self.assertTrue(400 == e.status)
            self.assertTrue(e.reason in ["Bad Request"])

    def test_bin_grid_with_InvalidCRS(self):
        request = self.__read_binGrid_request('v3/data/Convert_BinGrid_With_InvalidCRS.json')
        data_partition_header = self.api_instance.api_client.default_headers['data_partition_id']
        self.assertIsNotNone(request)
        try:
            # Convert a BinGrid
            api_response = self.api_instance.convert_bin_grid(body=request, data_partition_id=data_partition_header)
            self.assertIsNotNone(api_response)

        except ApiException as e:
            self.assertTrue(400 == e.status)
            self.assertTrue(e.reason in ["Bad Request"])

    def test_bin_grid_with_InvalidSize(self):
        request = self.__read_binGrid_request('v3/data/Convert_BinGrid_With_InvalidSize.json')
        data_partition_header = self.api_instance.api_client.default_headers['data_partition_id']
        self.assertIsNotNone(request)
        try:
            # Convert a BinGrid
            api_response = self.api_instance.convert_bin_grid(body=request, data_partition_id=data_partition_header)
            self.assertIsNotNone(api_response)
        except ApiException as e:
            self.assertTrue(400 == e.status)
            self.assertTrue(e.reason in ["Bad Request"])

    @staticmethod
    def __read_binGrid_request(file_name):
        dir_path = os.path.dirname(__file__)
        dir_path = os.path.join(dir_path, file_name)
        with open(dir_path) as json_file:
            r_dict = json.loads(convertString(json_file.read()))
            inBinGrid = r_dict['inBinGrid']
            if r_dict.get("toCRS"):
                return ConvertBinGridRequest(to_crs=r_dict['toCRS'], in_bin_grid=inBinGrid)
            else:
                return ConvertBinGridRequest(to_crs=None, in_bin_grid=inBinGrid)

class TestTrajectoryConverterIntegration(unittest.TestCase):
    """Post deployment tests for trajectory-converter service"""

    @classmethod
    def setUpClass(cls):
        warnings.filterwarnings("ignore", category=ResourceWarning, message="unclosed.*<ssl.SSLSocket.*>")
        warnings.filterwarnings("ignore", category=ResourceWarning, message="unclosed.*<socket.socket.*>")
        urllib3.disable_warnings()

        cls.env = TestEnvironment()
        if not cls.env.is_ok():
            raise Exception('Test environment is not properly set up; BASE_URL, ROOT_URL, MY_TENANT not set.')
        # Configure API key authorization: api_key
        configuration = Configuration()
        # Set the bearer token; use a service account to do this
        bearer = jwt_client.get_id_token()  # always create a proper bearer token, needed for calls to dps-trajectory
        configuration.api_key['Authorization'] = 'Bearer ' + bearer
        configuration.access_token = bearer
        configuration.verify_ssl = False
        if 'localhost' in cls.env.root_url:
            url = 'http://' + cls.env.root_url + cls.env.base_url
        else:
            url = 'https://' + cls.env.root_url + cls.env.base_url
        data_partition_header_name = 'data_partition_id'
        data_partition_header_value = cls.env.data_partition_id
        client = ApiClient(host=url)
        client.set_default_header(header_name=data_partition_header_name, header_value=data_partition_header_value)
        client.user_agent = 'IntegrationTest'
        cls.api_instance = TrajectoryComputationAndConversionApi(client)
        cls.test_records = TestRecords()
        cls.test_records.setup()

    @classmethod
    def tearDownClass(cls):
        cls.test_records.teardown()

    @staticmethod
    def __read_request(file_name):
        dir_path = os.path.dirname(__file__)
        dir_path = os.path.join(dir_path, file_name)
        with open(dir_path) as json_file:
            request_dict = json.loads(convertString(json_file.read()))
        stations = list()
        for station in request_dict['inputStations']:
            stations.append(TrajectoryStationIn(station['md'], station['inclination'], station['azimuth']))
        value = request_dict['referencePoint']
        reference_point = Point(value['x'], value['y'], value['z'])
        return ConvertTrajectoryRequest(trajectory_crs=request_dict['trajectoryCRS'],
                                        azimuth_reference=request_dict['azimuthReference'],
                                        unit_xy=request_dict['unitXY'],
                                        unit_z=request_dict['unitZ'],
                                        reference_point=reference_point,
                                        input_stations=stations, method=request_dict['method'],
                                        interpolate=request_dict['interpolate'])

    @staticmethod
    def __read_response(file_name):
        dir_path = os.path.dirname(__file__)
        dir_path = os.path.join(dir_path, file_name)
        with open(dir_path) as json_file:
            r_dict = json.loads(json_file.read())
        stations = list()
        for station in r_dict['stations']:
            p_d = station['point']
            p = Point(p_d['x'], p_d['y'], p_d['z'])
            stations.append(TrajectoryStationOut(md=station['md'],
                                                 inclination=station['inclination'],
                                                 azimuth_tn=station['azimuthTN'],
                                                 azimuth_gn=station['azimuthGN'],
                                                 dx_tn=station['dxTN'],
                                                 dy_tn=station['dyTN'],
                                                 point=p,
                                                 wgs84_latitude=station['wgs84Latitude'],
                                                 wgs84_longitude=station['wgs84Longitude'],
                                                 dls=station['dls'],
                                                 original=station['original'],
                                                 dz=station['dz']))
        return ConvertTrajectoryResponse(trajectory_crs=r_dict['trajectoryCRS'],
                                         unit_xy=r_dict['unitXY'],
                                         unit_z=r_dict['unitZ'],
                                         unit_dls=r_dict['unitDls'],
                                         stations=stations,
                                         method=r_dict['method'],
                                         local_crs=r_dict['localCRS'],
                                         operations_applied=r_dict['operationsApplied'])

    def test_32613_TN_trajectory(self):
        request = self.__read_request('v3/data/Trajectory/32613-TN-request.json')
        data_partition_header = self.api_instance.api_client.default_headers['data_partition_id']
        self.assertIsNotNone(request)
        response_expected = self.__read_response('v3/data/Trajectory/32613-TN-response.json')
        self.assertIsNotNone(response_expected)
        try:
            # Convert a list of points
            api_response = self.api_instance.convert_trajectory(body=request, data_partition_id=data_partition_header, _request_timeout=180)
            self.assertIsNotNone(api_response)
            self.assertIsInstance(api_response, ConvertTrajectoryResponse)
            self.assertIsNotNone(api_response.operations_applied)
            self.assertTrue(7 <= len(api_response.operations_applied))
            c = CompareResponseWithExpectation(api_response, expected=response_expected)
            ok = c.compare_convert_trajectory_response()
            self.assertTrue(ok, 'Actual response is different from expected response.')
        except ApiException as e:
            self.fail(str(e))

    def test_32613_TN_trajectory_ID(self):
        request = self.__read_request('v3/data/Trajectory/32613-TN-request_ID.json')
        data_partition_header = self.api_instance.api_client.default_headers['data_partition_id']
        self.assertIsNotNone(request)
        response_expected = self.__read_response('v3/data/Trajectory/32613-TN-response.json')
        self.assertIsNotNone(response_expected)
        try:
            # Convert a list of points
            api_response = self.api_instance.convert_trajectory(body=request, data_partition_id=data_partition_header, _request_timeout=180)
            self.assertIsNotNone(api_response)
            self.assertIsInstance(api_response, ConvertTrajectoryResponse)
            self.assertIsNotNone(api_response.operations_applied)
            self.assertTrue(7 <= len(api_response.operations_applied))
            c = CompareResponseWithExpectation(api_response, expected=response_expected)
            ok = c.compare_convert_trajectory_response()
            self.assertTrue(ok, 'Actual response is different from expected response.')
        except ApiException as e:
            self.fail(str(e))

    def test_32631_LMP_trajectory(self):
        request = self.__read_request('v3/data/Trajectory/32631-LMP-request.json')
        data_partition_header = self.api_instance.api_client.default_headers['data_partition_id']
        self.assertIsNotNone(request)
        response_expected = self.__read_response('v3/data/Trajectory/32631-LMP-response.json')
        self.assertIsNotNone(response_expected)
        try:
            # Convert a list of points
            api_response = self.api_instance.convert_trajectory(body=request, data_partition_id=data_partition_header,  _request_timeout=180)
            self.assertIsNotNone(api_response)
            self.assertIsInstance(api_response, ConvertTrajectoryResponse)
            self.assertIsNotNone(api_response.operations_applied)
            self.assertTrue(7 <= len(api_response.operations_applied))
            c = CompareResponseWithExpectation(api_response, expected=response_expected)
            ok = c.compare_convert_trajectory_response()
            self.assertTrue(ok, 'Actual response is different from expected response.')
        except ApiException as e:
            self.fail(str(e))

    def test_1612_LMP_trajectory(self):
        request = self.__read_request('v3/data/Trajectory/1612-LMP-request.json')
        data_partition_header = self.api_instance.api_client.default_headers['data_partition_id']
        self.assertIsNotNone(request)
        response_expected = self.__read_response('v3/data/Trajectory/1612-LMP-response.json')
        self.assertIsNotNone(response_expected)
        try:
            # Convert a list of points
            api_response = self.api_instance.convert_trajectory(body=request, data_partition_id=data_partition_header,  _request_timeout=180)
            self.assertIsNotNone(api_response)
            self.assertIsInstance(api_response, ConvertTrajectoryResponse)
            self.assertIsNotNone(api_response.operations_applied)
            c = CompareResponseWithExpectation(api_response, expected=response_expected)
            ok = c.compare_convert_trajectory_response()
            self.assertTrue(ok, 'Actual response is different from expected response.')
        except ApiException as e:
            self.fail(str(e))

class TestUnAuthorizedCrsConverterIntegration(unittest.TestCase):
    """Post deployment tests for crs-converter service"""

    @classmethod
    def setUpClass(cls):
        warnings.filterwarnings("ignore", category=ResourceWarning, message="unclosed.*<ssl.SSLSocket.*>")
        warnings.filterwarnings("ignore", category=ResourceWarning, message="unclosed.*<socket.socket.*>")
        urllib3.disable_warnings()

        cls.env = TestEnvironment()
        if not cls.env.is_ok():
            raise Exception('Test environment is not properly set up;  BASE_URL, ROOT_URL, MY_TENANT not set.')
        # Configure API key authorization: api_key
        configuration = Configuration()
        # Set the bearer token; use a service account to do this
        bearer = jwt_client.get_invalid_token() # temporary fix to avoid 500 response from java application using invalid token from jwt_client
        bearer = ''
        configuration.api_key['Authorization'] = 'Bearer ' + bearer
        print(bearer)
        configuration.access_token = bearer
        configuration.verify_ssl = False
        if 'localhost' in cls.env.root_url:
            url = 'http://' + cls.env.root_url + cls.env.base_url
        else:
            url = 'https://' + cls.env.root_url + cls.env.base_url
        data_partition_header_name = 'data_partition_id'
        data_partition_header_value = cls.env.data_partition_id
        client = ApiClient(host=url)
        client.set_default_header(header_name=data_partition_header_name, header_value=data_partition_header_value)
        client.user_agent = 'IntegrationTest'
        cls.api_instance = CRSConversionApi(client)

    def test_transformation_with_unAuthorized_token(self):
        """Read from data/PartialFail.json and convert/transform"""
        reader = TestDataReader('PartialFail.json')
        self.assertIsNotNone(reader)
        request, response = reader.get_request_response(0)
        data_partition_header = self.api_instance.api_client.default_headers['data_partition_id']
        self.assertIsInstance(request, ConvertPointsRequest)
        self.assertIsInstance(response, ConvertPointsResponse)
        try:
            # Convert a list of points
            api_response=self.api_instance.convert_point(body=request, data_partition_id=data_partition_header, _request_timeout=180)
            self.fail(api_response)
        except ApiException as e:
            vendor = self.env.vendor
            if vendor == "azure" or vendor == "ibm":
                reason = e.reason
            else:
                reason = json.loads(e.body)['reason']
            self.assertTrue(403==e.status or 401==e.status)
            self.assertTrue(reason in ["Forbidden", "Unauthorized", "Entitlement Error", "Access denied"])

class TestInfo(unittest.TestCase):
    """Test the info end-points"""
    @classmethod
    def setUpClass(cls):
        warnings.filterwarnings("ignore", category=ResourceWarning, message="unclosed.*<ssl.SSLSocket.*>")
        urllib3.disable_warnings()
        cls.env = TestEnvironment()
        if not cls.env.is_ok():
            raise Exception('Test environment is not properly set up; BASE_URL, ROOT_URL, MY_TENANT not set.')
        configuration = Configuration()
        # Set the bearer token; use a service account to do this
        bearer = jwt_client.get_id_token()
        configuration.api_key['Authorization'] = 'Bearer ' + bearer
        configuration.access_token = bearer
        configuration.verify_ssl = False
        if 'localhost' in cls.env.root_url:
            url = 'http://' + cls.env.root_url + cls.env.base_url
        else:
            url = 'https://' + cls.env.root_url + cls.env.base_url
        data_partition_header_name = 'data_partition_id'
        data_partition_header_value = cls.env.data_partition_id
        client = ApiClient(host=url)
        client.set_default_header(header_name=data_partition_header_name, header_value=data_partition_header_value)
        client.user_agent = 'IntegrationTest'
        cls.api_instance = InfoApiApi(client)

    def test_info_using_get(self):
        """test info_using_get"""
        try:
            api_response = self.api_instance.info_using_get(data_partition_id=self.env.data_partition_id)
            self.assertIsNotNone(api_response)
            self.assertIsInstance(api_response, VersionInfo)
        except ApiException as e:
            self.fail(str(e))

class TestRecords(unittest.TestCase):
    """Test the info end-points"""
    DATA_PARTITION_TO_REPLACE = '{{DATA_PARTITION_ID}}'
    DOMAIN_TO_REPLACE = '{{DOMAIN}}'
    TAG_TO_REPLACE = '{{LEGAL_TAG}}'
    TEST_ID_REPLACE = '{{TEST_ID}}'
    def setup(self):
        warnings.filterwarnings("ignore", category=ResourceWarning, message="unclosed.*<ssl.SSLSocket.*>")
        warnings.filterwarnings("ignore", category=ResourceWarning, message="unclosed.*<socket.socket.*>")
        urllib3.disable_warnings()

        self.env = TestEnvironment()
        if not self.env.is_ok():
            raise Exception('Test environment is not properly set up; BASE_URL, ROOT_URL, MY_TENANT not set.')
        # Configure API key authorization: api_key
        configuration = Configuration()
        # Set the bearer token; use a service account to do this
        bearer = jwt_client.get_id_token()  # always create a proper bearer token, needed for calls to dps-trajectory
        configuration.api_key['Authorization'] = 'Bearer ' + bearer
        configuration.access_token = bearer
        configuration.verify_ssl = False
        self.header = {}
        self.client = ApiClient(host=self.env.storage_url)
        self.header['data-partition-id']=self.env.data_partition_id
        self.header['Content-Type']='application/json'
        self.header['Authorization']='Bearer ' + bearer
        self.header['correlation-id'] = str(uuid.uuid4())
        self.client.user_agent = 'IntegrationTest'
        self.recordIDs = []

        if self.env.vendor == "azure":
            self.ingest_prereq_record_for_tests()
        self.put_records()

    def teardown(self):
        if self.env.vendor == "azure":
            self.delete_records()
        else:
            print('delete_records() will be called after v4 test cases complete in test_crs_converter_v4.py file.')

    def render_record_template(self, record_file_path):
        """render record template"""
        body_str = open(record_file_path, 'r').read()
        body_str = body_str.replace(self.DATA_PARTITION_TO_REPLACE, self.env.data_partition_id)
        body_str = body_str.replace(self.DOMAIN_TO_REPLACE, self.env.my_replace_domain)
        body_str = body_str.replace(self.TAG_TO_REPLACE, self.env.my_legal_tag)
        body_str = body_str.replace(self.TEST_ID_REPLACE, self.env.my_test_id)
        temp = json.loads(body_str)

        if len(temp) == 0:
            self.fail('No records found in the file: ' + record_file_path)

        self.recordIDs.append(temp[0].get('id'))
        return body_str

    def construct_storage_records_url(self):
        """construct storage records url"""
        storage_url = self.env.storage_url.rstrip('/')
        if storage_url.endswith('/records'):
            storage_record_url = storage_url
        else:
            storage_record_url = storage_url + "/records"
        return storage_record_url

    @retry(
        retry=retry_if_exception(retry_if_status_code),
        stop=stop_after_attempt(MAX_RETRY_ATTEMPTS),
        wait=wait_exponential(min=MIN_RETRY_WAIT_IN_SECONDS, max=MAX_RETRY_WAIT_IN_SECONDS)
    )
    def send_api_request(self, url, method="GET", headers=None, body=None):
        """send api request"""
        try:
            api_response = self.client.request(method=method, url=url, headers=headers, body=body)
            return api_response
        except ApiException as e:
            raise

    def check_records(self, expect_records_deletion=False):
        """check the existent of the records"""
        for record_id in self.recordIDs:
            storage_get_record_by_id_url = self.construct_storage_records_url() + '/' + record_id
            try:
                api_response = self.send_api_request(
                    url=storage_get_record_by_id_url,
                    method="GET",
                    headers=self.header
                )
                self.assertIsNotNone(api_response)
            except ApiException as e:
                if not expect_records_deletion:
                    self.fail(f'Failed to retrieve the record {record_id} due to ' + str(e))
                self.assertEqual(e.status, HTTPStatus.NOT_FOUND)

    def ingest_prereq_record_for_tests(self):
        """ingest prerequisite record for tests"""

        logger.info(f'Ingesting initial {INGEST_PREREQ_DATA_RECORD_FILE_NAME} record for tests.')
        body_str = self.render_record_template(INGEST_PREREQ_DATA_RECORD_FILE_PATH)
        try:
            api_response = self.send_api_request(method='PUT', url=self.construct_storage_records_url(), body=json.loads(body_str), headers=self.header)
            self.assertIsNotNone(api_response)
        except ApiException as e:
            self.fail(f'Failed to ingest initial test {INGEST_PREREQ_DATA_RECORD_FILE_NAME} record due to ' + str(e))

        # Ensure that the initial record is available
        self.check_records()

    def put_records(self):
        """test put records"""
        files = [os.path.join(STORAGE_DATA_RECORDS_FILE_PATH, f) for f in listdir(STORAGE_DATA_RECORDS_FILE_PATH)
             if isfile(join(STORAGE_DATA_RECORDS_FILE_PATH, f)) and f != INGEST_PREREQ_DATA_RECORD_FILE_NAME]

        storage_put_record_url = self.construct_storage_records_url()

        logger.info('Request URL for upsert records: ' + storage_put_record_url)
        for file_name in files:
            body_str = self.render_record_template(file_name)
            try:
                api_response = self.send_api_request(method='PUT', url=storage_put_record_url, body=json.loads(body_str), headers=self.header)
                self.assertIsNotNone(api_response)
            except ApiException as e:
                self.fail(f'Failed to ingest {os.path.basename(file_name)} record due to ' + str(e))

        # Ensure that the records are available
        self.check_records()

    def delete_records(self):
        """test delete records"""
        storage_delete_record_url = self.construct_storage_records_url()
        logger.info('Request URL for delete records: ' + storage_delete_record_url)
        for id in self.recordIDs:
            try:
                delete_url = storage_delete_record_url + '/' + id
                api_response = self.send_api_request(method='DELETE', url=delete_url, headers=self.header)
                self.assertIsNotNone(api_response)
            except ApiException as e:
                self.fail(f'Failed to delete {id} record due to ' + str(e))

        # Ensure that the records are deleted
        self.check_records(expect_records_deletion=True)

def suite():
    suite = unittest.TestSuite()
    suite.addTest(TestCrsConverterIntegration('test_conversion_only'))
    suite.addTest(TestCrsConverterIntegration('test_transformation_with_duplicate_points'))
    suite.addTest(TestCrsConverterIntegration('test_geo_json_to_any_crs'))
    suite.addTest(TestCrsConverterIntegration('test_any_crs_to_geo_json'))
    suite.addTest(TestTrajectoryConverterIntegration('test_32613_TN_trajectory'))
    suite.addTest(TestTrajectoryConverterIntegration('test_32631_LMP_trajectory'))
    suite.addTest(TestUnAuthorizedCrsConverterIntegration('test_transformation_with_unAuthorized_token'))
    suite.addTest(TestInfo('test_info_using_get'))
    suite.addTest(TestCrsConverterIntegration('test_conversion_only_ID'))
    suite.addTest(TestCrsConverterIntegration('test_convert_check_WGS84_to_case'))
    suite.addTest(TestCrsConverterIntegration('test_any_crs_to_geo_json_ID'))
    suite.addTest(TestCrsConverterIntegration('test_bin_grid_without_TOCRS'))
    suite.addTest(TestCrsConverterIntegration('test_bin_grid_with_TOCRS'))
    suite.addTest(TestCrsConverterIntegration('test_bin_grid_with_InvalidRequest'))
    suite.addTest(TestCrsConverterIntegration('test_bin_grid_with_InvalidCRS'))
    suite.addTest(TestCrsConverterIntegration('test_bin_grid_with_InvalidSize'))
    suite.addTest(TestTrajectoryConverterIntegration('test_32613_TN_trajectory_ID'))
    return suite

if __name__ == '__main__':
      runner = unittest.TextTestRunner(failfast=True)
      runner.run(suite())
