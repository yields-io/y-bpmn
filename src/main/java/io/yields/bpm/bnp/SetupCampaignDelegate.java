package io.yields.bpm.bnp;

import io.yields.bpm.bnp.chiron.ChironApi;
import io.yields.bpm.bnp.chiron.DatasetDTO;
import io.yields.bpm.bnp.config.YieldsProperties;
import io.yields.bpm.bnp.util.DataSetUtil;
import io.yields.bpm.bnp.util.RetryUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class SetupCampaignDelegate implements JavaDelegate {

    private final YieldsProperties yieldsProperties;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
       log.info("STARTING SetupCampaign");

//       String campaingId = (String) execution.getVariable("campaignId");
//       String campaingStart = (String) execution.getVariable("campaignStartDate");
//       String campaingEnd = (String) execution.getVariable("campaignEndDate");
//
//       byte[] fileData = createCampaingFile(campaingId, campaingStart, campaingEnd);
//       String fileName = String.format("T_CAMPAIGN_%s.csv", campaingId);
//       ChironApi.uploadFile(fileData, fileName);
//       String dataSetId = findDataSetId();
//       ChironApi.ingest(new SimpleEntry<>(fileName, dataSetId));
//
//       RetryUtil.checkWithRetry(
//                () -> {
//                    String ingestStatus = ChironApi.getIngestionStatus(fileName, dataSetId);
//                    if (ingestStatus.equals("Error")) {
//                        throw new RuntimeException("Ingest error");
//                    }
//                    return ingestStatus.equals("Done");
//                },
//                String.format("Checking ingest status timeouted for %s", dataSetId)
//        );

        log.info("SetupCampaign DONE");
    }

    private byte[] createCampaingFile(String campaignId, String campaignStart, String campaignEnd) {
        String header = "\"CAMPAIGN_ID\",\"CAMPAIGN_START\",\"CAMPAIGN_END\",\"T_SCORE_COMP\",\"T_SCORE_VAR_COMP\",\"T_REF_COMP\",\"T_MONITORING_COMP\",\"T_CRITERE_COMP\"\n";
        String values = String.format(
            "\"%s\",\"%s\",\"%s\",\"\",\"\",\"\",\"\",\"\"\n",
            campaignId, campaignStart, campaignEnd
        );

        String fileContent = header + values;
        return fileContent.getBytes();
    }

    private String findDataSetId() {
        List<DatasetDTO> datasets = ChironApi.getDatasets();
        return DataSetUtil.findDatasetId(datasets, yieldsProperties.getSetupCampaignDataSet());
    }
}
