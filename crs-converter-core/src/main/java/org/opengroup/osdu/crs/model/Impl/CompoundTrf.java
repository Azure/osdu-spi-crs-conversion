package org.opengroup.osdu.crs.model.Impl;

import lombok.Data;
import org.opengis.util.FactoryException;
import org.opengroup.osdu.crs.model.CRSType;
import org.opengroup.osdu.crs.model.ICompoundTrf;
import org.opengroup.osdu.crs.model.ISingleTrf;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opengroup.osdu.crs.model.ILateBoundCrs;
import org.opengroup.osdu.crs.model.ITrf;
import org.opengroup.osdu.crs.sis.AuthorityCodeUtils;
import org.opengroup.osdu.crs.sis.transform.ISisMathTransform;
import org.opengroup.osdu.crs.sis.transform.SisMathTransformFromCode;
import org.apache.sis.referencing.CRS;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.CoordinateOperationAuthorityFactory;

@Data
public class CompoundTrf implements ICompoundTrf {

    private static final Logger LOGGER = Logger.getLogger(CompoundTrf.class.getName());

    private org.opengroup.osdu.crs.model.v1.CompoundTRF implementationV1;
    private org.opengroup.osdu.crs.model.v2.CompoundTrf implementationV2;

    private String policy;
    private List<ISingleTrf> transformations;
    private int[] forwardDirection;
    private int[] inverseDirection;

    private String engineVersion;
    private CRSType type;
    private String name;
    private AuthorityCode authorityCode;

    CompoundTrf(org.opengroup.osdu.crs.model.v1.CompoundTRF parsedItem) {
        this(parsedItem, null);
    }
    
    CompoundTrf(org.opengroup.osdu.crs.model.v2.CompoundTrf parsedItem) {
        this(parsedItem, null);
    }
    
    CompoundTrf(org.opengroup.osdu.crs.model.v1.CompoundTRF parsedItem, ILateBoundCrs crs) {
        this.implementationV1 = parsedItem;
        this.implementationV2 = null;

        this.type = CRSType.COMPOUND_TRF;
        this.name = parsedItem.getName();
        if (parsedItem.getAuthorityCode() != null) {
            this.authorityCode = new AuthorityCode(parsedItem.getAuthorityCode().getAuthority(), parsedItem.getAuthorityCode().getCode());
        }
        this.policy = parsedItem.getPolicy();
        this.transformations = new ArrayList<>();
        for (org.opengroup.osdu.crs.model.v1.SingleTRF trf : parsedItem.getTransformations()) {
            this.transformations.add(new SingleTrf(trf, crs));
        }
        this.engineVersion = parsedItem.getEngineVersion();
        if (this.isFallback()) this.validateFallbackTrf();
        if (this.isConcatenated()) this.validateConcatenatedTrf();
    }

    CompoundTrf(org.opengroup.osdu.crs.model.v2.CompoundTrf parsedItem, ILateBoundCrs crs) {
        this.implementationV1 = null;
        this.implementationV2 = parsedItem;

        this.type = CRSType.COMPOUND_TRF;
        this.name = parsedItem.getTrfName();
        if (parsedItem.getAuthorityCode() != null) {
            this.authorityCode = new AuthorityCode(parsedItem.getAuthorityCode().getAuthority(), parsedItem.getAuthorityCode().getCode());
        }
        this.policy = parsedItem.getPolicy();
        this.transformations = new ArrayList<>();
        List<org.opengroup.osdu.crs.model.v2.SingleTrf> trfs = parsedItem.getTransformationList();
        if(trfs != null) {
            for (org.opengroup.osdu.crs.model.v2.SingleTrf trf : trfs) {
                this.transformations.add(new SingleTrf(trf, crs));
            }
        }
        this.engineVersion = parsedItem.getVersion();
        if (this.isFallback()) this.validateFallbackTrf();
        if (this.isConcatenated()) this.validateConcatenatedTrf();
    }


    @Override
    public boolean isFallback() {
        return this.getPolicy().toLowerCase().equals("fallback");
    }

    @Override
    public boolean isConcatenated() {
        return this.getPolicy().toLowerCase().equals("concatenated");
    }

    @Override
    public ISisMathTransform getTransformOperation() {
        if (!AuthorityCodeUtils.isEpsgCode(authorityCode)) {
            return null;
        }
        try {
            CoordinateOperationAuthorityFactory opFactory = (CoordinateOperationAuthorityFactory) CRS.getAuthorityFactory("EPSG");
            CoordinateOperation operation = opFactory.createCoordinateOperation(authorityCode.getCode());
            return new SisMathTransformFromCode(operation, false);
        } catch (FactoryException ex) {
            LOGGER.log(Level.WARNING, "Can't create EPSG transformation with the code " + authorityCode.getCode(), ex);
            return null;
        }
    }

