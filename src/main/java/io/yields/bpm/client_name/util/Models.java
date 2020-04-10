package io.yields.bpm.client_name.util;

import com.google.common.base.Splitter;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@UtilityClass
public class Models {


    public static final String FRA_MODEL_PREFIX = "FRA";
    public static final String BEL_MODEL_PREFIX = "BEL";

//    public static final List<String> SUPPORTED_MODEL_PREFIXES = Lists.newArrayList(FRA_MODEL_PREFIX, BEL_MODEL_PREFIX);


    public List<String> getSelectedModels(DelegateExecution execution) {
        String selectedModelIdsStr = StringUtils.substringBetween((String) execution.getVariable("selectedModel"), "[", "]");
        List<String> modelIds = Splitter.on(",").trimResults().splitToList(selectedModelIdsStr);
        Map<String, String> models = (Map<String, String>) execution.getVariable("modelList");
        return models.entrySet().stream()
                .filter(entry -> modelIds.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

}
