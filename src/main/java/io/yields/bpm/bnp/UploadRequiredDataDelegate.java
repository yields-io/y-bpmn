package io.yields.bpm.bnp;

import io.yields.bpm.bnp.chiron.ChironApi;
import io.yields.bpm.bnp.chiron.DatasetDTO;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


public class UploadRequiredDataDelegate implements JavaDelegate {

  private final static Logger LOGGER = Logger.getLogger(UploadRequiredDataDelegate.class.getName());
  private static DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern("yyyy_MM_ddHHmmssS");

  public void execute(DelegateExecution execution) {
    Map<String, String > datasetIds = new HashMap<>();
    boolean success;

    List<DatasetDTO> datasets = ChironApi.getDatasets();

    try {
      uploadFile(datasetIds, datasets, "V12_SCORE_BEL",
              execution.getVariable(ProcessVariables.T_SCORE_BEL), "T_SCORE_%s_BEL.csv");
      uploadFile(datasetIds, datasets, "V12_MONITORING_BELA0014V00",
              execution.getVariable(ProcessVariables.T_MONITORING_BEL), "T_MONITORING_%s_BELA0014V00.csv");

      // success = datasetIds.size() == 4;
      execution.setVariable(ProcessVariables.DATASET_IDS, datasetIds);
      success = true;
    } catch (Exception e) {
      success = false;
      LOGGER.warning(e.getMessage());
    }

    execution.setVariable("uploadSuccess", success);
  }

  private void uploadFile(Map<String, String> datasetIds,
                          List<DatasetDTO> datasets,
                          String dataSetName,
                          Object fileVariable,
                          String fileNameTemplate) {
    if (fileVariable != null && fileVariable instanceof ByteArrayInputStream) {
      ByteArrayInputStream scoreBelData = (ByteArrayInputStream) fileVariable;
      String fileName = String.format(fileNameTemplate, DT_FORMATTER.format(LocalDateTime.now()));
      ChironApi.uploadRequiredData(scoreBelData, fileName);
      datasetIds.put(fileName, findDatasetId(datasets, dataSetName));
    }
  }

  private String findDatasetId(List<DatasetDTO> datasets, String datasetName) {
    return datasets.stream()
            .filter(dataset -> dataset.getName().equals(datasetName))
            .map(dataset -> dataset.getId())
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Could not find dataset: " + datasetName));
  }

}
