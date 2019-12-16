package io.yields.bpm.bnp.chiron;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.ToString;
import lombok.Value;


@Value
@JsonDeserialize(builder = ModelDTO.ModelDTOBuilder.class)
@Builder
@ToString
public class ModelDTO {

    private String id;
    private String name;
//    private String status;
//    private String description;
//    private Long instancesCount;
//    private String modelOutputType;
//    private String modelOwnerName;
//    private String modelVersion;

    @JsonPOJOBuilder(withPrefix = "")
    public static class ModelDTOBuilder {

    }
}
