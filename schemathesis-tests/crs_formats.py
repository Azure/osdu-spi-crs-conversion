"""Generate semantic reference values while retaining positive and negative API checks."""
import json

import schemathesis
from hypothesis import strategies as st


@schemathesis.hook
def before_load_schema(context, raw_schema):
    """Do not negate numeric precision annotations as validation constraints.

    OpenAPI's double hint does not invalidate finite JSON numbers, including
    subnormal doubles. Keep types and explicit bounds available for negative tests.
    """
    def visit(node):
        if isinstance(node, dict):
            if node.get("type") == "number" and node.get("format") == "double":
                node.pop("format")
            for value in node.values():
                visit(value)
        elif isinstance(node, list):
            for value in node:
                visit(value)

    visit(raw_schema)

# Standalone WKT avoids requiring catalog records or an EPSG operation database.
GEOGRAPHIC_WKT = ('GEOGCS["WGS 84",DATUM["WGS_1984",'
                  'SPHEROID["WGS 84",6378137,298.257223563]],'
                  'PRIMEM["Greenwich",0],UNIT["degree",0.0174532925199433]]')
PROJECTED_WKT = ('PROJCS["WGS 84 / UTM zone 31N",' + GEOGRAPHIC_WKT + ','
                 'PROJECTION["Transverse_Mercator"],PARAMETER["latitude_of_origin",0],'
                 'PARAMETER["central_meridian",3],PARAMETER["scale_factor",0.9996],'
                 'PARAMETER["false_easting",500000],PARAMETER["false_northing",0],'
                 'UNIT["metre",1]]')

CRS_REFERENCES = tuple(json.dumps({"wkt": wkt, "type": "LBC", "name": name, "ver": "PE_10_3_1"})
                       for name, wkt in (("WGS 84", GEOGRAPHIC_WKT),
                                         ("WGS 84 / UTM zone 31N", PROJECTED_WKT)))
UNIT_REFERENCES = tuple(json.dumps({"scaleOffset": {"scale": scale, "offset": 0.0},
                                   "symbol": symbol,
                                   "baseMeasurement": {"ancestry": "Length", "type": "UM"},
                                   "type": "USO"})
                        for symbol, scale in (("m", 1.0), ("ft", 0.3048)))

schemathesis.openapi.format("osdu-crs-reference", st.sampled_from(CRS_REFERENCES))
schemathesis.openapi.format("osdu-unit-reference", st.sampled_from(UNIT_REFERENCES))


def _finite_float(value):
    if isinstance(value, bool) or not isinstance(value, (int, float)):
        return None
    try:
        number = float(value)
    except OverflowError:
        return None
    if number != number or abs(number) == float("inf"):
        return None
    return number


def _bin_grid_corners_span(body):
    """Keep positive bin-grid cases that the service can square.

    A zero inline or crossline span, or a zero bin width, is rejected with 400.
    Bin-grid numbers stay plain JSON numbers, so that rejection is not a NaN response.
    """
    if not isinstance(body, dict):
        return True
    try:
        features = body["inBinGrid"]["ABCDBinGridSpatialLocation"]["AsIngestedCoordinates"]["features"]
    except (KeyError, TypeError):
        return True
    if not isinstance(features, list) or len(features) != 4:
        return True
    corners = []
    for feature in features:
        try:
            point = feature["properties"]["PointProperties"][0]
            coordinates = feature["geometry"]["coordinates"]
            corners.append((point["Inline"], point["Crossline"], coordinates[0], coordinates[1]))
        except (KeyError, TypeError, IndexError):
            return True
    finite = []
    for inline, crossline, easting, northing in corners:
        values = tuple(_finite_float(part) for part in (inline, crossline, easting, northing))
        if any(part is None for part in values):
            return False
        finite.append(values)
    ordered = sorted(finite, key=lambda item: (item[0], item[1]))
    inline_span = ordered[2][0] - ordered[0][0]
    crossline_span = ordered[1][1] - ordered[0][1]
    if inline_span == 0 or crossline_span == 0:
        return False

    def distance(left, right):
        return ((left[2] - right[2]) ** 2 + (left[3] - right[3]) ** 2) ** 0.5

    try:
        width_i = (distance(ordered[0], ordered[2]) + distance(ordered[1], ordered[3])) / 2 / inline_span
        width_j = (distance(ordered[0], ordered[1]) + distance(ordered[2], ordered[3])) / 2 / crossline_span
    except (OverflowError, ZeroDivisionError):
        return False
    return width_i != 0 and width_j != 0 and _finite_float(width_i) is not None and _finite_float(width_j) is not None


def _is_bin_grid(context):
    operation = getattr(context, "operation", None)
    path = getattr(operation, "path", "") or ""
    return str(path).endswith("/convertBinGrid")


@schemathesis.hook
def filter_body(context, body):
    if not _is_bin_grid(context):
        return True
    return _bin_grid_corners_span(body)


@schemathesis.hook
def filter_case(context, case):
    # Coverage builds cases directly and does not pass them through filter_body.
    if not _is_bin_grid(context):
        return True
    return _bin_grid_corners_span(getattr(case, "body", None))
