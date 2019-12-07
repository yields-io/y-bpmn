package io.yields.bpm.bnp;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.util.logging.Logger;

public class IngestRequiredDataDelegate implements JavaDelegate {

  private final static Logger LOGGER = Logger.getLogger("RETRIEVE-MODELS");

  public void execute(DelegateExecution execution) throws Exception {

      execution.setVariable("ingestSuccessful", true);
  }

}
