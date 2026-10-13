# coding: utf-8
"""Integration unit tests for crs-converter"""
import unittest
import os
import math
import json

import urllib3
urllib3.disable_warnings()
import warnings
import logging
logging.basicConfig(level=os.environ.get("LOG_LEVEL", "INFO"))


from crs_converter_test_core.v2.swagger_client import ApiClient, CRSPointConversionApi, TrajectoryComputationAndConversionApi, \
    TrajectoryStationIn, TrajectoryStationOut, Configuration, ConvertTrajectoryResponse, InfoApiApi, InfoApiApiSlash
from crs_converter_test_core.v2.swagger_client.models import ConvertPointsResponse, ConvertPointsRequest, Point, ConvertTrajectoryRequest, \
    ConvertGeoJsonRequest, AnyCrsGeoJsonFeatureCollection, \
    AnyCrsGeoJsonFeature, AnyCrsGeoJsonPolygon, VersionInfo
from crs_converter_test_core.v2.swagger_client.rest import ApiException
from pprint import pprint
from crs_converter_test_core.utility import TestEnvironment
import jwt_client


FROM_CRS = "{\"authCode\":{\"auth\":\"SLB\",\"code\":\"4248009\"},\"type\":\"EBC\",\"ver\":\"PE_10_3_1\",\"name\":\"PSAD56 * DMA-Ven [4248,1209]\",\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4248\"},\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"name\":\"GCS_Provisional_S_American_1956\",\"wkt\":\"GEOGCS[\\\"GCS_Provisional_S_American_1956\\\",DATUM[\\\"D_Provisional_S_American_1956\\\",SPHEROID[\\\"International_1924\\\",6378388.0,297.0]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433],AUTHORITY[\\\"EPSG\\\",4248]]\"},\"singleCT\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"1209\"},\"type\":\"ST\",\"ver\":\"PE_10_3_1\",\"name\":\"PSAD_1956_To_WGS_1984_9\",\"wkt\":\"GEOGTRAN[\\\"PSAD_1956_To_WGS_1984_9\\\",GEOGCS[\\\"GCS_Provisional_S_American_1956\\\",DATUM[\\\"D_Provisional_S_American_1956\\\",SPHEROID[\\\"International_1924\\\",6378388.0,297.0]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Geocentric_Translation\\\"],PARAMETER[\\\"X_Axis_Translation\\\",-295.0],PARAMETER[\\\"Y_Axis_Translation\\\",173.0],PARAMETER[\\\"Z_Axis_Translation\\\",-371.0],AUTHORITY[\\\"EPSG\\\",1209]]\"}}"
# FROM_CRS = "{\"authCode\":{\"auth\":\"SLB\",\"code\":\"4248001\"},\"type\":\"EBC\",\"ver\":\"PE_10_3_1\",\"name\":\"PSAD56 * DMA-mean [4248,1201]\",\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4248\"},\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"name\":\"PSAD56\",\"wkt\":\"GEOGCS[\\\"GCS_Provisional_S_American_1956\\\",DATUM[\\\"D_Provisional_S_American_1956\\\",SPHEROID[\\\"International_1924\\\",6378388.0,297.0]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433],AUTHORITY[\\\"EPSG\\\",4248]]\\n\"},\"singleCT\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"1201\"},\"type\":\"ST\",\"ver\":\"PE_10_3_1\",\"name\":\"PSAD56 to WGS 84 (1)\",\"wkt\":\"GEOGTRAN[\\\"PSAD_1956_To_WGS_1984_1\\\",GEOGCS[\\\"GCS_Provisional_S_American_1956\\\",DATUM[\\\"D_Provisional_S_American_1956\\\",SPHEROID[\\\"International_1924\\\",6378388.0,297.0]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Geocentric_Translation\\\"],PARAMETER[\\\"X_Axis_Translation\\\",-288.0],PARAMETER[\\\"Y_Axis_Translation\\\",175.0],PARAMETER[\\\"Z_Axis_Translation\\\",-376.0],AUTHORITY[\\\"EPSG\\\",1201]]\"}}"
TO___CRS = "{\"authCode\":{\"auth\":\"SLB\",\"code\":\"30200002\"},\"type\":\"EBC\",\"ver\":\"PE_10_3_1\",\"name\":\"Trinidad 1903 * EOG-Tto Trin / Trinidad Grid [30200,10085]\",\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"30200\"},\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"name\":\"Trinidad_1903_Trinidad_Grid\",\"wkt\":\"PROJCS[\\\"Trinidad_1903_Trinidad_Grid\\\",GEOGCS[\\\"GCS_Trinidad_1903\\\",DATUM[\\\"D_Trinidad_1903\\\",SPHEROID[\\\"Clarke_1858\\\",6378293.64520876,294.260676369]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],PROJECTION[\\\"Cassini\\\"],PARAMETER[\\\"False_Easting\\\",430000.0],PARAMETER[\\\"False_Northing\\\",325000.0],PARAMETER[\\\"Central_Meridian\\\",-61.3333333333333],PARAMETER[\\\"Scale_Factor\\\",1.0],PARAMETER[\\\"Latitude_Of_Origin\\\",10.4416666666667],UNIT[\\\"Link_Clarke\\\",0.201166195164],AUTHORITY[\\\"EPSG\\\",30200]]\"},\"singleCT\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"10085\"},\"type\":\"ST\",\"ver\":\"PE_10_3_1\",\"name\":\"Trinidad_1903_To_WGS_1984_2\",\"wkt\":\"GEOGTRAN[\\\"Trinidad_1903_To_WGS_1984_2\\\",GEOGCS[\\\"GCS_Trinidad_1903\\\",DATUM[\\\"D_Trinidad_1903\\\",SPHEROID[\\\"Clarke_1858\\\",6378293.64520876,294.260676369]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Geocentric_Translation\\\"],PARAMETER[\\\"X_Axis_Translation\\\",-61.0],PARAMETER[\\\"Y_Axis_Translation\\\",285.2],PARAMETER[\\\"Z_Axis_Translation\\\",471.6],AUTHORITY[\\\"EPSG\\\",10085]]\"}}"
# TO___CRS = "{\"authCode\":{\"auth\":\"SLB\",\"code\":\"30200001\"},\"type\":\"EBC\",\"ver\":\"PE_10_3_1\",\"name\":\"Trinidad 1903 * Amoco-Tto Trin / Trinidad Grid [30200,1296]\",\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"30200\"},\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"name\":\"Trinidad 1903 / Trinidad Grid\",\"wkt\":\"PROJCS[\\\"Trinidad_1903_Trinidad_Grid\\\",GEOGCS[\\\"GCS_Trinidad_1903\\\",DATUM[\\\"D_Trinidad_1903\\\",SPHEROID[\\\"Clarke_1858\\\",6378293.645208759,294.260676369]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],PROJECTION[\\\"Cassini\\\"],PARAMETER[\\\"False_Easting\\\",430000.0],PARAMETER[\\\"False_Northing\\\",325000.0],PARAMETER[\\\"Central_Meridian\\\",-61.33333333333334],PARAMETER[\\\"Scale_Factor\\\",1.0],PARAMETER[\\\"Latitude_Of_Origin\\\",10.44166666666667],UNIT[\\\"Link_Clarke\\\",0.201166195164],AUTHORITY[\\\"EPSG\\\",30200]]\\n\"},\"singleCT\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"1296\"},\"type\":\"ST\",\"ver\":\"PE_10_3_1\",\"name\":\"Trinidad 1903 to WGS 84 (1)\",\"wkt\":\"GEOGTRAN[\\\"Trinidad_1903_To_WGS_1984\\\",GEOGCS[\\\"GCS_Trinidad_1903\\\",DATUM[\\\"D_Trinidad_1903\\\",SPHEROID[\\\"Clarke_1858\\\",6378293.645208759,294.260676369]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Geocentric_Translation\\\"],PARAMETER[\\\"X_Axis_Translation\\\",-61.702],PARAMETER[\\\"Y_Axis_Translation\\\",284.488],PARAMETER[\\\"Z_Axis_Translation\\\",472.052],AUTHORITY[\\\"EPSG\\\",1296]]\"}}"
WGS84 = "{\"wkt\":\"GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433],AUTHORITY[\\\"EPSG\\\",4326]]\",\"ver\":\"PE_10_3_1\",\"name\":\"GCS_WGS_1984\",\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4326\"},\"type\":\"LBC\"}"
LAS = "{\"lateBoundCRS\":{\"wkt\":\"PROJCS[\\\"NAD_1927_StatePlane_Louisiana_South_FIPS_1702\\\",GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.9786982]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],PROJECTION[\\\"Lambert_Conformal_Conic\\\"],PARAMETER[\\\"False_Easting\\\",2000000.0],PARAMETER[\\\"False_Northing\\\",0.0],PARAMETER[\\\"Central_Meridian\\\",-91.3333333333333],PARAMETER[\\\"Standard_Parallel_1\\\",29.3],PARAMETER[\\\"Standard_Parallel_2\\\",30.7],PARAMETER[\\\"Latitude_Of_Origin\\\",28.6666666666667],UNIT[\\\"Foot_US\\\",0.304800609601219],AUTHORITY[\\\"EPSG\\\",26782]]\",\"ver\":\"PE_10_3_1\",\"name\":\"NAD_1927_StatePlane_Louisiana_South_FIPS_1702\",\"authCode\":{\"auth\":\"EPSG\",\"code\":\"26782\"},\"type\":\"LBC\"},\"singleCT\":{\"wkt\":\"GEOGTRAN[\\\"NAD_1927_To_WGS_1984_79_CONUS\\\",GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.9786982]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"NADCON\\\"],PARAMETER[\\\"Dataset_conus\\\",0.0],AUTHORITY[\\\"EPSG\\\",15851]]\",\"ver\":\"PE_10_3_1\",\"name\":\"NAD_1927_To_WGS_1984_79_CONUS\",\"authCode\":{\"auth\":\"EPSG\",\"code\":\"15851\"},\"type\":\"ST\"},\"ver\":\"PE_10_3_1\",\"name\":\"NAD27 * OGP-Usa Conus / Louisiana South [26782,15851]\",\"authCode\":{\"auth\":\"SLB\",\"code\":\"26782079\"},\"type\":\"EBC\"}"

