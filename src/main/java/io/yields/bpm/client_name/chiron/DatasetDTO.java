package io.yields.bpm.client_name.chiron;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DatasetDTO {

    private String id;
    private String name;
    private String status;
}
