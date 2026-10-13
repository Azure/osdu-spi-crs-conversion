package org.opengroup.osdu.crs.sis.operation;

import java.util.ArrayList;
import java.util.List;

import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengroup.osdu.crs.model.CRSType;
import org.opengroup.osdu.crs.model.ICompoundTrf;
import org.opengroup.osdu.crs.model.ICrs;
import org.opengroup.osdu.crs.model.IEarlyBoundCrs;
import org.opengroup.osdu.crs.model.ISingleTrf;
import org.opengroup.osdu.crs.model.ITrf;
import org.opengroup.osdu.crs.model.Impl.AuthorityCode;
import org.opengroup.osdu.crs.sis.AuthorityCodeUtils;
import org.opengroup.osdu.crs.sis.ISisCrs;
import org.opengroup.osdu.crs.sis.SisCrsUtils;
import org.opengroup.osdu.crs.sis.SisTransformations;
import org.opengroup.osdu.crs.sis.transform.ConcatenatedWGS84TransformFromCode;
import org.opengroup.osdu.crs.sis.transform.ISisMathTransform;
import org.opengroup.osdu.crs.sis.transform.IWGS84Transform;
import org.opengroup.osdu.crs.sis.transform.SingleWGS84TransformFromCode;
import org.opengroup.osdu.crs.sis.transform.SingleWGS84TransformFromCrs;
import org.opengroup.osdu.crs.util.Constants;

public class CRSCoordinateOperationFactory {

    public List<ICRSCoordinateOperation> createOperations(ICrs fromCrs, ICrs toCrs) throws IllegalArgumentException {
        try {
            List<ICRSCoordinateOperation> operations = new ArrayList<>();

            //check if transformations is needed
            boolean fromTransformOperationsNeeded = true;
            boolean toTransformOperationsNeeded = true;

            ISisCrs fromBaseCrs = fromCrs.getBaseGeographicCrs();
            ISisCrs toBaseCrs = toCrs.getBaseGeographicCrs();
            if (!needTransform(fromCrs, toCrs)) {
                fromTransformOperationsNeeded = false;
                toTransformOperationsNeeded = false;
            } else {
                AuthorityCode fromAuthorityCode = fromBaseCrs.getAuthorityCode();
                if (fromAuthorityCode != null && AuthorityCodeUtils.isWGS84(fromAuthorityCode)) {
                    fromTransformOperationsNeeded = false;
                }
                AuthorityCode toAuthorityCode = toBaseCrs.getAuthorityCode();
                if (toAuthorityCode != null && AuthorityCodeUtils.isWGS84(toAuthorityCode)) {
                    toTransformOperationsNeeded = false;
                }
            }

            if (needFromProjection(fromCrs, toCrs, fromTransformOperationsNeeded, toTransformOperationsNeeded)) {
                ISisCrs fromProjectedCRS = fromCrs.getProjectedCrs();
                ISisCrs fromBaseCRS = fromCrs.getBaseGeographicCrs();
                operations.add(new CRSProjectionOperation(fromProjectedCRS, fromBaseCRS, null));
            }

            //add transforms to wgs 84
            if (fromTransformOperationsNeeded) {
                ITrf transform = getTransformation(fromCrs);
                if (transform == null) {
                    throw new IllegalArgumentException(Constants.ERROR_MSG_NO_SUITABLE_CONVERSION);
                }
                ISisMathTransform simpleDatumTransform = getSimpleTransformIfNeeded(transform);
                if (simpleDatumTransform != null) {
                    SingleWGS84TransformFromCrs simpleTransform = new SingleWGS84TransformFromCrs(fromBaseCrs, simpleDatumTransform);
                    operations.add(new CRSTransformToWGS84Operation(simpleTransform));
                } else {
                    if (transform.getType() == CRSType.TRF) {
                        IWGS84Transform transformOperation = new SingleWGS84TransformFromCode(fromBaseCrs, toBaseCrs, (ISingleTrf) transform, false);
                        operations.add(new CRSTransformToWGS84Operation(transformOperation));
                    } else if (transform.getType() == CRSType.COMPOUND_TRF) {
                        IWGS84Transform transformOperation = new ConcatenatedWGS84TransformFromCode(fromBaseCrs, (ICompoundTrf) transform);
                        operations.add(new CRSTransformToWGS84Operation(transformOperation));
                    }
                }
            }

            //add transforms from wgs 84
            if (toTransformOperationsNeeded) {
                ITrf transform = getTransformation(toCrs);
                if (transform == null) {
                    throw new IllegalArgumentException(Constants.ERROR_MSG_NO_SUITABLE_CONVERSION);
                }
                ISisMathTransform simpleDatumTransform = getSimpleTransformIfNeeded(transform);
                if (simpleDatumTransform != null) {
                    SingleWGS84TransformFromCrs simpleTransform = new SingleWGS84TransformFromCrs(toBaseCrs, simpleDatumTransform);
                    operations.add(new CRSTransformFromWGS84Operation(simpleTransform));
                } else {
                    if (transform.getType() == CRSType.TRF) {
                        IWGS84Transform transformOperation = new SingleWGS84TransformFromCode(toBaseCrs, fromBaseCrs, (ISingleTrf) transform, false);
                        operations.add(new CRSTransformFromWGS84Operation(transformOperation));
                    } else if (transform.getType() == CRSType.COMPOUND_TRF) {
                        IWGS84Transform transformOperation = new ConcatenatedWGS84TransformFromCode(toBaseCrs, (ICompoundTrf) transform);
                        operations.add(new CRSTransformFromWGS84Operation(transformOperation));
                    }
                }
            }

            if (needToProjection(fromCrs, toCrs, fromTransformOperationsNeeded, toTransformOperationsNeeded)) {
                ISisCrs toProjectedCrs = toCrs.getProjectedCrs();
                operations.add(new CRSProjectionOperation(toBaseCrs, toProjectedCrs, null));
            }
            return operations;
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Can't convert points", ex);
        }

    }
    //added for explict transform

