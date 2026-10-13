package org.opengroup.osdu.crs.sis;

import java.util.List;
import org.apache.sis.metadata.iso.citation.Citations;
import org.apache.sis.referencing.CRS;
import org.apache.sis.referencing.IdentifiedObjects;
import org.opengis.metadata.Identifier;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.Conversion;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.Projection;
import org.opengis.referencing.operation.Transformation;
import org.opengroup.osdu.crs.model.ICrs;
import org.opengroup.osdu.crs.model.Impl.AuthorityCode;
import org.opengroup.osdu.crs.sis.wkt.WktParser;
import org.opengroup.osdu.crs.sis.wkt.WktSection;

public class SisCrsUtils {
//check to see if only a projection is needed to convert from fromCrs to toCrs

    public static boolean isValidConversionRoute(ICrs fromCrs, ICrs toCrs) {
        ISisCrs fromBaseSISCrs = fromCrs.getBaseGeographicCrs();
        ISisCrs toBaseSISCrs = toCrs.getBaseGeographicCrs();
        AuthorityCode fromBaseAuthorityCode = fromBaseSISCrs.getAuthorityCode();
        AuthorityCode toBaseAuthorityCode = toBaseSISCrs.getAuthorityCode();
        if (AuthorityCodeUtils.isEpsgCode(fromBaseAuthorityCode) && AuthorityCodeUtils.isEpsgCode(toBaseAuthorityCode)) {
            return fromBaseAuthorityCode.getCode().equals(toBaseAuthorityCode.getCode());
        }
        Identifier fromBaseIdentifier = IdentifiedObjects.getIdentifier(fromBaseSISCrs.getCoordinateReferenceSystem(),
                Citations.EPSG);
        Identifier toBaseIdentifier = IdentifiedObjects.getIdentifier(toBaseSISCrs.getCoordinateReferenceSystem(),
                Citations.EPSG);
        if (fromBaseIdentifier != null && toBaseIdentifier != null) {
            return fromBaseIdentifier.getCode().equals(toBaseIdentifier.getCode());
        }
        if (fromBaseSISCrs.getCoordinateReferenceSystem().equals(toBaseSISCrs.getCoordinateReferenceSystem())) {
            return true;
        }
        try {
            CoordinateReferenceSystem fromCoordinateReferenceSystem = fromBaseSISCrs.getCoordinateReferenceSystem();
            CoordinateReferenceSystem toCoordinateReferenceSystem = toBaseSISCrs.getCoordinateReferenceSystem();
            List<CoordinateOperation> operations = CRS.findOperations(fromCoordinateReferenceSystem, toCoordinateReferenceSystem, null);
            if (operations.size() == 1) {
                CoordinateOperation coordinateOperation = operations.get(0);
                if (coordinateOperation instanceof Transformation) {
                    if (coordinateOperation.getMathTransform().equals(coordinateOperation.getMathTransform().inverse())) {
                        return true;
                    }
                    ReferenceIdentifier nameIdentifer = coordinateOperation.getName();
                    if (nameIdentifer != null && nameIdentifer.getCode() != null && nameIdentifer.getCode().equals("Ellipsoid change")) {
                        //there can be some minor precision changes when importing from wkt, check the original ellipsoid values in the
                        //wkt to see if only a conversion between fromCrs and toCrs is needed
                        String fromWkt = fromBaseSISCrs.getWkt();
                        if (fromCrs.getProjectedCrs() != null) {
                            fromWkt = fromCrs.getProjectedCrs().getWkt();
                        }

                        String toWkt = toBaseSISCrs.getWkt();
                        if (toCrs.getProjectedCrs() != null) {
                            toWkt = toCrs.getProjectedCrs().getWkt();
                        }

                        WktParser parser = new WktParser();
                        WktSection fromWktSection = parser.parseWkt(fromWkt);
                        WktSection toWktSection = parser.parseWkt(toWkt);
                        WktSection fromWktDatum = fromWktSection.getSubsection("DATUM");
                        WktSection toWktDatum = toWktSection.getSubsection("DATUM");
                        if (fromWktDatum.equals(toWktDatum)) {
                            return true;
                        }
                    }
                    return false;
                } else if (coordinateOperation instanceof Projection) {
                    ReferenceIdentifier nameIdentifer = coordinateOperation.getName();
                    if (nameIdentifer != null && nameIdentifer.getCode() != null && nameIdentifer.getCode().equals("Identity")) {
                        return true;
                    }
                    return false;
                } else if (coordinateOperation instanceof Conversion) {
                    ReferenceIdentifier nameIdentifer = coordinateOperation.getName();
                    if (nameIdentifer != null && nameIdentifer.getCode() != null && nameIdentifer.getCode().equals("Identity")) {
                        return true;
                    }
                    return false;
                }
            }
        } catch (Exception ex) {
            return false;
        }

        return false;
    }
}
