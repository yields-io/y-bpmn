package io.yields.bpm.bnp;

import io.yields.bpm.bnp.chiron.ChironApi;
import io.yields.bpm.bnp.chiron.StageDTO;
import io.yields.bpm.bnp.chiron.StartSessionResponse;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.core.ConditionTimeoutException;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.util.List;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Awaitility.with;


@Slf4j
public class RunDataCheckDelegate implements JavaDelegate {


  public void execute(DelegateExecution execution) throws Exception {

    // TODO: parameters should be determined by the following mapping:
    //      BE team needs to run the derivation script for the dataset that is called V12_SCORE_ANALYSIS_BEL
    //      FR team needs to run the derivation script for the dataset that is called V12_SCORE_ANALYSIS_FRA
    // FOR NOW HARDCODED

    StageDTO stage = ChironApi.getStage("Derivation", "V12_SCORE_ANALYSIS_BEL");
    StartSessionResponse startSessionResponse = ChironApi.startSession(stage.getId());

    execution.setVariable(
            "dataCheckSuccessful",
            SessionsCheck.allSessionsCompletedWithSuccess(startSessionResponse.getIds())
    );
 }

}
