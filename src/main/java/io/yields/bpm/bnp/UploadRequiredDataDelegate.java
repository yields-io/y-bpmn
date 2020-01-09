package io.yields.bpm.bnp;

import io.yields.bpm.bnp.chiron.ChironApi;
import io.yields.bpm.bnp.chiron.DatasetDTO;
import io.yields.bpm.bnp.config.FileMapping;
import io.yields.bpm.bnp.config.YieldsProperties;
import io.yields.bpm.bnp.util.DataSetUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.value.FileValue;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Component
@RequiredArgsConstructor
public class UploadRequiredDataDelegate implements JavaDelegate {

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
                uploadFile(execution,
                        datasetIds, datasets,
                        fileMapping.getDataSet(),
                        fileMapping.getProcessVariable()
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

    private void uploadFile(DelegateExecution execution,
                            Map<String, String> datasetIds,
                            List<DatasetDTO> datasets,
                            String dataSetName,
                            String fileVariable) throws IOException {

        FileValue fileValue = execution.getVariableTyped(fileVariable);
        ByteArrayInputStream fileData = (ByteArrayInputStream) execution.getVariable(fileVariable);

        if (fileData != null) {
            byte[] fileBytes = IOUtils.toByteArray(fileData);
            if (fileBytes.length > 0) {
//        throw new RuntimeException("No file or empty file uploaded");

                ChironApi.uploadFile(fileBytes, fileValue.getFilename());
                log.info("Uploaded file {} to dataset {}", fileValue.getFilename(), dataSetName);
                datasetIds.put(fileValue.getFilename(), DataSetUtil.findDatasetId(datasets, dataSetName));
            }
        }
    }


}
