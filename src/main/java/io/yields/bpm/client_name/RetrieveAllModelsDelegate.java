package io.yields.bpm.client_name;

import io.yields.bpm.client_name.chiron.ChironApi;
import io.yields.bpm.client_name.chiron.ModelDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

import static io.yields.bpm.client_name.util.Models.BEL_MODEL_PREFIX;
import static io.yields.bpm.client_name.util.Models.FRA_MODEL_PREFIX;
import static org.camunda.bpm.engine.variable.Variables.objectValue;

@Slf4j
@Component
@RequiredArgsConstructor
public class RetrieveAllModelsDelegate implements JavaDelegate {


  public void execute(DelegateExecution execution) throws Exception {
    log.info("STARTING RetrieveAllModels STEP");

    Map<String, String> models = ChironApi.getModels().stream()
            .filter(model ->
                    model.getName().toUpperCase().startsWith(FRA_MODEL_PREFIX)
                    || model.getName().toUpperCase().startsWith(BEL_MODEL_PREFIX)
            )
            .collect(Collectors.toMap(ModelDTO::getId, ModelDTO::getName));

    execution.setVariable("modelList",
            objectValue(models)
                    .serializationDataFormat(Variables.SerializationDataFormats.JSON)
                    .create()
    );

    log.info("RetrieveAllModels done");
  }

}
