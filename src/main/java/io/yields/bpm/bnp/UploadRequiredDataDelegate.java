package io.yields.bpm.bnp;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.impl.value.FileValueImpl;

import java.io.ByteArrayInputStream;
import java.util.logging.Logger;


public class UploadRequiredDataDelegate implements JavaDelegate {

  private final static Logger LOGGER = Logger.getLogger(UploadRequiredDataDelegate.class.getName());

  public void execute(DelegateExecution execution) throws Exception {
    Object tScoreBel = execution.getVariable("T_SCORE_BEL");
    if (tScoreBel != null && tScoreBel instanceof ByteArrayInputStream) {
      ByteArrayInputStream scoreBelFile = (ByteArrayInputStream) tScoreBel;
      LOGGER.info("T_SCORE_BEL has some bytes? " + scoreBelFile.available());

    }
    LOGGER.info(execution.getVariables().toString());

    execution.setVariable("uploadSuccess", true);
  }

}
