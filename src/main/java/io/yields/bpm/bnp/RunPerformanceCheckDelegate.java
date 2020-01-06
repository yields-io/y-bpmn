package io.yields.bpm.bnp;

import io.yields.bpm.bnp.chiron.ChironApi;
import io.yields.bpm.bnp.chiron.StageDTO;
import io.yields.bpm.bnp.chiron.StartSessionResponse;
import io.yields.bpm.bnp.config.CheckProps;
import io.yields.bpm.bnp.config.YieldsProperties;
import io.yields.bpm.bnp.util.SessionsCheck;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class RunPerformanceCheckDelegate implements JavaDelegate {

    private final YieldsProperties yieldsProperties;

    public void execute(DelegateExecution execution) {
        log.info("STARTING RunPerformanceCheck STEP");
        boolean success = false;

        String localTeam = (String) execution.getVariable("localTeam");
        CheckProps performanceCheckProps = yieldsProperties.getPerformanceChecks().get(localTeam);
        log.debug("performanceCheckProps: {}", performanceCheckProps);

        try {
            StageDTO stage = ChironApi.getStage(performanceCheckProps.getStageType(), performanceCheckProps.getDataSet());

            StartSessionResponse startSessionResponse = ChironApi.startSession(stage.getId());
            success = SessionsCheck.allSessionsCompletedWithSuccess(execution, startSessionResponse.getIds());
            execution.setVariable(ProcessVariables.performanceCheckReportUrl,
                    String.format(yieldsProperties.getPerformanceCheckReportUrlTemplate(), stage.getId())
            );
        } catch (Exception e) {
            execution.setVariable(ProcessVariables.processError,
                    e.getMessage() + String.format("Stage: %s, dataset: %s", performanceCheckProps.getStageType(), performanceCheckProps.getDataSet())
            );
        }

        execution.setVariable(ProcessVariables.performanceCheckSuccess, success);

        log.info("RunPerformanceCheck success: {}", success);
    }

}