    public List<ICRSCoordinateOperation> createOperationsV4(ICrs fromCrs, ICrs toCrs, ITrf explicitTransform) throws IllegalArgumentException {
        try {
            List<ICRSCoordinateOperation> operations = new ArrayList<>();

            //check if transformations is needed
            boolean fromTransformOperationsNeeded = true;
            boolean toTransformOperationsNeeded = true;

            ISisCrs fromBaseCrs = fromCrs.getBaseGeographicCrs();
            ISisCrs toBaseCrs = toCrs.getBaseGeographicCrs();
            if (!needTransform(fromCrs, toCrs, explicitTransform)) {
                fromTransformOperationsNeeded = false;
                toTransformOperationsNeeded = false;
            } else {
                AuthorityCode fromAuthorityCode = fromBaseCrs.getAuthorityCode();
                if (fromAuthorityCode != null && AuthorityCodeUtils.isWGS84(fromAuthorityCode)) {
                    fromTransformOperationsNeeded = false;
                }
                AuthorityCode toAuthorityCode = toBaseCrs.getAuthorityCode();
                if (toAuthorityCode != null && AuthorityCodeUtils.isWGS84(toAuthorityCode)) {
                    toTransformOperationsNeeded = false;
                }
            }

            if (needFromProjection(fromCrs, toCrs, fromTransformOperationsNeeded, toTransformOperationsNeeded)) {
                ISisCrs fromProjectedCRS = fromCrs.getProjectedCrs();
                ISisCrs fromBaseCRS = fromCrs.getBaseGeographicCrs();
                operations.add(new CRSProjectionOperation(fromProjectedCRS, fromBaseCRS, null));
            }

            //add transformations
            addTransformsIfNeededV4(fromCrs, toCrs, explicitTransform, fromTransformOperationsNeeded,
                    toTransformOperationsNeeded, operations);

            if (needToProjection(fromCrs, toCrs, fromTransformOperationsNeeded, toTransformOperationsNeeded)) {
                ISisCrs toProjectedCrs = toCrs.getProjectedCrs();
                operations.add(new CRSProjectionOperation(toBaseCrs, toProjectedCrs, null));
            }
            return operations;
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Can't convert points", ex);
        }

    }

