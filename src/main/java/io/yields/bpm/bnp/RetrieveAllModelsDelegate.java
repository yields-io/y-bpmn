package io.yields.bpm.bnp;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.Variables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.camunda.bpm.engine.variable.Variables.objectValue;

public class RetrieveAllModelsDelegate implements JavaDelegate {

  private final static Logger LOGGER = Logger.getLogger("RETRIEVE-MODELS");

  public void execute(DelegateExecution execution) throws Exception {
    LOGGER.info("Processing request by '"+execution.getVariable("modelList"));


    // TODO read from Chiron API
    Map<String, String> models = new HashMap<String, String>();
    models.put("001", "France");
    models.put("002", "Belgium");
    models.put("003", "Other");

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
