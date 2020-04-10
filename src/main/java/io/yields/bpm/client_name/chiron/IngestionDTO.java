package io.yields.bpm.client_name.chiron;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Slf4j
@ToString
public class IngestionDTO {

    private String fileName;
    private String status;
}
