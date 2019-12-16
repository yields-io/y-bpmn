package io.yields.bpm.bnp;

import io.yields.bpm.bnp.chiron.ChironApi;
import io.yields.bpm.bnp.chiron.ModelDTO;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.Variables;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.camunda.bpm.engine.variable.Variables.objectValue;

public class RetrieveAllModelsDelegate implements JavaDelegate {

  private final static Logger LOGGER = Logger.getLogger("RETRIEVE-MODELS");

  public void execute(DelegateExecution execution) throws Exception {
    LOGGER.info("Processing request by '"+execution.getVariable("modelList"));


    Map<String, String> models = ChironApi.getModels().stream()
            .collect(Collectors.toMap(ModelDTO::getId, ModelDTO::getName));

    execution.setVariable("modelList",
            objectValue(models)
                    .serializationDataFormat(Variables.SerializationDataFormats.JSON)
                    .create());

    List<String> localTeams = new ArrayList<String>();
    localTeams.add("France");
    localTeams.add("Belgium");

    execution.setVariable("localTeams", localTeams);
 }

}
