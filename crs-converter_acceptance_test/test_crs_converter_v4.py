import json
import os
import unittest
import allure
import warnings

from os import listdir
from os.path import isfile, join
from time import sleep

import jwt_client
from utility import TestEnvironment
from v4.swagger_client import Configuration, ApiClient, ConvertTrajectoryRequestV4, \
    TrajectoryComputationAndConversionv4EXPERIMENTALApi
from v4.swagger_client.rest import ApiException

import urllib3


urllib3.disable_warnings()


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


@allure.feature('CRS Converter Trajectory Conversion')
@allure.epic('CRS Converter v4 Integration Tests')
class TestTrajectoryConverterIntegrationV4(unittest.TestCase):
    """Post deployment tests for v4 trajectory-converter service"""

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
        url = cls.env.service_url()
        data_partition_header_name = 'data_partition_id'
        data_partition_header_value = cls.env.data_partition_id
        client = ApiClient(host=url)
        client.set_default_header(header_name=data_partition_header_name, header_value=data_partition_header_value)
        client.user_agent = 'IntegrationTest'
        cls.api_instance = TrajectoryComputationAndConversionv4EXPERIMENTALApi(client)
        cls.test_records = TestRecords()
        cls.test_records.setup()

    @classmethod
    def tearDownClass(cls):
        cls.test_records.teardown()

    def test_convertTrajectoryForLMPGeographicCRS_GN_WithSuccess(self):
        request = self.__read_v4_convert_trajectory_request(
            'v4/data/LMPGeographicCRS_GN_WithSuccess.json')
        data_partition_header = self.api_instance.api_client.default_headers['data_partition_id']
        self.assertIsNotNone(request)
        try:
            # convert_trajectory
            api_response = self.api_instance.convert_trajectory(body=request,
                                                                data_partition_id=data_partition_header)
            self.assertIsNotNone(api_response)
            self.assertIsNotNone(api_response.operations_applied)
            self.assertTrue(len(api_response.operations_applied) <= 7)
        except ApiException as e:
            self.fail(str(e))

    def test_convertTrajectoryForAzimuthalEquidistantProjectedCRS_GN_WithSuccess(self):
        request = self.__read_v4_convert_trajectory_request(
            'v4/data/AzimuthalEquidistantProjectedCRS_GN_WithSuccess.json')
        data_partition_header = self.api_instance.api_client.default_headers['data_partition_id']
        self.assertIsNotNone(request)
        try:
            # convert_trajectory
            api_response = self.api_instance.convert_trajectory(body=request,
                                                                data_partition_id=data_partition_header)
            self.assertIsNotNone(api_response)
            self.assertEqual(api_response.scale_convergence_list[0].scalefactor, 0.999723)
            self.assertEqual(api_response.scale_convergence_list[0].convergence, -1.47055)
            self.assertEqual(api_response.scale_convergence_list[1].scalefactor, 0.999699)
            self.assertEqual(api_response.scale_convergence_list[1].convergence, -1.32361)
        except ApiException as e:
            self.fail(str(e))
            
    def test_dls_convertTrajectory(self):
        request = self.__read_v4_convert_trajectory_request(
            'v4/data/Dls_InputRequest.json')
        data_partition_header = self.api_instance.api_client.default_headers['data_partition_id']
        self.assertIsNotNone(request)
        try:
            api_response = self.api_instance.convert_trajectory(body=request,
                                                                data_partition_id=data_partition_header)
            self.assertIsNotNone(api_response)
            self.assertEqual(api_response.stations[len(api_response.stations)-1].dls, 0.0)
            self.assertEqual(api_response.stations_i[len(api_response.stations_i)-1].dls, 0.0)
        except ApiException as e:
            self.fail(str(e))
            
    def test_convertTrajectoryForMDI(self):
        request = self.__read_v4_convert_trajectory_request(
            'v4/data/MDInterpolateRequest.json')
        data_partition_header = self.api_instance.api_client.default_headers['data_partition_id']
        error_msg = 'Both md_i array and md_interval values are provided in the input.'
        self.assertIsNotNone(request)
        try:
            # convert_trajectory
            api_response = self.api_instance.convert_trajectory(body=request,
                                                                data_partition_id=data_partition_header)
            self.assertIsNotNone(api_response)
        except ApiException as e:
            self.assertTrue(400 == json.loads(e.body)['code'])
            self.assertTrue(error_msg == json.loads(e.body)['message'])
    
    def test_convertTrajectoryForMDI_Out_Of_Range(self):
        request = self.__read_v4_convert_trajectory_request(
            'v4/data/MDValuesOutOfRange.json')
        data_partition_header = self.api_instance.api_client.default_headers['data_partition_id']
        error_msg = 'md_i array values provided are not in range of MD stations.'
        self.assertIsNotNone(request)
        try:
            # convert_trajectory
            api_response = self.api_instance.convert_trajectory(body=request,
                                                                data_partition_id=data_partition_header)
            self.assertIsNotNone(api_response)
        except ApiException as e:
            self.assertTrue(400 == json.loads(e.body)['code'])
            self.assertTrue(error_msg == json.loads(e.body)['message'])

    def test_convertTrajectoryFor_INC_ONLY_Success(self):
        request = self.__read_v4_convert_trajectory_request(
            'v4/data/ConvertTrajectoryFor_INC_ONLY.json')
        data_partition_header = self.api_instance.api_client.default_headers['data_partition_id']
        self.assertIsNotNone(request)
        try:
            # convert_trajectory
            api_response = self.api_instance.convert_trajectory(body=request,
                                                                data_partition_id=data_partition_header)
            self.assertIsNotNone(api_response)
            self.assertEqual(api_response.stations[0].dx_tn, 0.0)
            self.assertEqual(api_response.stations[0].dy_tn, 0.0)
            self.assertEqual(api_response.stations[0].point.x, 400000.0000000041)
            self.assertEqual(api_response.stations[0].point.y, 2999999.9999999115)
        except ApiException as e:
            self.fail(str(e))

    def test_convertTrajectoryFor_InverseMinimumCurvature_ONLY_Success(self):
        request = self.__read_v4_convert_trajectory_request(
            'v4/data/ConvertTrajectoryFor_InverseMinimumCurvature.json')
        data_partition_header = self.api_instance.api_client.default_headers['data_partition_id']
        self.assertIsNotNone(request)

        try:
            api_response = self.api_instance.convert_trajectory(body=request,
                                                                data_partition_id=data_partition_header)
            self.assertIsNotNone(api_response)
            self.assertIsNotNone(api_response.operations_applied)
            self.assertTrue(
                api_response.operations_applied[0] == "Input dX_dY_dZ .  Applying inverse minimum curvature "
                                                      "to compute Md_Incl_Azim")
        except ApiException as e:
            self.fail(str(e))
    @staticmethod
    def __read_v4_convert_trajectory_request(file_name):
        dir_path = os.path.dirname(__file__)
        dir_path = os.path.join(dir_path, file_name)
        with open(dir_path) as json_file:
            r_dict = json.loads(convertString(json_file.read()))
            if r_dict.get("unitXY") and r_dict.get("MD_i") is None:
                return ConvertTrajectoryRequestV4(trajectory_crs=r_dict['trajectoryCRS'],
                                                  azimuth_reference=r_dict['azimuthReference'],
                                                  unit_xy=r_dict['unitXY'], unit_z=r_dict['unitZ'],
                                                  reference_point=r_dict['referencePoint'],
                                                  input_stations=r_dict['inputStations'], method=r_dict['method'],
                                                  input_kind=r_dict['inputKind'], interpolate=r_dict['interpolate'])
            elif r_dict.get("MD_i") and r_dict.get("unitXY"):
                return ConvertTrajectoryRequestV4(trajectory_crs=r_dict['trajectoryCRS'],
                                                  azimuth_reference=r_dict['azimuthReference'],
                                                  unit_xy=r_dict['unitXY'], unit_z=r_dict['unitZ'],
                                                  reference_point=r_dict['referencePoint'],
                                                  input_stations=r_dict['inputStations'], method=r_dict['method'],
                                                  input_kind=r_dict['inputKind'], interpolate=r_dict['interpolate'],
                                                  md_i=r_dict['MD_i'])
            elif r_dict.get("MD_i") and r_dict.get("unitXY") is None:
                return ConvertTrajectoryRequestV4(trajectory_crs=r_dict['trajectoryCRS'],
                                                  azimuth_reference=r_dict['azimuthReference'],
                                                  unit_z=r_dict['unitZ'], reference_point=r_dict['referencePoint'],
                                                  input_stations=r_dict['inputStations'], method=r_dict['method'],
                                                  input_kind=r_dict['inputKind'], interpolate=r_dict['interpolate'],
                                                  md_i=r_dict['MD_i'])
            elif r_dict.get('inputKind') == 'MD_Incl':
                return ConvertTrajectoryRequestV4(trajectory_crs=r_dict['trajectoryCRS'],
                                                  unit_z=r_dict['unitZ'], reference_point=r_dict['referencePoint'],
                                                  input_stations=r_dict['inputStations'], method=r_dict['method'],
                                                  input_kind=r_dict['inputKind'], interpolate=r_dict['interpolate'])
            elif r_dict.get('inputKind') == 'dX_dY_dZ' and r_dict.get("unit_xy") is not None:
                return ConvertTrajectoryRequestV4(trajectory_crs=r_dict['trajectoryCRS'],
                                                  unit_xy=r_dict['unitXY'],
                                                  unit_z=r_dict['unitZ'],
                                                  unit_md=r_dict['unitMD'], reference_point=r_dict['referencePoint'],
                                                  input_stations=r_dict['inputStations'], method=r_dict['method'],
                                                  input_kind=r_dict['inputKind'], interpolate=r_dict['interpolate'])
            else:
                return ConvertTrajectoryRequestV4(trajectory_crs=r_dict['trajectoryCRS'],
                                                  azimuth_reference=r_dict['azimuthReference'],
                                                  unit_z=r_dict['unitZ'], reference_point=r_dict['referencePoint'],
                                                  input_stations=r_dict['inputStations'], method=r_dict['method'],
                                                  input_kind=r_dict['inputKind'], interpolate=r_dict['interpolate'])