    //added for explict transform
    private void addTransformsIfNeededV4(ICrs fromCrs, ICrs toCrs, ITrf explicitTransform,
                                         boolean fromTransformOperationsNeeded, boolean toTransformOperationsNeeded,
                                         List<ICRSCoordinateOperation> operations) throws Exception {
        if (!fromTransformOperationsNeeded && !toTransformOperationsNeeded) {
            return;
        }
        if (explicitTransform != null && explicitTransform.getType() != CRSType.COMPOUND_TRF) {
            addExplicitTransformV4(fromCrs, toCrs, explicitTransform, operations);
            return;
        }
        ISisCrs fromBaseCrs = fromCrs.getBaseGeographicCrs();
        ISisCrs toBaseCrs = toCrs.getBaseGeographicCrs();

        //add transforms to wgs 84
        if (fromTransformOperationsNeeded) {
            ITrf transform;
            if(explicitTransform != null)
                transform = explicitTransform;
            else
                transform = getTransformation(fromCrs);
            if (transform == null) {
                throw new IllegalArgumentException(Constants.ERROR_MSG_NO_SUITABLE_CONVERSION);
            }
            ISisMathTransform simpleDatumTransform = getSimpleTransformIfNeeded(transform);
            if (simpleDatumTransform != null) {
                SingleWGS84TransformFromCrs simpleTransform = new SingleWGS84TransformFromCrs(fromBaseCrs, simpleDatumTransform);
                operations.add(new CRSTransformToWGS84Operation(simpleTransform));
            } else {
                if (transform.getType() == CRSType.TRF) {
                    IWGS84Transform transformOperation = new SingleWGS84TransformFromCode(fromBaseCrs, toBaseCrs, (ISingleTrf) transform, false);
                    operations.add(new CRSTransformToWGS84Operation(transformOperation));
                } else if (transform.getType() == CRSType.COMPOUND_TRF) {
                    IWGS84Transform transformOperation = new ConcatenatedWGS84TransformFromCode(fromBaseCrs, (ICompoundTrf) transform);
                    operations.add(new CRSTransformToWGS84Operation(transformOperation));
                }
            }
        }

        //add transforms from wgs 84
        if (toTransformOperationsNeeded) {
            ITrf transform;
            if(explicitTransform != null)
                transform = explicitTransform;
            else
                transform = getTransformation(toCrs);
            if (transform == null) {
                throw new IllegalArgumentException(Constants.ERROR_MSG_NO_SUITABLE_CONVERSION);
            }
            ISisMathTransform simpleDatumTransform = getSimpleTransformIfNeeded(transform);
            if (simpleDatumTransform != null) {
                SingleWGS84TransformFromCrs simpleTransform = new SingleWGS84TransformFromCrs(toBaseCrs, simpleDatumTransform);
                operations.add(new CRSTransformFromWGS84Operation(simpleTransform));
            } else {
                if (transform.getType() == CRSType.TRF) {
                    IWGS84Transform transformOperation = new SingleWGS84TransformFromCode(toBaseCrs, fromBaseCrs, (ISingleTrf) transform, false);
                    operations.add(new CRSTransformFromWGS84Operation(transformOperation));
                } else if (transform.getType() == CRSType.COMPOUND_TRF) {
                    IWGS84Transform transformOperation = new ConcatenatedWGS84TransformFromCode(toBaseCrs, (ICompoundTrf) transform);
                    operations.add(new CRSTransformFromWGS84Operation(transformOperation));
                }
            }
        }
    }


    //added for explict transform
    private void addExplicitTransformV4(ICrs fromCrs, ICrs toCrs, ITrf explicitTransform, List<ICRSCoordinateOperation> operations) throws Exception {
        if (explicitTransform.getType() != CRSType.TRF) {
            throw new IllegalArgumentException(Constants.ERROR_MSG_INVALID_INPUT_TRANSFORM_SPECIFICATION);
        }
        ISingleTrf singleExplicitTransform = (ISingleTrf) explicitTransform;
        ISisMathTransform sisTransform = singleExplicitTransform.getTransformOperation();
        CoordinateOperation transformCoordinateOperation = sisTransform.getFromWGS84Operation();
        ISisCrs fromBaseCrs = fromCrs.getBaseGeographicCrs();
        ISisCrs toBaseCrs = toCrs.getBaseGeographicCrs();

        CoordinateReferenceSystem transformSourceCRS = transformCoordinateOperation.getSourceCRS();
        CoordinateReferenceSystem transformTargetCRS = transformCoordinateOperation.getTargetCRS();

        if (SisTransformations.checkInverseTransformationFromScore(transformSourceCRS, transformTargetCRS, fromBaseCrs, toBaseCrs)) {
            operations.add(new ExplicitInverseTransformFromCode(fromBaseCrs, toBaseCrs, singleExplicitTransform));
        }else{
            operations.add(new ExplicitTransformFromCode(fromBaseCrs, toBaseCrs, singleExplicitTransform));
        }
    }

