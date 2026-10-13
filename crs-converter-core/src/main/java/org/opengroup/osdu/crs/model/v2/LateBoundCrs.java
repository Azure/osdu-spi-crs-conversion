package org.opengroup.osdu.crs.model.v2;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.opengroup.osdu.crs.sis.CrsNameUtils;
import org.opengroup.osdu.crs.sis.ISisCrs;
import org.opengroup.osdu.crs.sis.SisCrsFactory;

@Data
@EqualsAndHashCode(callSuper = true)
public class LateBoundCrs extends PersistableReference {

    @JsonProperty("authCode")
    private AuthorityCode authorityCode;

    @JsonProperty("name")
    @NotEmpty
    private String crsName;

    @JsonProperty("ver")
    private String version;

    @JsonProperty("wkt")
    @NotEmpty
    private String lateBoundCrsWkt;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private ISisCrs projectedCrs;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private ISisCrs baseGeographicCrs;

    @Override
    public String toJsonString() {
        return super.toJsonString();
    }

    public LateBoundCrs() {
        super("LBC");
    }
    
    public ISisCrs createProjectedCrsIfNeeded() throws Exception {
        if (baseGeographicCrs == null) {
            createCrss();
        }
        return projectedCrs;
    }

    public ISisCrs createBaseGeographicCrsIfNeeded() throws Exception {
        if (baseGeographicCrs == null) {
            createCrss();
        }
        return baseGeographicCrs;
    }

    private void createCrss() throws Exception {
        org.opengroup.osdu.crs.model.Impl.AuthorityCode commonAuthorityCode = null;
        if (authorityCode != null) {
            commonAuthorityCode = new org.opengroup.osdu.crs.model.Impl.AuthorityCode(authorityCode.getAuthority(), authorityCode.getCode());
        }
        if (this.crsName == null) {
            this.crsName = CrsNameUtils.findCrsName(lateBoundCrsWkt, "");
        }
        SisCrsFactory sisFactory = new SisCrsFactory();
        ISisCrs sisCrs = sisFactory.createSisCrs(lateBoundCrsWkt, commonAuthorityCode, crsName);
        ISisCrs tempCrs = sisCrs.getGeogCoordSys();
        if (tempCrs != null) {
            this.projectedCrs = sisCrs;
            this.baseGeographicCrs = tempCrs;
        } else {
            this.baseGeographicCrs = sisCrs;
        }
    }
}
