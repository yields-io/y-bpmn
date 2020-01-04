package io.yields.bpm.bnp;

import com.google.common.base.Splitter;
import io.yields.bpm.bnp.config.FileMapping;
import io.yields.bpm.bnp.config.YieldsProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Component
@RequiredArgsConstructor
public class LocalTeamStart implements JavaDelegate {

    private final YieldsProperties yieldsProperties;

    @Override
    public void execute(DelegateExecution execution) {

        String localTeam = (String) execution.getVariable("localTeam");
        String selectedModelIdsStr = StringUtils.substringBetween((String) execution.getVariable("selectedModel"), "[", "]");
        List<String> modelIds = Splitter.on(",").trimResults().splitToList(selectedModelIdsStr);
        Map<String, String> models = (Map<String, String>) execution.getVariable("modelList");
        List<String> selectedModelNames = models.entrySet().stream()
                .filter(entry -> modelIds.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        List<FileMapping> fileMappings = yieldsProperties.getMappings().get(localTeam);
        execution.setVariableLocal("fileMappings",
            String.join(
                    ",",
                    fileMappings.stream()
                        .filter(
                                fileMapping -> selectedModelNames.isEmpty()
                                || StringUtils.isBlank(fileMapping.getIfModel())
                                || selectedModelNames.contains(fileMapping.getIfModel())
                        )
                        .map(FileMapping::getProcessVariable)
                        .collect(Collectors.toList())
            )
        );
    }
}
