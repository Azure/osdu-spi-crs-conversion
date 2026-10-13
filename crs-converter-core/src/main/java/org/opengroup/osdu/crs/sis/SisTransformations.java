package org.opengroup.osdu.crs.sis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.sis.metadata.iso.citation.Citations;
import org.apache.sis.referencing.IdentifiedObjects;
import org.opengis.metadata.Identifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengroup.osdu.crs.sis.operation.CRSProjectionOperation;
import org.opengroup.osdu.crs.sis.operation.CRSTransformToWGS84Operation;
import org.opengroup.osdu.crs.sis.operation.OperationResponse;
import org.opengroup.osdu.crs.util.Constants;

public class SisTransformations {

    private static final Logger LOGGER = Logger.getLogger(SisTransformations.class.getName());

    public static void projToGeog(ISisCrs projectionSISCrs, int numberOfPoints, double[] xyPoints) {
        try {
            CoordinateReferenceSystem coordianteReferenceSystem = projectionSISCrs.getCoordinateReferenceSystem();
            if (coordianteReferenceSystem == null) {
                //for now since apache sis does not support azimuth projection
                return;
            }
            if (!(coordianteReferenceSystem instanceof ProjectedCRS)) {
                throw new IllegalArgumentException("crs is not a projection");
            }
            if (xyPoints.length /2 != numberOfPoints) {
                throw new IllegalArgumentException("numberOfPoints does not match what is expected");
            }
            ProjectedCRS projectedCRS = (ProjectedCRS) coordianteReferenceSystem;
            GeographicCRS baseCRS = projectedCRS.getBaseCRS();

            ISisCrs baseSISCrs = new SisCrs(baseCRS, projectionSISCrs.isNegativeScale(), null, null, null);
            CRSProjectionOperation projectionOperation = new CRSProjectionOperation(projectionSISCrs, baseSISCrs, null);

            double[] zValues = new double[numberOfPoints];
            Arrays.fill(zValues, 0, numberOfPoints, 0);

            OperationResponse response = projectionOperation.convertPoints(xyPoints, zValues);
            if (response.getSuccessCount() < numberOfPoints) {
                throw new IllegalArgumentException("Can't convert from proj to geog");
            }
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Can't convert from proj to geog", ex);
        }
    }

