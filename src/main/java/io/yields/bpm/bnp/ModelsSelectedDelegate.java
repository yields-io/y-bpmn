package io.yields.bpm.bnp;

import com.google.common.collect.Lists;
import io.yields.bpm.bnp.util.Models;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.List;

import static io.yields.bpm.bnp.util.Models.BEL_MODEL_PREFIX;
import static io.yields.bpm.bnp.util.Models.FRA_MODEL_PREFIX;


@Component
@Slf4j
public class ModelsSelectedDelegate implements JavaDelegate {


    @Override
    public void execute(DelegateExecution execution) {
        log.info("STARTED ModelsSelectedDelegate");

        List<String> selectedModelNames = Models.getSelectedModels(execution);
        List<String> localTeams = Lists.newArrayList();
        if (modelNameStartsWith(selectedModelNames, FRA_MODEL_PREFIX)) {
            localTeams.add("France");
        }
        if (modelNameStartsWith(selectedModelNames, BEL_MODEL_PREFIX)) {
            localTeams.add("Belgium");
        }
        execution.setVariable("localTeams", localTeams);
    }

    private boolean modelNameStartsWith(List<String> modelNames, String prefix) {
        return modelNames.stream()
                .map(modelName -> modelName.toUpperCase())
                .filter(modelName -> modelName.startsWith(prefix))
                .findAny()
                .isPresent();
    }
}
