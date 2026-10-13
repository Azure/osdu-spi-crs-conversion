package org.opengroup.osdu.crs.model.Impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.Multimaps;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.sis.io.wkt.WKTFormat;
import org.apache.sis.referencing.CRS;
import org.apache.sis.referencing.crs.AbstractCRS;
import org.apache.sis.referencing.cs.AxesConvention;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.CoordinateOperationAuthorityFactory;
import org.opengis.referencing.operation.SingleOperation;
import org.opengroup.osdu.crs.model.CRSType;
import org.opengroup.osdu.crs.model.ICompoundTrf;
import org.opengroup.osdu.crs.model.ILateBoundCrs;
import org.opengroup.osdu.crs.model.ISingleTrf;
import org.opengroup.osdu.crs.model.ITrf;
import org.opengroup.osdu.crs.sis.AuthorityCodeUtils;
import org.opengroup.osdu.crs.sis.ISisCrs;
import org.opengroup.osdu.crs.sis.transform.ISisMathTransform;
import org.opengroup.osdu.crs.sis.transform.SisMathTransformFromCode;
import org.opengroup.osdu.crs.sis.transform.SisMathTransformFromCrs;
import org.opengroup.osdu.crs.sis.transform.SisMathTransformFromWkt;
import org.opengroup.osdu.crs.sis.wkt.IWktAttribute;
import org.opengroup.osdu.crs.sis.wkt.WktParser;
import org.opengroup.osdu.crs.sis.wkt.WktSection;
import org.opengroup.osdu.crs.sis.wkt.WktStringAttribute;

@Data
public class SingleTrf implements ISingleTrf {

    private static final Logger LOGGER = Logger.getLogger(SingleTrf.class.getName());

    private org.opengroup.osdu.crs.model.v1.SingleTRF implementationV1;
    private org.opengroup.osdu.crs.model.v2.SingleTrf implementationV2;

    private ISisMathTransform transformOperation;
    private String wellKnownText;
    private String engineVersion;
    private CRSType type;
    private String name;
    private AuthorityCode authorityCode;
    private ILateBoundCrs lateBoundCrs;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Boolean valid;

    SingleTrf(org.opengroup.osdu.crs.model.v1.SingleTRF parsedItem) {
        this(parsedItem, null);
    }

    SingleTrf(org.opengroup.osdu.crs.model.v2.SingleTrf parsedItem) {
        this(parsedItem, null);
    }

    SingleTrf(org.opengroup.osdu.crs.model.v1.SingleTRF parsedItem, ILateBoundCrs lateBoundCrs) {
        this.implementationV1 = parsedItem;
        this.implementationV2 = null;
        this.wellKnownText = parsedItem.getWellKnownText();
        this.type = CRSType.TRF;
        this.name = parsedItem.getName();
        if (parsedItem.getAuthorityCode() != null) {
            this.authorityCode = new AuthorityCode(parsedItem.getAuthorityCode().getAuthority(), parsedItem.getAuthorityCode().getCode());
        }
        this.lateBoundCrs = lateBoundCrs;
        this.engineVersion = parsedItem.getEngineVersion();
        loadImplementation();
    }

    private boolean is3dConversionDisable() {
        String env = System.getenv("DISABLE_3D_CONVERSIONS");
        if (env != null && !env.isEmpty()) {
            if (env.equalsIgnoreCase("true")) {
                return true;
            }
        }
        return false;
    }

    SingleTrf(org.opengroup.osdu.crs.model.v2.SingleTrf parsedItem, ILateBoundCrs lateBoundCrs) {
        this.implementationV1 = null;
        this.implementationV2 = parsedItem;
        this.wellKnownText = parsedItem.getWellKnownText();
        this.type = CRSType.TRF;
        this.name = parsedItem.getTrfName();
        if (parsedItem.getAuthorityCode() != null) {
            this.authorityCode = new AuthorityCode(parsedItem.getAuthorityCode().getAuthority(), parsedItem.getAuthorityCode().getCode());
        }
        this.lateBoundCrs = lateBoundCrs;
        this.engineVersion = parsedItem.getVersion();
        loadImplementation();
    }

    private void loadImplementation() {
        try {
            if (lateBoundCrs != null && !lateBoundCrs.isValid()) {
                valid = false;
                return;
            }
            this.transformOperation = createTransformOperation(lateBoundCrs);
            if (this.transformOperation == null) {
                this.valid = false;
                return;
            }
            this.valid = true;
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Can't create transformation", ex);
            this.valid = false;
        }
    }

    private ISisMathTransform createTransformOperation(ILateBoundCrs lateBoundCrs) throws Exception {
        if (!AuthorityCodeUtils.isEpsgCode(authorityCode)) {
            return createTransformFromWKT();
        }
        try {
            CoordinateOperationAuthorityFactory opFactory = (CoordinateOperationAuthorityFactory) CRS.getAuthorityFactory("EPSG");
            CoordinateOperation operation = opFactory.createCoordinateOperation(authorityCode.getCode());
            boolean supports3DPointConversion = supports3DPointConversion(operation);
            return new SisMathTransformFromCode(operation, supports3DPointConversion);
        } catch (NoSuchAuthorityCodeException e) {
            return createTransformFromWKT();
        }
    }

