package org.opengroup.osdu.crs.model.v1;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.opengroup.osdu.crs.sis.CrsNameUtils;
import org.opengroup.osdu.crs.sis.ISisCrs;
import org.opengroup.osdu.crs.sis.SisCrsFactory;

@Data
@EqualsAndHashCode(callSuper = true)
public class LateBoundCRS extends CRS {

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private ISisCrs projectedCrs;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private ISisCrs baseGeographicCrs;

    @JsonProperty("WKT")
    protected String wellKnownText;

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

        String crsName = CrsNameUtils.findCrsName(wellKnownText, "");
        SisCrsFactory sisFactory = new SisCrsFactory();
        ISisCrs sisCrs = sisFactory.createSisCrs(wellKnownText, commonAuthorityCode, crsName);
        ISisCrs tempCrs = sisCrs.getGeogCoordSys();
        if (tempCrs != null) {
            this.projectedCrs = sisCrs;
            this.baseGeographicCrs = tempCrs;
        } else {
            this.baseGeographicCrs = sisCrs;
        }
    }

}
