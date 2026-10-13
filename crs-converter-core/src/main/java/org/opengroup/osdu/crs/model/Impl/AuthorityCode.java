package org.opengroup.osdu.crs.model.Impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
public class AuthorityCode {
    public AuthorityCode() {
        authority = "";
        code = "";
    }
    @NonNull
    private String authority;

    @NonNull
    private String code;

    public boolean isDefined() {
        return (!this.getAuthority().isEmpty() && !this.getCode().isEmpty());
    }
}