    private boolean needFromProjection(ICrs fromCrs, ICrs toCrs, boolean fromTransformNeeded, boolean toTransformNeeded) {
        ISisCrs fromProjectionCrs = fromCrs.getProjectedCrs();
        if (fromProjectionCrs == null) {
            return false;
        }
        if (fromTransformNeeded || toTransformNeeded) {
            return true;
        }
        ISisCrs toProjectionCrs = toCrs.getProjectedCrs();
        if (toProjectionCrs != null && fromProjectionCrs.isEqual(toProjectionCrs)) {
            return false;
        }
        return true;
    }

    private boolean needToProjection(ICrs fromCrs, ICrs toCrs, boolean fromTransformNeeded, boolean toTransformNeeded) {
        ISisCrs toProjectionCrs = toCrs.getProjectedCrs();
        if (toProjectionCrs == null) {
            return false;
        }
        if (fromTransformNeeded || toTransformNeeded) {
            return true;
        }
        ISisCrs fromProjectionCrs = fromCrs.getProjectedCrs();
        if (fromProjectionCrs != null && fromProjectionCrs.isEqual(toProjectionCrs)) {
            return false;
        }
        return true;
    }

    private boolean needTransform(ICrs fromCRS, ICrs toCRS) {
        //isValid has already been called and both fromCRS and toCRS must be valid
        ISisCrs fromBaseSISCrs = fromCRS.getBaseGeographicCrs();
        ISisCrs toBaseSISCrs = toCRS.getBaseGeographicCrs();

        validateRoute(fromCRS, toCRS);
        if (fromCRS.getType() == CRSType.LATE_BOUND && toCRS.getType() == CRSType.LATE_BOUND) {
            return false;
        }

        boolean skip = false;
        ITrf trf1 = getTransformation(fromCRS);
        ITrf trf2 = getTransformation(toCRS);
        if (trf1 != null && trf2 != null) {
            skip = fromBaseSISCrs.isEqual(toBaseSISCrs) && trf1.equalInBehavior(trf2);
        } else if (trf1 != null || trf2 != null) {
            skip = fromBaseSISCrs.isEqual(toBaseSISCrs);
        }
        return !skip;
    }
    
    private ITrf getTransformation(ICrs crs) {
        if (!(crs instanceof IEarlyBoundCrs)) {
            return null;
        }
        return ((IEarlyBoundCrs) crs).getTrf();
    }
    //added for explict transform

    private boolean needTransform(ICrs fromCRS, ICrs toCRS, ITrf explicitTransform) {
        if (explicitTransform != null) {
            if (!needExplicitTransform(fromCRS, toCRS, explicitTransform)) {
                return false;
            }
            if(!(explicitTransform instanceof ICompoundTrf))
                validateExplicitTransform(fromCRS, toCRS, explicitTransform);
            return true;
        }

        //isValid has already been called and both fromCRS and toCRS must be valid
        ISisCrs fromBaseSISCrs = fromCRS.getBaseGeographicCrs();
        ISisCrs toBaseSISCrs = toCRS.getBaseGeographicCrs();

        validateBoundCRSRoute(fromCRS, toCRS);
        if (fromCRS.getType() == CRSType.LATE_BOUND && toCRS.getType() == CRSType.LATE_BOUND) {
            return false;
        }

        boolean skip = false;
        ITrf trf1 = getTransformation(fromCRS);
        ITrf trf2 = getTransformation(toCRS);
        if (trf1 != null && trf2 != null) {
            skip = fromBaseSISCrs.isEqual(toBaseSISCrs) && trf1.equalInBehavior(trf2);
        } else if (trf1 != null || trf2 != null) {
            skip = fromBaseSISCrs.isEqual(toBaseSISCrs);
        }
        return !skip;
    }

