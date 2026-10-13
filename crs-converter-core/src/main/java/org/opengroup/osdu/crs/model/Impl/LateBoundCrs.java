package org.opengroup.osdu.crs.model.Impl;

import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.opengroup.osdu.crs.model.CRSType;
import org.opengroup.osdu.crs.model.ILateBoundCrs;
import org.opengroup.osdu.crs.sis.ISisCrs;

@Data
@NoArgsConstructor
public class LateBoundCrs implements ILateBoundCrs {

    private static final Logger LOGGER = Logger.getLogger(LateBoundCrs.class.getName());
    
    // V1 constructor
    LateBoundCrs(org.opengroup.osdu.crs.model.v1.LateBoundCRS parsedItem) {
        this.implementationV1 = parsedItem;
        this.implementationV2 = null;
        this.wellKnownText = parsedItem.getWellKnownText();
        this.type = CRSType.LATE_BOUND;
        this.name = parsedItem.getName();
        if (parsedItem.getAuthorityCode() != null) {
            this.authorityCode = new AuthorityCode(parsedItem.getAuthorityCode().getAuthority(), parsedItem.getAuthorityCode().getCode());
        }
        this.engineVersion = parsedItem.getEngineVersion();
        this.valid = null;
    }

    // V2 constructor
    LateBoundCrs(org.opengroup.osdu.crs.model.v2.LateBoundCrs parsedItem) {
        this.implementationV2 = parsedItem;
        this.implementationV1 = null;
        this.wellKnownText = parsedItem.getLateBoundCrsWkt();
        this.type = CRSType.LATE_BOUND;
        this.name = parsedItem.getCrsName();
        if (parsedItem.getAuthorityCode() != null) {
            this.authorityCode = new AuthorityCode(parsedItem.getAuthorityCode().getAuthority(), parsedItem.getAuthorityCode().getCode());
        }
        this.engineVersion = parsedItem.getVersion();
        this.valid = null;
    }

    private org.opengroup.osdu.crs.model.v1.LateBoundCRS implementationV1;
    private org.opengroup.osdu.crs.model.v2.LateBoundCrs implementationV2;

    private String wellKnownText;
    private String engineVersion;
    private CRSType type;
    private String name;
    private AuthorityCode authorityCode;
    
    private ISisCrs baseGeographicCrs;
    private ISisCrs projectedCrs;
    
    @Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE) private Boolean valid;

    static org.opengroup.osdu.crs.model.v2.LateBoundCrs createLateBoundCrsV2(ILateBoundCrs crs) {
        org.opengroup.osdu.crs.model.v2.LateBoundCrs lb = new org.opengroup.osdu.crs.model.v2.LateBoundCrs();
        lb.setCrsName(crs.getName());
        lb.setLateBoundCrsWkt(crs.getWellKnownText());
        lb.setVersion(crs.getEngineVersion());
        lb.setAuthorityCode(null);
        if (crs.getAuthorityCode() == null || !crs.getAuthorityCode().isDefined()) {
            lb.setAuthorityCode(null);
        } else {
            lb.setAuthorityCode(new org.opengroup.osdu.crs.model.v2.AuthorityCode(
                    crs.getAuthorityCode().getAuthority(),
                    crs.getAuthorityCode().getCode()));
        }
        return lb;
    }

    @Override
    public boolean isProjectedCrs() {
        if (isValid()) {
            return projectedCrs != null;
        }
        return false;
    }

    @Override
    public boolean isGeographicCrs() {
        if (isValid()) {
            return projectedCrs == null && baseGeographicCrs != null;
        }
        return false;
    }

    @Override
    public boolean isVerticalCrs() {
        return false;
    }

    @Override
    public boolean isValid() {
        if (valid != null) {
            return valid;
        }
        try {
            if (implementationV1 != null) {
                projectedCrs = implementationV1.createProjectedCrsIfNeeded();
                baseGeographicCrs = implementationV1.createBaseGeographicCrsIfNeeded();
            } else if (implementationV2 != null) {
                projectedCrs = implementationV2.createProjectedCrsIfNeeded();
                baseGeographicCrs = implementationV2.createBaseGeographicCrsIfNeeded();
            }
            
            if (projectedCrs == null && baseGeographicCrs == null) {
                valid = false;
            } else {
                valid = true;
            }
            return valid;
            
        } catch(Exception ex) {
            LOGGER.log(Level.WARNING, "Could not valid crs", ex);
            valid = false;
        }
        return valid;
    }
    
    

    @Override
    public String createPersistableReference() {
        String pr = "";
        if (this.isValid()) {
            org.opengroup.osdu.crs.model.v2.LateBoundCrs crs = new org.opengroup.osdu.crs.model.v2.LateBoundCrs();
            crs.setLateBoundCrsWkt(this.getWellKnownText());
            crs.setCrsName(this.getName());
            crs.setVersion(this.getEngineVersion());
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