    private ISisMathTransform createTransformFromWKT() throws Exception {
        WKTFormat format = new WKTFormat(null, null);
        WktParser parser = new WktParser();
        String correctedWellKnownText = correctFileParametersIfNeeded();
        CoordinateOperation operation = (CoordinateOperation) format.parseObject(correctedWellKnownText);
        boolean supports3DPointConversion = supports3DPointConversion(operation);
        return new SisMathTransformFromWkt(operation, supports3DPointConversion);
    }

    //correct wkts that have file parameters specified
    private String correctFileParametersIfNeeded() {
        try {
            WktParser parser = new WktParser();
            WktSection rootSection = parser.parseWkt(wellKnownText);
            WktSection methodSection = rootSection.getSubsection("METHOD");
            if (methodSection == null) {
                return wellKnownText;
            }
            List<IWktAttribute> methodAttributes = methodSection.getAttributes();
            if (methodAttributes == null || methodAttributes.size() != 1) {
                return wellKnownText;
            }
            String methodNameFromWKT = (String) methodAttributes.get(0).getValue();
            methodNameFromWKT = methodNameFromWKT.replace("\"", "");
            if (!methodNameFromWKT.equals("NADCON") && !methodNameFromWKT.equals("NTv2")) {
                return wellKnownText;
            }
            List<WktSection> subSections = rootSection.getSubSections();
            for (int i = 0; i < subSections.size(); i++) {
                WktSection currentSubSection = subSections.get(i);
                if (!currentSubSection.getType().equals("PARAMETER")) {
                    continue;
                }
                List<IWktAttribute> existingAttributes = currentSubSection.getAttributes();
                if (existingAttributes.size() != 2) {
                    continue;
                }
                String parameterName = (String) existingAttributes.get(0).getValue();
                parameterName = parameterName.replace("\"", "");
                if (!parameterName.startsWith("Dataset_")) {
                    continue;
                }
                String fileName = getFileNameFromParameter(parameterName);
                rootSection.remove(i);
                if (methodNameFromWKT.equals("NTv2")) {
                    List<IWktAttribute> fileParameterAttributes = new ArrayList<>();
                    fileParameterAttributes.add(new WktStringAttribute("\"Latitude and longitude difference file\""));
                    fileParameterAttributes.add(new WktStringAttribute("\"" + fileName + ".gsb\""));
                    rootSection.add(i, new WktSection("PARAMETER", fileParameterAttributes, new ArrayList<>()));
                } else {
                    List<IWktAttribute> latitudeFileParameterAttributes = new ArrayList<>();
                    latitudeFileParameterAttributes.add(new WktStringAttribute("\"Latitude difference file\""));
                    latitudeFileParameterAttributes.add(new WktStringAttribute("\"" + fileName + ".las\""));
                    rootSection.add(i, new WktSection("PARAMETER", latitudeFileParameterAttributes, new ArrayList<>()));

                    List<IWktAttribute> longitudeFileParameterAttributes = new ArrayList<>();
                    longitudeFileParameterAttributes.add(new WktStringAttribute("\"Longitude difference file\""));
                    longitudeFileParameterAttributes.add(new WktStringAttribute("\"" + fileName + ".los\""));
                    rootSection.add(i, new WktSection("PARAMETER", longitudeFileParameterAttributes, new ArrayList<>()));
                }
                //there should be only one file parameter specified
                return  rootSection.toWktString();
            }
            return wellKnownText;
        } catch(Exception ex) {
            LOGGER.log(Level.INFO, "Unable to correct file parameters", ex);
            return wellKnownText;
        }
    }

    private String getFileNameFromParameter(String parameterName) {
        int indexOfDirDivider = parameterName.lastIndexOf("/");
        if (indexOfDirDivider > -1) {
            return parameterName.substring(indexOfDirDivider + 1);
        }
        int indexOfDivider = parameterName.indexOf("_");
        return parameterName.substring(indexOfDivider + 1);
    }


