package io.yields.bpm.bnp.chiron;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Slf4j
public class SessionDetailsDTO {

    private String id;
    private String stageId;
    private String status;

}
