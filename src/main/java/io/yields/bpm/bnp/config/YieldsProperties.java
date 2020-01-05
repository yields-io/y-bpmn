package io.yields.bpm.bnp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


@ConfigurationProperties(prefix = "yields")
@Component
@Data
public class YieldsProperties {

    private Map<String, List<FileMapping>> mappings;
    private Map<String, CheckProps> dataChecks;
    private Map<String, CheckProps> performanceChecks;
    private String dataCheckReportUrlTemplate;
    private String performanceCheckReportUrlTemplate;
}
