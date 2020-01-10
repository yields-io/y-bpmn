package io.yields.bpm.bnp;

import io.yields.bpm.bnp.chiron.ChironApi;
import io.yields.bpm.bnp.chiron.ModelDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.camunda.bpm.engine.variable.Variables.objectValue;

@Slf4j
@Component
@RequiredArgsConstructor
public class RetrieveAllModelsDelegate implements JavaDelegate {


  public void execute(DelegateExecution execution) throws Exception {
    log.info("STARTING RetrieveAllModels STEP");

    Map<String, String> models = ChironApi.getModels().stream()
            .collect(Collectors.toMap(ModelDTO::getId, ModelDTO::getName));

    execution.setVariable("modelList",
            objectValue(models)
                    .serializationDataFormat(Variables.SerializationDataFormats.JSON)
                    .create()
    );

    List<String> localTeams = new ArrayList<String>();
    localTeams.add("France");
    localTeams.add("Belgium");
    execution.setVariable("localTeams", localTeams);

    log.info("RetrieveAllModels done");
  }

}
