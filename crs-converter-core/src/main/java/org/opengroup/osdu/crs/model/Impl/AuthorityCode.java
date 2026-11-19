package org.opengroup.osdu.crs.model.Impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
public class AuthorityCode {
    // Jackson 3 would otherwise bind via the all-args constructor and skip these defaults
    @JsonCreator
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