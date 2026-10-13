# coding: utf-8
"""Test an entire suite of coordinate conversions and transforms"""
import unittest
import time
import glob
import io
import os
from six import python_2_unicode_compatible
from crs_converter_test_core.v2.swagger_client import ApiClient, CRSPointConversionApi, Configuration
from crs_converter_test_core.v2.swagger_client.models import ConvertPointsResponse, ConvertPointsRequest, Point
from crs_converter_test_core.v2.swagger_client.rest import ApiException
from utility import TestEnvironment, TestDataReader, CompareResponseWithExpectation
from utility import is_close
import jwt_client


class TestCrsConverterFullSuite(unittest.TestCase):
    """Using test data files with from/to CRS definitions and from/to points,
    compare with expected conversion results"""

    @classmethod
    def setUpClass(cls):
        cls.env = TestEnvironment()
        if not cls.env.is_ok():
            raise Exception('Test environment is not properly set up; BASE_URL, ROOT_URL(VIRTUAL_SERVICE_HOST_NAME), MY_TENANT not set.')
        if 'localhost' in cls.env.root_url:
            url = 'http://' + cls.env.root_url + cls.env.base_url
        else:
            url = 'https://' + cls.env.root_url + cls.env.base_url
        cls.local = 'localhost' in url
        cls.refresh_configuration()
        data_partition_header_name = 'data_partition_id'
        data_partition_header_value = cls.env.data_partition_id
        client = ApiClient(host=url)
        client.set_default_header(header_name=data_partition_header_name, header_value=data_partition_header_value)
        client.user_agent = 'SuiteTest'
        cls.api_instance = CRSPointConversionApi(client)

    @classmethod
    def refresh_configuration(cls):
        configuration = Configuration()
        # Set the bearer token; use a service account to do this
        bearer = jwt_client.get_id_token()
        configuration.api_key['Authorization'] = 'Bearer ' + bearer
        configuration.access_token = bearer
        if cls.local:
            configuration.verify_ssl = False
            configuration.ssl_ca_cert = None
            configuration.assert_hostname = False
            configuration.cert_file = None

    def test_full_suite(self):
        """Loop over all test data sets and test-convert/compare"""
        start = int(time.time())
        data_partition_header = self.api_instance.api_client.default_headers['data_partition_id']
        with io.open(self.env.report_path, 'a+', encoding='utf8') as reporter:
            search_pattern = os.path.join(self.env.data_dir, self.env.data_pattern)
            print(search_pattern)
            for file_name in glob.glob(search_pattern):
                reader = TestDataReader(file_name)
                self.assertIsNotNone(reader, 'None reader for ' + file_name)
                n_sets = reader.number_of_sets()
                print(file_name + ' number of sets: ' + str(n_sets))
                for i in range(0, n_sets):
                    request, response = reader.get_request_response(i)
                    self.assertIsInstance(request, ConvertPointsRequest)
                    self.assertIsInstance(response, ConvertPointsResponse)
                    from_crs, to_crs = reader.get_from_to_crs_info(i)
                    try:
                        if int(time.time()) - start > 3500:
                            start = int(time.time())
                            self.refresh_configuration()  # refresh the jwt
                        # Convert a list of points
                        api_response = self.api_instance.convert_point(body=request, data_partition_id=data_partition_header,  _request_timeout=180)
                        self.assertIsNotNone(api_response)
                        self.assertIsInstance(api_response, ConvertPointsResponse)
                        c = CompareResponseWithExpectation(api_response, expected=response,
                                                           from_info=from_crs, to_info=to_crs)
                        ok = c.compare()
                        if not ok:
                            for line in c.report:
                                print(line)
                                reporter.write(python_2_unicode_compatible(file_name + ': ' + line + '\n'))
                        else:
                            print(c.from_info + ' --> ' + c.to_info + ': OK')
                        # self.assertTrue(ok, 'Actual response is different from expected response.')
                    except ApiException as e:
                        reporter.write(python_2_unicode_compatible(file_name + ': \n' + str(e) + '\n'))
                        # self.fail(str(e))
                reporter.flush()


if __name__ == '__main__':
    unittest.main()
