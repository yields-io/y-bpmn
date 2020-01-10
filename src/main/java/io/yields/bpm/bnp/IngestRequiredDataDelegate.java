package io.yields.bpm.bnp;

import io.yields.bpm.bnp.chiron.ChironApi;
import io.yields.bpm.bnp.config.YieldsProperties;
import io.yields.bpm.bnp.util.RetryUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@Component
@RequiredArgsConstructor
public class IngestRequiredDataDelegate implements JavaDelegate {

    private final YieldsProperties yieldsProperties;

    public void execute(DelegateExecution execution) {
        log.info("STARTING INGEST STEP");
        Map<String, String> datasetIds = (Map<String, String>) execution.getVariable(ProcessVariables.DATASET_IDS);
        boolean success;

        try {
            String localTeam = (String) execution.getVariable("localTeam");
            String filePrefix = localTeam.substring(0, 3);
            // ingest only files belonging to localTeam
            Map<String, String> localTeamDataSetIds = datasetIds.entrySet().stream()
                    .filter(entry -> entry.getKey().toUpperCase().contains(filePrefix.toUpperCase()))
                    .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));

            for (Map.Entry<String, String> ingestFilenameAndId : localTeamDataSetIds.entrySet()) {
                ChironApi.ingest(ingestFilenameAndId);
            }

            success = localTeamDataSetIds.size() == 0 || RetryUtil.checkWithRetry(
                    () -> {
                        Map<String, String> ingestStatuses = new HashMap<>();
                        for (Map.Entry<String, String> fileNameAndId : localTeamDataSetIds.entrySet()) {
                            ingestStatuses.put(fileNameAndId.getKey(), ChironApi.getIngestionStatus(fileNameAndId.getKey(), fileNameAndId.getValue()));
                        }
                        breakIfError(ingestStatuses);
                        boolean allFilesIngested = allIngested(ingestStatuses);
                        log.info("Retried all ingested check, All files ingested? {}", allFilesIngested);
                        return allFilesIngested;
                    },
                    String.format("Checking ingest statuses timeouted for %s", localTeamDataSetIds)
            );
        } catch (Exception e) {
            log.error("IngestRequiredData error", e);
            execution.setVariableLocal(ProcessVariables.processError, e.getMessage());
            success = false;
        }

        log.info("Ingest step success: {}", success);
        execution.setVariable("ingestSuccessful", success);
    }

    private boolean allIngested(Map<String, String> ingestStatuses) {
        return !ingestStatuses.values().stream()
                .filter(status -> !status.equals("Done"))
                .findAny()
                .isPresent();
    }

    private void breakIfError(Map<String, String> ingestStatuses) {
        Optional<Map.Entry<String, String>> error = ingestStatuses.entrySet().stream()
                .filter(entry -> entry.getValue().equals("Error"))
                .findAny();
        if (error.isPresent()) {
            throw new RuntimeException("Ingestion error for file: " + error.get().getKey());
        }
    }

}
