package io.yields.bpm.bnp;

import io.yields.bpm.bnp.chiron.ChironApi;
import io.yields.bpm.bnp.chiron.DatasetDTO;
import io.yields.bpm.bnp.config.FileMapping;
import io.yields.bpm.bnp.config.YieldsProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Component
@RequiredArgsConstructor
public class UploadRequiredDataDelegate implements JavaDelegate {

    private static DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern("yyyy_MM_ddHHmmssS");
    private final YieldsProperties yieldsProperties;

    public void execute(DelegateExecution execution) {
        log.info("STARTING UPLOAD STEP");

        boolean success;
        Map<String, String> datasetIds = new HashMap<>();
        List<DatasetDTO> datasets = ChironApi.getDatasets();

        try {
            String localTeam = (String) execution.getVariable("localTeam");
            List<FileMapping> fileMappings = yieldsProperties.getMappings().get(localTeam);

            for (FileMapping fileMapping : fileMappings) {
                uploadFile(datasetIds, datasets, fileMapping.getDataSet(),
                        execution.getVariable(fileMapping.getProcessVariable()), fileMapping.getFileNameTemplate()
                );
            }

            execution.setVariable(ProcessVariables.DATASET_IDS, datasetIds);
            success = true; //no exception
        } catch (Exception e) {
            success = false;
            execution.setVariable(ProcessVariables.processError, e.getMessage());
            log.error("Upload error", e);
        }

        log.info("Upload step success: {}", success);
        execution.setVariable(ProcessVariables.uploadSuccess, success);
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
            if (fileBytes.length > 0) {
//        throw new RuntimeException("No file or empty file uploaded");
                ChironApi.uploadRequiredData(fileBytes, fileName);
                log.info("Uploaded file {} to dataset {}", fileName, dataSetName);
                datasetIds.put(fileName, findDatasetId(datasets, dataSetName));
            }
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