    public static void geogToProj(ISisCrs projectionSISCrs, int numberOfPoints, double[] xyPoints) {
        try {
            CoordinateReferenceSystem coordianteReferenceSystem = projectionSISCrs.getCoordinateReferenceSystem();
            if (coordianteReferenceSystem == null) {
                //for now since apache sis does not support azimuth projection
                return;
            }
            if (!(coordianteReferenceSystem instanceof ProjectedCRS)) {
                throw new IllegalArgumentException("crs is not a projection");
            }
            if (xyPoints.length /2 != numberOfPoints) {
                throw new IllegalArgumentException("numberOfPoints does not match what is expected");
            }
            ProjectedCRS projectedCRS = (ProjectedCRS) coordianteReferenceSystem;
            GeographicCRS baseCRS = projectedCRS.getBaseCRS();

            ISisCrs baseSISCrs = new SisCrs(baseCRS, projectionSISCrs.isNegativeScale(), null, null, null);
            CRSProjectionOperation projectionOperation = new CRSProjectionOperation(baseSISCrs, projectionSISCrs, null);

            double[] zValues = new double[numberOfPoints];
            Arrays.fill(zValues, 0, numberOfPoints, 0);
            OperationResponse response = projectionOperation.convertPoints(xyPoints, zValues);
            if (response.getSuccessCount() < numberOfPoints) {
                throw new IllegalArgumentException("Can't convert from proj to geog");
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("Can't convert from geog to proj", ex);
        }
    }

    /**
     * Returns do_reverse=true based on the score i.e. means do reverse direction else forward direction.
     * @param transformSourceCRS - sourceCRS
     * @param transformTargetCRS - targetCRS
     * @param sisCrs - sisCrs
     * @param iSisCrs - iSisCrs
     * @return - boolean
     */

    public static boolean checkInverseTransformationFromScore(CoordinateReferenceSystem transformSourceCRS,
                                                              CoordinateReferenceSystem transformTargetCRS,
                                                              ISisCrs sisCrs, ISisCrs iSisCrs) {
        Identifier identifierSource = IdentifiedObjects.getIdentifier(transformSourceCRS, Citations.EPSG);
        Identifier identifierTarget = IdentifiedObjects.getIdentifier(transformTargetCRS, Citations.EPSG);
        boolean do_reverse = false;
        // First check if reverse can be determined from codes
        // for example, Source=4267, target=4326, and from=4326, to=32065= NAD27/BLM15, with BaseGeog 4267
        if (identifierSource != null && identifierTarget != null) {
            int sourceCode = Integer.parseInt(identifierSource.getCode());
            int targetCode = Integer.parseInt(identifierTarget.getCode());
            int sisCRSCode = Integer.parseInt(sisCrs.getAuthorityCode().getCode());
            int iSisCRSCode = Integer.parseInt(iSisCrs.getAuthorityCode().getCode());
            if (sourceCode==sisCRSCode && targetCode==iSisCRSCode){
                do_reverse=false;
                return do_reverse;
            }
            if (sourceCode==iSisCRSCode && targetCode==sisCRSCode){
                do_reverse=true;
                return do_reverse;
            }
        }

        // If not by code, attempt string matching of GEOGCS string
        String fromCRS = CrsNameUtils.getCrsNameFromWKT(sisCrs.getWkt());
        String toCRS = CrsNameUtils.getCrsNameFromWKT(iSisCrs.getWkt());

        String sourceCRS = String.valueOf(transformSourceCRS.getName().getCode());
        String targetCRS = String.valueOf(transformTargetCRS.getName().getCode());
        //we are defining a new algorithm if the above conditions is failing. In this method we are comapring the
        // characters and getting score. By using the score we are deciding to reverse or forword the transformation
        //Compare and get the score for fromCRS, sourceCRS
        LOGGER.info("Compare and get the score...");
        int score_CTsource_is_fromCRS = compareCharacters(fromCRS, sourceCRS);
        //Compare and get the score for toCRS, sourceCRS
        int score_CTsource_is_toCRS = compareCharacters(toCRS, sourceCRS);
        //Compare and get the score for fromCRS, targetCRS
        int score_CTtarget_is_fromCRS = compareCharacters(fromCRS, targetCRS);
        //Compare and get the score for toCRS, targetCRS
        int score_CTtarget_is_toCRS = compareCharacters(toCRS, targetCRS);

        //Added all the score values to temp_map to find the reverse or forward direction
        LinkedHashMap<String, Integer> temp_map = new LinkedHashMap<String, Integer>();
        temp_map.put("score_CTsource_is_fromCRS", score_CTsource_is_fromCRS);
        temp_map.put("score_CTsource_is_toCRS", score_CTsource_is_toCRS);
        temp_map.put("score_CTtarget_is_fromCRS", score_CTtarget_is_fromCRS);
        temp_map.put("score_CTtarget_is_toCRS", score_CTtarget_is_toCRS);

        do_reverse = findDirection(temp_map);//This method is used to do_reverse boolean value true or false.

        return do_reverse;

    }

    private static int compareCharacters(String crsname, String othercrsname){
        //Step1- Remove all the spaces and underscores(special characters)(- and /)GCS
        String formattedinput =removingSpacesFromName(crsname);
        String Otherformattedinput =removingSpacesFromName(othercrsname);
        //Step2- score equal the number of characters match from left to right
        int left_right_score= characterMatchLefttoRight(formattedinput,Otherformattedinput);
        //Score + equals the number of characters match from right to left
        int right_left_score= characterMatchRighttoLeft(formattedinput,Otherformattedinput);
        //return the total score
        //sum the charactersmatching from left to right
        int score = left_right_score +right_left_score;
        return score;
    }

    private static boolean findDirection(LinkedHashMap<String, Integer> temp_map) {
        boolean do_reverse_flag = false;
        ArrayList<Integer> tempList = (ArrayList<Integer>) temp_map.values().stream().collect(Collectors.toList());
        int max_score = tempList.stream().mapToInt(Integer::intValue).max().getAsInt();
        //if max_score is < 3 then throw an exception.
        if (max_score < 3) {
            LOGGER.info("max_score is less than 3 and and it will be invalid transformation crs match");
            throw new IllegalArgumentException(Constants.ERROR_MSG_INVALID_TRANSFORM_CRS_MATCH);
        }
        // based on the score it will check the below if-else conditions
        if (temp_map.get("score_CTtarget_is_toCRS") > temp_map.get("score_CTtarget_is_fromCRS")
                && temp_map.get("score_CTsource_is_fromCRS") > temp_map.get("score_CTsource_is_toCRS")) {
            do_reverse_flag = false;// forward direction
        } else if (temp_map.get("score_CTtarget_is_fromCRS") > temp_map.get("score_CTtarget_is_toCRS")
                && temp_map.get("score_CTsource_is_toCRS") > temp_map.get("score_CTsource_is_fromCRS")) {
            do_reverse_flag = true;// reverse direction
        } else {// It will work to throw an exception
            LOGGER.info("Invalid transformation crs match");
            throw new IllegalArgumentException(Constants.ERROR_MSG_INVALID_TRANSFORM_CRS_MATCH);
        }
        return do_reverse_flag;
    }
    private static int characterMatchLefttoRight(String formattedinput, String otherformattedinput) {
        int otherformattedinputLength = otherformattedinput.length();
        int formattedinputLength = formattedinput.length();
        int count = 0;
        // start iterating from the beginning of both
        int i = 0;
        int j = 0;
        while (i < formattedinputLength && j < otherformattedinputLength
                && formattedinput.charAt(i) == otherformattedinput.charAt(j)) {
            count++;
            i++;
            j++;
        }
        return count;
    }
    private static int characterMatchRighttoLeft(String formattedinput, String otherformattedinput) {
        int count = 0;
        // start iterating from the beginning of both
        int i = formattedinput.length() - 1;
        int j = otherformattedinput.length() - 1;
        while (i >= 0 && j >= 0 && formattedinput.charAt(i) == otherformattedinput.charAt(j)) {
            count++;
            i--;
            j--;
        }
        return count;
    }
    private static String removingSpacesFromName(String input) {
        String regex = "GCS_|[-_ /\\\\[\\\\]()]";
        Pattern pattern = Pattern.compile(regex);
        Matcher match = pattern.matcher(input);
        String updatedName = match.replaceAll("");
        updatedName = updatedName.replaceAll("\\s+", "");
        updatedName = remove(updatedName);
        return updatedName.toUpperCase();
    }

    private static String remove(String updatedName) {

        Pattern pattern = Pattern.compile("(?<=\\D|^)(\\d{2})(\\d{2})(?=\\D|$)");
        Matcher matcher = pattern.matcher(updatedName);
        return matcher.replaceAll("$2");
    }


}