    private boolean supports3DPointConversion(CoordinateOperation operation) {
        if (is3dConversionDisable()) {
            return false;
        }
        try {
            if (!(operation instanceof SingleOperation)) {
                return false;
            }
            SingleOperation singleOperation = (SingleOperation) operation;
            String methodName = singleOperation.getMethod().getName().getCode();
            if (!methodName.contains("geog2D")) {
                return false;
            }
            if (wellKnownText != null) {
                WktParser parser = new WktParser();
                WktSection section = parser.parseWkt(wellKnownText);
                if (section == null) {
                    return false;
                }
                WktSection methodSection = section.getSubsection("METHOD");
                if (methodSection == null) {
                    return false;
                }
                List<IWktAttribute> methodAttributes = methodSection.getAttributes();
                if (methodAttributes == null || methodAttributes.size() != 1) {
                    return false;
                }
                String methodNameFromWKT = (String) methodAttributes.get(0).getValue();
                methodNameFromWKT = methodNameFromWKT.replace("\"", "").toLowerCase();
                switch (methodNameFromWKT) {
                    case "geocentric_translation":
                    case "position_vector":
                    case "molodensky":
                    case "molodensky_abridged":
                    case "coordinate_frame":
                    case "molodensky_badekas":
                        return true;
                    default:
                        return false;
                }
            }
            String formattedMethodName = methodName.replace("\"", "").replace(" ", "_").toLowerCase();
            if (formattedMethodName.startsWith("geocentric_translation")) {
                return true;
            }
            if (formattedMethodName.startsWith("position_vector")) {
                return true;
            }
            if (formattedMethodName.startsWith("molodensky")) {
                return true;
            }
            if (formattedMethodName.startsWith("coordinate_frame")) {
                return true;
            }

            //parse wkt first
            return false;
        } catch (Exception ex) {
            return false;
        }
    }

    //if authority code is not valid or there is no transformation for the authority code, try and find the best transformation
    //between the lateBoundCrs and WGS 84
    private ISisMathTransform createBestTransformationWithoutCode(ILateBoundCrs lateBoundCrs) throws Exception {
        if (lateBoundCrs == null) {
            return null;
        }
        ISisCrs baseCrs = lateBoundCrs.getBaseGeographicCrs();
        if (baseCrs == null) {
            return null;
        }
        CoordinateReferenceSystem crs = AbstractCRS.castOrCopy(baseCrs.getCoordinateReferenceSystem()).forConvention(AxesConvention.DISPLAY_ORIENTED);
        CoordinateReferenceSystem wgs84 = CRS.forCode("EPSG:4326");
        wgs84 = AbstractCRS.castOrCopy(wgs84).forConvention(AxesConvention.DISPLAY_ORIENTED);
        CoordinateOperation toWGS84Operation = CRS.findOperation(crs, wgs84, null);
        CoordinateOperation fromWGS84Operation = CRS.findOperation(wgs84, crs, null);
        if (toWGS84Operation == null || fromWGS84Operation == null) {
            return null;
        }
        return new SisMathTransformFromCrs(toWGS84Operation, fromWGS84Operation);
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public String createPersistableReference() {
        String pr = "";
        this.loadImplementation();
        if (this.isValid()) {
            this.implementationV2 = createSingleTrfV2(this);
            pr = this.implementationV2.toJsonString();
        }
        return pr;
    }

    static org.opengroup.osdu.crs.model.v2.SingleTrf createSingleTrfV2(ISingleTrf trf) {
        org.opengroup.osdu.crs.model.v2.SingleTrf impl;
        impl = new org.opengroup.osdu.crs.model.v2.SingleTrf();
        impl.setTrfName(trf.getName());
        impl.setVersion(trf.getEngineVersion());
        impl.setWellKnownText(trf.getWellKnownText());
        if (trf.getAuthorityCode() == null || !trf.getAuthorityCode().isDefined()) {
            impl.setAuthorityCode(null);
        } else {
            impl.setAuthorityCode(new org.opengroup.osdu.crs.model.v2.AuthorityCode(
                    trf.getAuthorityCode().getAuthority(),
                    trf.getAuthorityCode().getCode()));
        }
        return impl;
    }

    @Override
    public boolean equalInBehavior(ITrf otherTrf) {
        if (otherTrf instanceof ISingleTrf) {
            ISingleTrf otherSingleTrf = (ISingleTrf) otherTrf;
            //compare the wkt strings
            if (!AuthorityCodeUtils.isEpsgCode(this.getAuthorityCode()) && !AuthorityCodeUtils.isEpsgCode(otherSingleTrf.getAuthorityCode())) {
                if (this.transformOperation != null && otherSingleTrf.getTransformOperation() != null) {
                    return this.transformOperation.isEqual(otherSingleTrf.getTransformOperation());
                }
            }
            //can't create transforms from wtf so need to compare authority codes
            return AuthorityCodeUtils.isEqual(this.getAuthorityCode(), otherSingleTrf.getAuthorityCode());
        } else if (otherTrf instanceof ICompoundTrf) {
            ICompoundTrf otherCompoundTrf = (ICompoundTrf) otherTrf;
            //compare the first transform
            List<ISingleTrf> otherSingleTransforms = otherCompoundTrf.getTransformations();
            if (otherSingleTransforms.isEmpty()) {
                //snould not happen
                return false;
            }
            ISingleTrf otherSingleTrf = otherSingleTransforms.get(0);
            if (!AuthorityCodeUtils.isEpsgCode(this.getAuthorityCode()) && !AuthorityCodeUtils.isEpsgCode(otherSingleTrf.getAuthorityCode())) {
                if (this.transformOperation != null && otherSingleTrf.getTransformOperation() != null) {
                    return this.transformOperation.isEqual(otherSingleTrf.getTransformOperation());
                }
            }
            return AuthorityCodeUtils.isEqual(this.getAuthorityCode(), otherSingleTrf.getAuthorityCode());
        }
        return false;
    }

}