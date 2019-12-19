package io.yields.bpm.bnp;

import io.yields.bpm.bnp.chiron.ChironApi;
import io.yields.bpm.bnp.chiron.StageDTO;
import io.yields.bpm.bnp.chiron.StartSessionResponse;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;


@Slf4j
public class RunDataCheckDelegate implements JavaDelegate {


  public void execute(DelegateExecution execution) throws Exception {
    log.info("STARTING RunDataCheck STEP");

    // TODO: parameters should be determined by the following mapping:
    //      BE team needs to run the derivation script for the dataset that is called V12_SCORE_ANALYSIS_BEL
    //      FR team needs to run the derivation script for the dataset that is called V12_SCORE_ANALYSIS_FRA
    // FOR NOW HARDCODED

    StageDTO stage = ChironApi.getStage("Derivation", "V12_SCORE_ANALYSIS_BEL");
    StartSessionResponse startSessionResponse = ChironApi.startSession(stage.getId());

    boolean success = SessionsCheck.allSessionsCompletedWithSuccess(startSessionResponse.getIds());
    execution.setVariable("dataCheckSuccessful", success);

    log.info("RunDataCheck success? {}", success);
 }

}
