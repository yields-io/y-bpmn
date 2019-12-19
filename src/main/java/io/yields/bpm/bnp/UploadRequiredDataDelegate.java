package io.yields.bpm.bnp;

import io.yields.bpm.bnp.chiron.ChironApi;
import io.yields.bpm.bnp.chiron.DatasetDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


@Slf4j
public class UploadRequiredDataDelegate implements JavaDelegate {

  private static DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern("yyyy_MM_ddHHmmssS");

  public void execute(DelegateExecution execution) {
    Map<String, String > datasetIds = new HashMap<>();
    boolean success;
    List<DatasetDTO> datasets = ChironApi.getDatasets();

    try {
      // TODO: this is done for BEL only
      //      T_CRITERE_BEL
      //      • T_REF_BEL
      //      • T_SCORE_BEL
      //      • T_SCORE_VAR_BEL
      //      • T_MONITORING_BELA0014V00
      uploadFile(datasetIds, datasets, "V12_CRITERE_BEL",
              execution.getVariable(ProcessVariables.T_CRITERE_BEL), "T_CRITERE_%s_BEL.csv");

      uploadFile(datasetIds, datasets, "V12_REF_BEL",
              execution.getVariable(ProcessVariables.T_REF_BEL), "V12_REF_%s_BEL.csv");

      uploadFile(datasetIds, datasets, "V12_SCORE_BEL",
              execution.getVariable(ProcessVariables.T_SCORE_BEL), "T_SCORE_%s_BEL.csv");

      uploadFile(datasetIds, datasets, "V12_SCORE_VAR_BEL",
              execution.getVariable(ProcessVariables.T_SCORE_VAR_BEL), "T_SCORE_VAR_%s_BEL.csv");

      uploadFile(datasetIds, datasets, "V12_MONITORING_BELA0014V00",
              execution.getVariable(ProcessVariables.T_MONITORING_BEL), "T_MONITORING_%s_BELA0014V00.csv");

      // TODO: for FRA it should be:
      //      • T_CRITERE_FRA
      //      • T_REF_FRA
      //      • T_SCORE_FRA
      //      • T_SCORE_VAR_FRA
      //      • T_MONITORING_FRAA0014V00
      //      • T_MONITORING_FRAB0001V00
      execution.setVariable(ProcessVariables.DATASET_IDS, datasetIds);
      success = true; //no exception
    } catch (Exception e) {
      success = false;
      log.error("Upload error", e);
//      throw new RuntimeException("Upload error", e);
    }

    execution.setVariable("uploadSuccess", success);
  }

  private void uploadFile(Map<String, String> datasetIds,
                          List<DatasetDTO> datasets,
                          String dataSetName,
                          Object fileVariable,
                          String fileNameTemplate) throws IOException {
    if (fileVariable != null && fileVariable instanceof ByteArrayInputStream) {
      ByteArrayInputStream fileData = (ByteArrayInputStream) fileVariable;
      String fileName = String.format(fileNameTemplate, DT_FORMATTER.format(LocalDateTime.now()));
      byte[] fileBytes = IOUtils.toByteArray(fileData);
      if (!(fileBytes.length > 0)) {
        throw new RuntimeException("No file or empty file uploaded");
      }
      ChironApi.uploadRequiredData(fileBytes, fileName);
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
