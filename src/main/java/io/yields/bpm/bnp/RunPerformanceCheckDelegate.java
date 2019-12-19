package io.yields.bpm.bnp;

import io.yields.bpm.bnp.chiron.ChironApi;
import io.yields.bpm.bnp.chiron.StageDTO;
import io.yields.bpm.bnp.chiron.StartSessionResponse;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;


@Slf4j
public class RunPerformanceCheckDelegate implements JavaDelegate {


  public void execute(DelegateExecution execution) {
      log.info("STARTING RunPerformanceCheck STEP");
      // TODO: parameters should be determined by the following mapping:
      //      BE team needs to run the derivation script for the dataset that is called V12_SCORE_ANALYSIS_BEL
      //      FR team needs to run the derivation script for the dataset that is called V12_SCORE_ANALYSIS_FRA
      // FOR NOW HARDCODED

      //stages?stageType=Analysis&name=V12_BELA0014V00_SCORE_ANALYSIS
      StageDTO stage = ChironApi.getStage("Analysis", "V12_BELA0014V00_SCORE_ANALYSIS");
      StartSessionResponse startSessionResponse = ChironApi.startSession(stage.getId());

      boolean success = SessionsCheck.allSessionsCompletedWithSuccess(startSessionResponse.getIds());
      execution.setVariable("performanceCheckSuccessful", success);

      log.info("RunPerformanceCheck success: {}", success);
  }

}