    //added for explict transform
    private void validateBoundCRSRoute(ICrs fromCRS, ICrs toCRS) {
        if (fromCRS.getType() == CRSType.LATE_BOUND && toCRS.getType() == CRSType.LATE_BOUND) {
            if (!SisCrsUtils.isValidConversionRoute(fromCRS, toCRS)) {
                throw new IllegalArgumentException(Constants.ERROR_MSG_NO_SUITABLE_CONVERSION + generateFromToMessage(fromCRS, toCRS));
            }
        }
        //source crs for both transform should be WGS84, no need to check transforms
    }

    private boolean needExplicitTransform(ICrs fromCRS, ICrs toCRS, ITrf explicitTransform) {
        if (SisCrsUtils.isValidConversionRoute(fromCRS, toCRS)) {
            return false;
        }
        ITrf trf1 = getTransformation(fromCRS);
        ITrf trf2 = getTransformation(toCRS);

        if (trf1 != null && trf2 != null && trf1.equalInBehavior(explicitTransform) && trf2.equalInBehavior(explicitTransform)) {
            return false;
        }
        return true;
    }

    private boolean validateExplicitTransform(ICrs fromCRS, ICrs toCRS, ITrf explicitTransform) {
        if (!(explicitTransform instanceof ISingleTrf)) {
            throw new IllegalArgumentException(Constants.ERROR_MSG_INVALID_INPUT_TRANSFORM_SPECIFICATION);
        }
        ISingleTrf singleTrf = (ISingleTrf) explicitTransform;
        ISisMathTransform transformOperation = singleTrf.getTransformOperation();
        if (transformOperation.getDerivedType() == ISisMathTransform.TransformDerivedType.DERIVED_FROM_CRS) {
            throw new IllegalArgumentException(Constants.ERROR_MSG_INVALID_INPUT_TRANSFORM_SPECIFICATION);
        }
        CoordinateOperation transformCoordinateOperation = transformOperation.getFromWGS84Operation();
        CoordinateReferenceSystem transformSourceCRS = transformCoordinateOperation.getSourceCRS();
        CoordinateReferenceSystem transformTargetCRS = transformCoordinateOperation.getTargetCRS();

        ISisCrs iSisCrs = fromCRS.getBaseGeographicCrs();
        ISisCrs fromBaseCrs = fromCRS.getBaseGeographicCrs();
        ISisCrs toBaseCrs = toCRS.getBaseGeographicCrs();

        return SisTransformations.checkInverseTransformationFromScore(transformSourceCRS, transformTargetCRS, fromBaseCrs, toBaseCrs);
    }

    private void validateRoute(ICrs fromCRS, ICrs toCRS) {
        if (fromCRS.getType() == CRSType.LATE_BOUND && toCRS.getType() == CRSType.LATE_BOUND) {
            if (!SisCrsUtils.isValidConversionRoute(fromCRS, toCRS)) {
                throw new IllegalArgumentException(Constants.ERROR_MSG_NO_SUITABLE_CONVERSION + generateFromToMessage(fromCRS, toCRS));
            }
        }
        //source crs for both transform should be WGS84, no need to check transforms
    }

    private ISisMathTransform getSimpleTransformIfNeeded(ITrf transform) {
        if (transform.getType() == CRSType.TRF) {
            ISisMathTransform datumOperation = ((ISingleTrf) transform).getTransformOperation();
            if (datumOperation.getDerivedType() == ISisMathTransform.TransformDerivedType.DERIVED_FROM_CRS) {
                return datumOperation;
            }
        } else if (transform.getType() == CRSType.COMPOUND_TRF) {
            List<ISingleTrf> singleTransforms = ((ICompoundTrf) transform).getTransformations();
            ISisMathTransform firstSimpleTransformFound = null;
            for (ISingleTrf currentSingleTransform : singleTransforms) {
                ISisMathTransform datumOperation = currentSingleTransform.getTransformOperation();
                if (datumOperation.getDerivedType() == ISisMathTransform.TransformDerivedType.DERIVED_FROM_CRS) {
                    firstSimpleTransformFound = datumOperation;
                } else {
                    return null;
                }
            }
            return firstSimpleTransformFound;
        }
        return null;

    }

    private String generateFromToMessage(ICrs from, ICrs to) {
        String fromCRS = "";
        String toCRS = "";
        if (from != null) {
            fromCRS = from.getName();
        }
        if (to != null) {
            toCRS = to.getName();
        }
        return " no transformation '" + fromCRS + "' -> '" + toCRS + "'";
    }

}
