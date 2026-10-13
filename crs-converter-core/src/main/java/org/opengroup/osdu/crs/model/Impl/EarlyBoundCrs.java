package org.opengroup.osdu.crs.model.Impl;

import org.opengroup.osdu.crs.model.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opengroup.osdu.crs.sis.ISisCrs;

@Data
@NoArgsConstructor
public class EarlyBoundCrs implements IEarlyBoundCrs {

    // V1 constructor
    EarlyBoundCrs(org.opengroup.osdu.crs.model.v1.EarlyBoundCRS parsedItem) {
        this.implementationV1 = parsedItem;
        this.implementationV2 = null;
        this.type = CRSType.EARLY_BOUND;
        this.lateBoundCrs = new LateBoundCrs(parsedItem.getLateBoundCRS());
        if (parsedItem.getTrf().getType() == org.opengroup.osdu.crs.model.v1.CRSType.TRF) {
            this.trf = new SingleTrf((org.opengroup.osdu.crs.model.v1.SingleTRF) parsedItem.getTrf(), lateBoundCrs);
        } else if (parsedItem.getTrf().getType() == org.opengroup.osdu.crs.model.v1.CRSType.COMPOUND_TRF) {
            this.trf = new CompoundTrf((org.opengroup.osdu.crs.model.v1.CompoundTRF) parsedItem.getTrf(), lateBoundCrs);
        }
        this.name = parsedItem.getName();
        if (parsedItem.getAuthorityCode() != null) {
            this.authorityCode = new AuthorityCode(parsedItem.getAuthorityCode().getAuthority(), parsedItem.getAuthorityCode().getCode());
        }
        this.engineVersion = parsedItem.getEngineVersion();
    }

    // V2 constructor
    EarlyBoundCrs(org.opengroup.osdu.crs.model.v2.EarlyBoundCrs parsedItem) {
        this.implementationV2 = parsedItem;
        this.implementationV1 = null;
        this.type = CRSType.EARLY_BOUND;
        this.lateBoundCrs = new LateBoundCrs(parsedItem.getLateBoundCrs());
        if (parsedItem.getSingleTransformation() != null) {
            this.trf = new SingleTrf(parsedItem.getSingleTransformation(), lateBoundCrs);
            ((SingleTrf) trf).setLateBoundCrs(lateBoundCrs);
        } else if (parsedItem.getCompoundTransformation() != null) {
            this.trf = new CompoundTrf(parsedItem.getCompoundTransformation(), lateBoundCrs);
        }
        this.name = parsedItem.getCrsName();
        if (parsedItem.getAuthorityCode() != null) {
            this.authorityCode = new AuthorityCode(parsedItem.getAuthorityCode().getAuthority(), parsedItem.getAuthorityCode().getCode());
        }
        this.engineVersion = parsedItem.getVersion();
    }

    private org.opengroup.osdu.crs.model.v1.EarlyBoundCRS implementationV1;
    private org.opengroup.osdu.crs.model.v2.EarlyBoundCrs implementationV2;

    private String engineVersion;
    private CRSType type;
    private String name;
    private AuthorityCode authorityCode;

    private ITrf trf;
    private ILateBoundCrs lateBoundCrs;

    public boolean isValid() {
        return getLateBoundCrs() != null && getTrf() != null && getLateBoundCrs().isValid() && getTrf().isValid();
    }

    @Override
    public boolean isProjectedCrs() {
        return this.getLateBoundCrs().isProjectedCrs();
    }

    public boolean isVerticalCrs() {
        return this.getLateBoundCrs().isVerticalCrs();
    }

    public boolean isGeographicCrs() {
        return this.getLateBoundCrs().isGeographicCrs();
    }

    @Override
    public ISisCrs getBaseGeographicCrs() {
        return this.getLateBoundCrs().getBaseGeographicCrs();
    }

    @Override
    public ISisCrs getProjectedCrs() {
        return this.getLateBoundCrs().getProjectedCrs();
    }

    @Override
    public String createPersistableReference() {
        String pr = "";
        if (this.isValid()) {
            org.opengroup.osdu.crs.model.v2.EarlyBoundCrs crs = new org.opengroup.osdu.crs.model.v2.EarlyBoundCrs();
            crs.setCrsName(this.getName());
            crs.setVersion(this.getEngineVersion());
            crs.setLateBoundCrs(LateBoundCrs.createLateBoundCrsV2(this.getLateBoundCrs()));
            if (this.getTrf().getType().equals(CRSType.TRF)) {
                crs.setSingleTransformation(SingleTrf.createSingleTrfV2((ISingleTrf) this.getTrf()));
            } else if (this.getTrf().getType().equals(CRSType.COMPOUND_TRF)) {
                crs.setCompoundTransformation(CompoundTrf.createCompoundTrfV2((ICompoundTrf) this.getTrf()));
            }
            if (this.getAuthorityCode() == null || !this.getAuthorityCode().isDefined()) {
                crs.setAuthorityCode(null);
            } else {
                crs.setAuthorityCode(new org.opengroup.osdu.crs.model.v2.AuthorityCode(
                        this.getAuthorityCode().getAuthority(),
                        this.getAuthorityCode().getCode()));
            }
            this.implementationV2 = crs;
            pr = crs.toJsonString();
        }
        return pr;
    }
}
