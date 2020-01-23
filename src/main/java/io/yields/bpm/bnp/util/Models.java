package io.yields.bpm.bnp.util;

import lombok.experimental.UtilityClass;
import org.camunda.bpm.engine.delegate.DelegateExecution;

import java.util.Map;


@UtilityClass
public class Models {

    public String getSelectedModels(DelegateExecution execution) {
        String selectedModelIdStr = (String) execution.getVariable("selectedModel");
        Map<String, String> models = (Map<String, String>) execution.getVariable("modelList");

        return models.entrySet().stream()
                .filter(entry -> selectedModelIdStr.equals(entry.getKey())).findAny()
                .map(Map.Entry::getValue).orElseThrow(() -> new RuntimeException("Select model error"));
    }

}
