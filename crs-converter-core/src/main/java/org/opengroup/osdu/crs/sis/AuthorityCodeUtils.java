package org.opengroup.osdu.crs.sis;

import com.google.common.base.Objects;

public class AuthorityCodeUtils {

    public static boolean isEqual(org.opengroup.osdu.crs.model.Impl.AuthorityCode authorityCode1,
            org.opengroup.osdu.crs.model.Impl.AuthorityCode authorityCode2) {
        if (!isEpsgCode(authorityCode1) && !isEpsgCode(authorityCode2)) {
            return false;
        }
        if (!isEpsgCode(authorityCode1) || !isEpsgCode(authorityCode2)) {
            return false;
        }
        if (!Objects.equal(authorityCode1.getCode(), authorityCode2.getCode())) {
            return false;
        }
        if (!Objects.equal(authorityCode1.getAuthority(), authorityCode2.getAuthority())) {
            return false;
        }
        return true;
    }

    public static boolean isEpsgCode(String authority, String code) {
        if (authority == null) {
            return false;
        }
        return authority.equalsIgnoreCase("EPSG");
    }

    public static boolean isWGS84(org.opengroup.osdu.crs.model.Impl.AuthorityCode authorityCode) {
        if (!isEpsgCode(authorityCode)) {
            return false;
        }
        return authorityCode.getCode().equals("4326");
    }

    public static boolean isEpsgCode(org.opengroup.osdu.crs.model.Impl.AuthorityCode authorityCode) {
        if (authorityCode == null) {
            return false;
        }
        return isEpsgCode(authorityCode.getAuthority(), authorityCode.getCode());
    }

    public static boolean isEpsgCode(org.opengroup.osdu.crs.model.v1.AuthorityCode authorityCode) {
        if (authorityCode == null) {
            return false;
        }
        return isEpsgCode(authorityCode.getAuthority(), authorityCode.getCode());
    }

    public static boolean isEpsgCode(org.opengroup.osdu.crs.model.v2.AuthorityCode authorityCode) {
        if (authorityCode == null) {
            return false;
        }
        return isEpsgCode(authorityCode.getAuthority(), authorityCode.getCode());
    }

}