    @Override
    public boolean isValid() {
        boolean valid = this.getPolicy() != null && (this.isFallback() || this.isConcatenated());

        if (valid) { // else give up early
            for (ISingleTrf trf : this.getTransformations()) {
                valid = valid && trf.isValid();
            }
            if (valid) { // else give up early
                // check specific sub-types
                if (this.isFallback()) {
                    valid = validateFallbackTrf(); // the individual transforms are valid,
                    //                                but the combination may be incoherent
                } else {
                    valid = validateConcatenatedTrf();
                }
            }
        }
        return valid;
    }

    /**
     * Validates a concatenated transformation by checking the validity of each individual transformation.
     *
     * This method iterates through all the individual transformations associated with the current object
     * and checks if each one is valid. It returns a boolean value indicating the overall validity.
     *
     * @return {@code true} if all individual transformations are valid, {@code false} otherwise.
     */
    private boolean validateConcatenatedTrf() {
        boolean validated = true;

        if(this.getTransformations() != null) {
            for (ISingleTrf trf : this.getTransformations()) {
                validated &= trf.isValid();
            }
        }
        return validated;
    }

    private boolean validateFallbackTrf() {
        boolean validated = true;
        int nofTrf = this.getTransformations().size();
        this.setForwardDirection(new int[nofTrf]);
        this.setInverseDirection(new int[nofTrf]);
        for (int i = 0; i < nofTrf; i++) {
            ISingleTrf trf = this.getTransformations().get(i);
            //direction shouldn't matter for apache sis transforms
        }
        return validated;
    }

    static org.opengroup.osdu.crs.model.v2.CompoundTrf createCompoundTrfV2(ICompoundTrf trf) {
        org.opengroup.osdu.crs.model.v2.CompoundTrf impl;
        impl = new org.opengroup.osdu.crs.model.v2.CompoundTrf();
        impl.setTrfName(trf.getName());
        impl.setVersion(trf.getEngineVersion());
        impl.setPolicy(trf.getPolicy());
        if (trf.getAuthorityCode() == null || !trf.getAuthorityCode().isDefined()) {
            impl.setAuthorityCode(null);
        } else {
            impl.setAuthorityCode(new org.opengroup.osdu.crs.model.v2.AuthorityCode(
                    trf.getAuthorityCode().getAuthority(),
                    trf.getAuthorityCode().getCode()));
        }
        impl.setTransformationList(new ArrayList<>());
        for (ISingleTrf item : trf.getTransformations()) {
            org.opengroup.osdu.crs.model.v2.SingleTrf t = SingleTrf.createSingleTrfV2(item);
            impl.getTransformationList().add(t);
        }
        return impl;
    }

    @Override
    public String createPersistableReference() {
        String pr = "";
        if (this.isValid()) {
            this.implementationV2 = createCompoundTrfV2(this);
            pr = this.implementationV2.toJsonString();
        }
        return pr;
    }

    @Override
    public boolean equalInBehavior(ITrf otherTrf) {
        if (transformations.isEmpty()) {
            //snould not happen
            return false;
        }
        ISingleTrf firstTransform = transformations.get(0);
        if (otherTrf instanceof ISingleTrf) {
            ISingleTrf otherSingleTrf = (ISingleTrf) otherTrf;
            //can't create transforms from wtf so need to compare authority codes
            if (AuthorityCodeUtils.isEpsgCode(firstTransform.getAuthorityCode()) && !AuthorityCodeUtils.isEpsgCode(otherSingleTrf.getAuthorityCode())) {
                if (firstTransform.getTransformOperation() != null && otherSingleTrf.getTransformOperation() != null) {
                    return firstTransform.getTransformOperation().isEqual(otherSingleTrf.getTransformOperation());
                }
            }
            return otherSingleTrf.getAuthorityCode().equals(firstTransform.getAuthorityCode());
        } else if (otherTrf instanceof ICompoundTrf) {
            ICompoundTrf otherCompoundTrf = (ICompoundTrf) otherTrf;
            //compare the first transform
            List<ISingleTrf> otherSingleTransforms = otherCompoundTrf.getTransformations();
            if (otherSingleTransforms.isEmpty()) {
                //snould not happen
                return false;
            }
            ISingleTrf otherSingleTrf = (ISingleTrf) otherSingleTransforms.get(0);
            if (AuthorityCodeUtils.isEpsgCode(firstTransform.getAuthorityCode()) && !AuthorityCodeUtils.isEpsgCode(otherSingleTrf.getAuthorityCode())) {
                if (firstTransform.getTransformOperation() != null && otherSingleTrf.getTransformOperation() != null) {
                    return firstTransform.getTransformOperation().isEqual(otherSingleTrf.getTransformOperation());
                }
            }
            return otherSingleTrf.getAuthorityCode().equals(firstTransform.getAuthorityCode());
        }
        return false;
    }
    
    
}
