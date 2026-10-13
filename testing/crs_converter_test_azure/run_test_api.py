import sys
sys.path.append("..")

from crs_converter_test_core.test_crs_converter_v2 import *

if __name__ == '__main__':
    import xmlrunner
    unittest.main(testRunner=xmlrunner.XMLTestRunner(output='test-reports/v2', outsuffix='azure'))
