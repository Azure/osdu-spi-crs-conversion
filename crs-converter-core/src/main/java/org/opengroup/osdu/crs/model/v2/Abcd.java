package org.opengroup.osdu.crs.model.v2;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;


@Data
@AllArgsConstructor
public class Abcd {
    public Abcd() {
        this.a = Double.NaN;
        this.b = Double.NaN;
        this.c = Double.NaN;
        this.d = Double.NaN;
    }

    @NonNull
    @JsonProperty("a")
    private Double a;

    @NonNull
    @JsonProperty("b")
    private Double b;

    @NonNull
    @JsonProperty("c")
    private Double c;

    @NonNull
    @JsonProperty("d")
    private Double d;
}