@allure.feature('CRS Converter Records')
@allure.epic('CRS Converter v4 Integration Tests')
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

        self.storage_url = self.construct_storage_records_url()
        self.client = ApiClient(host=self.storage_url)
        self.header['data-partition-id'] = self.env.data_partition_id
        self.header['Content-Type'] = 'application/json'
        self.header['Authorization'] = 'Bearer ' + bearer
        self.client.user_agent = 'IntegrationTest'
        self.recordIDs = []
        self.ensure_legal_tag()
        self.put_records()

    def teardown(self):
        self.delete_records()

    def ensure_legal_tag(self):
        """Create MY_LEGAL_TAG via Legal API when missing (201/409 OK)."""
        if not self.env.my_legal_tag or self.env.my_legal_tag == 'NOT_FOUND':
            return
        if not self.env.legal_url or self.env.legal_url == 'NOT_FOUND':
            self.fail('LEGAL_URL could not be resolved from VIRTUAL_SERVICE_HOST_NAME')
        short_name = self.env.legal_tag_short_name()
        if not short_name:
            self.fail('MY_LEGAL_TAG is empty after stripping partition prefix')
        template_path = os.path.join(os.path.dirname(__file__), 'v3', 'data', 'LegalTag.json')
        body = json.loads(open(template_path, 'r').read().replace('{{legal_tag_name}}', short_name))
        print(f'Ensuring legal tag {short_name} via {self.env.legal_url}')
        try:
            self.client.request(
                method='POST', url=self.env.legal_url, body=body, headers=self.header
            )
        except ApiException as e:
            if e.status == 409:
                print(f'Legal tag {short_name} already exists (409)')
                return
            self.fail(f'Failed to create legal tag {short_name}: {e}')

    def construct_storage_records_url(self):
        """construct storage records url"""
        storage_url = self.env.storage_url.rstrip('/')
        if storage_url.endswith('/records'):
            return storage_url
        return storage_url + "/records"

    def put_records(self):
        """test put records"""
        dir_path = os.path.dirname(__file__)
        # use v3 records for testing v4 service
        mypath = os.path.join(dir_path, "v3/data/Storagerecords/")
        files = [os.path.join(mypath, f) for f in listdir(mypath) if isfile(join(mypath, f))]
        print('Request URL for upsert records: ' + self.storage_url)
        for file_name in files:
            body_str = open(file_name, 'r').read()
            body_str = body_str.replace(self.DATA_PARTITION_TO_REPLACE, self.env.data_partition_id)
            body_str = body_str.replace(self.DOMAIN_TO_REPLACE, self.env.my_replace_domain)
            body_str = body_str.replace(self.TAG_TO_REPLACE, self.env.my_legal_tag)
            body_str = body_str.replace(self.TEST_ID_REPLACE, self.env.my_test_id)
            temp = json.loads(body_str)
            record_id = temp[0].get('id')
            self.recordIDs.append(record_id)

            try:
                api_response = self.client.request(method='PUT', url=self.storage_url, body=json.loads(body_str), headers=self.header)
                self.assertIsNotNone(api_response)
            except ApiException as e:
                self.fail(f'Failed to ingest {os.path.basename(file_name)} record {record_id} due to {e}')

        # Verify records are readable via Storage GET before tests run
        for record_id in self.recordIDs:
            try:
                get_url = self.storage_url + '/' + record_id
                api_response = self.client.request(method='GET', url=get_url, headers=self.header, body=None)
                self.assertIsNotNone(api_response)
            except ApiException as e:
                self.fail(f'Record {record_id} not readable after PUT: {e}')
        sleep(5)

    """deleting records for v3 & v4 test cases"""
    def delete_records(self):
        """test delete records"""
        print('Request URL for delete records: ' + self.storage_url)
        for id in self.recordIDs:
            try:
                delete_url = self.storage_url + '/' + id
                api_response = self.client.request('DELETE', url=delete_url, headers=self.header, body=None)
                self.assertIsNotNone(api_response)
            except ApiException as e:
                if e.status == 404:
                    print(f'Record {id} already absent during teardown')
                    continue
                self.fail(f'Failed to delete {id} record due to {e}')


def suite():
    suite = unittest.TestSuite()
    suite.addTest(
        TestTrajectoryConverterIntegrationV4('test_convertTrajectoryForLMPGeographicCRS_GN_WithSuccess'))
    suite.addTest(
        TestTrajectoryConverterIntegrationV4(
            'test_convertTrajectoryForAzimuthalEquidistantProjectedCRS_GN_WithSuccess'))
    suite.addTest(TestTrajectoryConverterIntegrationV4(
                        'test_convertTrajectoryForMDI'))
    suite.addTest(TestTrajectoryConverterIntegrationV4(
                            'test_convertTrajectoryForMDI_Out_Of_Range'))
    suite.addTest(
        TestTrajectoryConverterIntegrationV4(
            'test_dls_convertTrajectory'))
    suite.addTest(
        TestTrajectoryConverterIntegrationV4(
            'test_convertTrajectoryFor_INC_ONLY_Success'))
    suite.addTest(
        TestTrajectoryConverterIntegrationV4(
            'test_convertTrajectoryFor_InverseMinimumCurvature_ONLY_Success'))        
    return suite


if __name__ == '__main__':
    runner = unittest.TextTestRunner(failfast=True)
    runner.run(suite())
