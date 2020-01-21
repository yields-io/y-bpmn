package io.yields.bpm.bnp.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;

import java.util.Map;


@UtilityClass
public class Models {


    public static final String FRA_MODEL_PREFIX = "FRA";
    public static final String BEL_MODEL_PREFIX = "BEL";

//    public static final List<String> SUPPORTED_MODEL_PREFIXES = Lists.newArrayList(FRA_MODEL_PREFIX, BEL_MODEL_PREFIX);


    public String getSelectedModels(DelegateExecution execution) {
        String selectedModelIdStr = (String) execution.getVariable("selectedModel");
//        String selectedModelIdStr = StringUtils.substringBetween((String) execution.getVariable("selectedModel"), "[", "]");
        Map<String, String> models = (Map<String, String>) execution.getVariable("modelList");

        return models.entrySet().stream()
                .filter(entry -> selectedModelIdStr.equals(entry.getKey())).findAny()
                .map(Map.Entry::getValue).orElseThrow(() -> new RuntimeException("Select model error"));
    }

}
