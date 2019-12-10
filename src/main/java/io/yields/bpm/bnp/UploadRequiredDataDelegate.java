package io.yields.bpm.bnp;

import io.yields.bpm.bnp.chiron.ChironApi;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


public class UploadRequiredDataDelegate implements JavaDelegate {

  private final static Logger LOGGER = Logger.getLogger(UploadRequiredDataDelegate.class.getName());

  public void execute(DelegateExecution execution) throws Exception {
    Map<String, String > ingestIds = new HashMap<>();
    boolean success;

    try {
      Object tScoreBel = execution.getVariable(ProcessVariables.T_SCORE_BEL);

      if (tScoreBel != null && tScoreBel instanceof ByteArrayInputStream) {
        ByteArrayInputStream scoreBelData = (ByteArrayInputStream) tScoreBel;
        String ingestId = ChironApi.uploadRequiredData(scoreBelData, "T_SCORE_123_BEL.csv");
        ingestIds.put(ProcessVariables.T_SCORE_BEL, ingestId);
      }

      // success = ingestIds.size() == 4;
      execution.setVariable(ProcessVariables.INGEST_IDS, ingestIds);
      success = true;
    } catch (Exception e) {
      success = false;
      LOGGER.warning(e.getMessage());
    }

    execution.setVariable("uploadSuccess", success);
  }

}
