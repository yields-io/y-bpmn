package io.yields.bpm.bnp;

import io.yields.bpm.bnp.chiron.ChironApi;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.util.HashMap;
import java.util.Map;


@Slf4j
public class IngestRequiredDataDelegate implements JavaDelegate {


  public void execute(DelegateExecution execution) throws Exception {
      Map<String, String> ingestIds = (Map<String, String>) execution.getVariable(ProcessVariables.INGEST_IDS);
      boolean success = false;

      try {
          for (Map.Entry<String, String> ingestFilenameAndId : ingestIds.entrySet()) {
              ChironApi.ingest(ingestFilenameAndId);
          }

          Map<String, String> ingestStatuses = new HashMap<>();
          do {
              for (String ingestId : ingestIds.values()) {
                  ingestStatuses.put(ingestId, ChironApi.getDatasetStatus(ingestId));
              }
          } while (notAllIngested(ingestStatuses));

      } catch (Exception e) {
          log.error("IngestRequiredData error", e);
          success = false;
      }

      execution.setVariable("ingestSuccessful", success);
  }

    private boolean notAllIngested(Map<String, String> ingestStatuses) {
        return ingestStatuses.values().stream()
                .filter(status -> !status.equals("Ingested"))
                .findAny()
                .isPresent();
    }

}
