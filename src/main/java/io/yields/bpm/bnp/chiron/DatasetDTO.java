package io.yields.bpm.bnp.chiron;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DatasetDTO {

    private String id;
    private String name;
    private String status;
}
