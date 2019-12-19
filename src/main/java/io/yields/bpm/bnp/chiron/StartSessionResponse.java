package io.yields.bpm.bnp.chiron;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Slf4j
public class StartSessionResponse {

    private List<String> ids;
}