def is_close(a, b, rel_tol=1e-09, abs_tol=0.0):
    """Compare a double
    https://stackoverflow.com/questions/5595425/what-is-the-best-way-to-compare-floats-for-almost-equality-in-python"""
    if math.isnan(float(a)) and math.isnan(float(b)):
        return True  # we treat NaN as values here
    if math.isnan(float(a)) or math.isnan(float(b)):
        return False
    return abs(a-b) <= max(rel_tol * max(abs(a), abs(b)), abs_tol)


class TestDataReader(object):
    """Read a test data set and return a dictionary"""
    def __init__(self, filename):
        self.__data_set = None
        if filename is not None:
            dir_path = os.path.dirname(__file__)
            dir_path = os.path.join(dir_path,  'v2', 'data', filename)
            try:
                with open(dir_path) as json_file:
                    string = json_file.read()
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
        cls.api_instance = CRSPointConversionApi(client)

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

    def test_geo_json_to_any_crs(self):
        request = self.__read_request('v2/data/GeoJsonPolygon.json')
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
        request = self.__read_request('v2/data/AnyCrsGeoJsonPolygon.json')
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
            r_dict = json.loads(json_file.read())
        p = AnyCrsGeoJsonPolygon(type=r_dict['featureCollection']['features'][0]['geometry']['type'],
                                 coordinates=r_dict['featureCollection']['features'][0]['geometry']['coordinates'])
        f = AnyCrsGeoJsonFeature(type=r_dict['featureCollection']['features'][0]['type'], geometry=p,
                                 properties=r_dict['featureCollection']['features'][0]['properties'])
        fc = AnyCrsGeoJsonFeatureCollection(type=r_dict['featureCollection']['type'], features=[f])
        if 'persistableReferenceCrs' in r_dict['featureCollection']:
            fc.persistable_reference_crs = r_dict['featureCollection']['persistableReferenceCrs']
        if 'persistableReferenceUnitZ' in r_dict['featureCollection']:
            fc.persistable_reference_unit_z = r_dict['featureCollection']['persistableReferenceUnitZ']
        if 'bbox' in r_dict['featureCollection']:
            fc.bbox = r_dict['featureCollection']['bbox']
        return ConvertGeoJsonRequest(to_crs=r_dict['toCRS'], feature_collection=fc)

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

    @staticmethod
    def __read_request(file_name):
        dir_path = os.path.dirname(__file__)
        dir_path = os.path.join(dir_path, file_name)
        with open(dir_path) as json_file:
            request_dict = json.loads(json_file.read())
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
        request = self.__read_request('v2/data/Trajectory/32613-TN-request.json')
        data_partition_header = self.api_instance.api_client.default_headers['data_partition_id']
        self.assertIsNotNone(request)
        response_expected = self.__read_response('v2/data/Trajectory/32613-TN-response.json')
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
        request = self.__read_request('v2/data/Trajectory/32631-LMP-request.json')
        data_partition_header = self.api_instance.api_client.default_headers['data_partition_id']
        self.assertIsNotNone(request)
        response_expected = self.__read_response('v2/data/Trajectory/32631-LMP-response.json')
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
        cls.api_instance = CRSPointConversionApi(client)

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
            VENDOR = os.getenv("VENDOR")
            if VENDOR == "azure" or VENDOR == "ibm":
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


class TestInfoSlash(unittest.TestCase):
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
        cls.api_instance = InfoApiApiSlash(client)

    def test_info_using_get(self):
        """test info_using_get"""
        try:
            api_response = self.api_instance.info_using_get(data_partition_id=self.env.data_partition_id)
            self.assertIsNotNone(api_response)
            self.assertIsInstance(api_response, VersionInfo)
        except ApiException as e:
            self.fail(str(e))



if __name__ == '__main__':
    unittest.main()
