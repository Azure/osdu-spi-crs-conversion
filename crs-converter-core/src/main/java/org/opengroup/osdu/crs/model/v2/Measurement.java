package org.opengroup.osdu.crs.model.v2;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotEmpty;


@Data
@EqualsAndHashCode(callSuper = true)
public class Measurement extends PersistableReference {

    @JsonProperty("ancestry")
    @NotEmpty
    private String measurementAncestry;


    @Override
    public String toJsonString() {
        return super.toJsonString();
    }


    public Measurement() {
        super("UM");
        this.measurementAncestry = "";
    }
}
