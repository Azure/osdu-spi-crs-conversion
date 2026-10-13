# coding: utf-8
"""Utilities for crs-converter tests"""
import math
import os
import json
import pprint
from crs_converter_test_core.v3.swagger_client.models import ConvertPointsResponse, ConvertPointsRequest, Point
import crs_converter_test_core.constants as constants


def is_close(a, b, rel_tol=1e-09, abs_tol=0.0):
    """Compare a double
    https://stackoverflow.com/questions/5595425/what-is-the-best-way-to-compare-floats-for-almost-equality-in-python"""
    if math.isnan(float(a)) and math.isnan(float(b)):
        return True  # we treat NaN as values here
    try:
        return abs(a-b) <= max(rel_tol * max(abs(a), abs(b)), abs_tol)
    except TypeError:
        return False


class TestEnvironment(object):
    """Container for resolved environment variables"""
    def __init__(self):
        self.base_url = constants.BASE_URL
        self.root_url = constants.ROOT_URL
        self.data_dir = constants.DATA_DIR
        self.data_pattern = constants.DATA_PATTERN
        self.report_path = constants.REPORT_PATH
        self.data_partition_id = constants.MY_TENANT
        self.storage_url = constants.STORAGE_URL
        self.my_replace_domain = constants.MY_REPLACE_DOMAIN
        self.my_test_id = constants.MY_TEST_ID
        self.my_legal_tag = constants.MY_LEGAL_TAG
        self.vendor = constants.VENDOR

    def is_ok(self):
        """Returns true if all expected environment variables are defined."""
        return self.base_url is not None and self.root_url is not None and self.data_partition_id is not None


class TestDataReader(object):
    """Read a test data set and return a dictionary"""
    def __init__(self, filename):
        self.__data_set = None
        if filename is not None:
            try:
                with open(filename) as json_file:
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

    def get_from_to_crs_info(self, index=0):
        """extract the from and to CRS information from the data set"""
        if self.is_ok():
            ds = self.__get_data_by_index(index)
            if ds is not None:
                from_crs = ds['FromInfo']
                to___crs = ds['ToInfo']
                return from_crs, to___crs


class CompareResponseWithExpectation(object):
    """Helper class to compare an actual conversion response to an expected"""
    def __init__(self, actual, expected, from_info, to_info):
        self.actual = actual
        self.expected = expected
        self.from_info = from_info
        self.to_info = to_info
        if '[dega]' in self.to_info or '[grad]' in self.to_info:
            self.abs_tol = math.pi * 1.0e-7
            if '[dega]' in self.to_info:
                self.diff = 360
            else:
                self.diff = 400
        else:
            self.abs_tol = 0.01
            self.diff = 0
        self.report = list()

    def compare(self):
        if isinstance(self.actual, ConvertPointsResponse) and isinstance(self.expected, ConvertPointsResponse):
            same = self.actual.success_count == self.expected.success_count
            same = same and len(self.actual.points) == len(self.expected.points)
            if same:
                for i in range(0, len(self.actual.points)):
                    a = self.actual.points[i]
                    e = self.expected.points[i]
                    if not self.diff == 0:
                        try:
                            if is_close(abs(a.x-e.x), self.diff, abs_tol=self.abs_tol):
                                e.x = a.x
                        except TypeError:  # NaNs and other errors
                            pass
                    close = is_close(a.x, e.x, abs_tol=self.abs_tol) and is_close(a.y, e.y, abs_tol=self.abs_tol)
                    # and is_close(a.z, e.z, abs_tol=self.abs_tol)  # 2D for now.
                    if not close:
                        if len(self.report) == 0:
                            self.report.append(self.from_info + ' --> ' + self.to_info)
                        self.report.append('Different point values at index ' + str(i) + '; actual, expected')
                        self.report.append(pprint.pformat(a))
                        self.report.append(pprint.pformat(e))
                    same = same and close
            return same
        return False
