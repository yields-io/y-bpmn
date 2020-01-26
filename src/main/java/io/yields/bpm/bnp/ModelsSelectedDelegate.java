package io.yields.bpm.bnp;

import com.google.common.collect.Lists;

import io.yields.bpm.bnp.config.FileMapping;
import io.yields.bpm.bnp.config.YieldsProperties;
import io.yields.bpm.bnp.util.Models;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


@Component
@Slf4j
@RequiredArgsConstructor
public class ModelsSelectedDelegate implements JavaDelegate {

    private final YieldsProperties yieldsProperties;

    @Override
    public void execute(DelegateExecution execution) {
        log.info("STARTED ModelsSelectedDelegate");

        String localTeam = "Belgium";

        execution.setVariableLocal("localTeam", localTeam);

        List<FileMapping> fileMappings = yieldsProperties.getMappings().get(localTeam);
        String selectedModelName = Models.getSelectedModels(execution);

        execution.setVariableLocal("fileMappings",
                String.join(
                        ",",
                        fileMappings.stream()
                                .filter(
                                        fileMapping -> StringUtils.isBlank(fileMapping.getIfModel())
                                                || selectedModelName.equals(fileMapping.getIfModel())
                                )
                                .map(FileMapping::getProcessVariable)
                                .collect(Collectors.toList())
                )
        );

    }
}
