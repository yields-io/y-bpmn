package io.yields.bpm.bnp;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.util.logging.Logger;

public class RetrieveAllModelsDelegate implements JavaDelegate {

  private final static Logger LOGGER = Logger.getLogger("RETRIEVE-MODELS");

  public void execute(DelegateExecution execution) throws Exception {
    LOGGER.info("Processing request by '"+execution.getVariable("modelList"));
  }

}
