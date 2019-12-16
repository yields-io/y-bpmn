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
      Map<String, String> datasetIds = (Map<String, String>) execution.getVariable(ProcessVariables.DATASET_IDS);
      boolean success;

      try {
          for (Map.Entry<String, String> ingestFilenameAndId : datasetIds.entrySet()) {
              ChironApi.ingest(ingestFilenameAndId);
          }

          Map<String, String> ingestStatuses = new HashMap<>();
          do {
              for (Map.Entry<String, String> fileNameAndId : datasetIds.entrySet()) {
                  ingestStatuses.put(fileNameAndId.getKey(), ChironApi.getIngestionStatus(fileNameAndId.getKey(), fileNameAndId.getValue()));
              }
          } while (notAllIngested(ingestStatuses));

          success = true;
      } catch (Exception e) {
          log.error("IngestRequiredData error", e);
          success = false;
      }

      execution.setVariable("ingestSuccessful", success);
  }

    private boolean notAllIngested(Map<String, String> ingestStatuses) {
        return ingestStatuses.values().stream()
                .filter(status -> !status.equals("Done"))
                .findAny()
                .isPresent();
    }

}
