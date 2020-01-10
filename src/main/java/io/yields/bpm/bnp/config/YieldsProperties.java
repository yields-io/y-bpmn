package io.yields.bpm.bnp.config;

import lombok.Data;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


@ConfigurationProperties(prefix = "yields")
@Component
@Data
public class YieldsProperties {

    private String setupCampaignDataSet;
    private Map<String, List<FileMapping>> mappings;
    private Map<String, CheckProps> dataChecks;
    private Map<String, CheckProps> performanceChecks;


    public List<FileMapping> getLocalTeamMappings(DelegateExecution execution) {
        String localTeam = (String) execution.getVariable("localTeam");
        return getMappings().get(localTeam);
    }
}
