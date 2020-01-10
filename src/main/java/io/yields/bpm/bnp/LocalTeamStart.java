package io.yields.bpm.bnp;

import io.yields.bpm.bnp.config.FileMapping;
import io.yields.bpm.bnp.config.YieldsProperties;
import io.yields.bpm.bnp.util.Models;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Component
@RequiredArgsConstructor
public class LocalTeamStart implements JavaDelegate {

    private final YieldsProperties yieldsProperties;

    @Override
    public void execute(DelegateExecution execution) {

        String localTeam = (String) execution.getVariable("localTeam");
        List<FileMapping> fileMappings = yieldsProperties.getMappings().get(localTeam);
        List<String> selectedModelNames = Models.getSelectedModels(execution);

        execution.setVariableLocal("fileMappings",
            String.join(
                    ",",
                    fileMappings.stream()
                        .filter(
                                fileMapping -> StringUtils.isBlank(fileMapping.getIfModel())
                                || selectedModelNames.contains(fileMapping.getIfModel())
                        )
                        .map(FileMapping::getProcessVariable)
                        .collect(Collectors.toList())
            )
        );
    }
}
