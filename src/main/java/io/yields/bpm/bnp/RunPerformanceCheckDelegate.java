package io.yields.bpm.bnp;

import io.yields.bpm.bnp.config.CheckProps;
import io.yields.bpm.bnp.config.YieldsProperties;
import io.yields.bpm.bnp.util.SessionRunner;
import io.yields.bpm.bnp.util.SessionRunner.SessionRunResult;
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
            SessionRunResult sessionResult = SessionRunner.runSessionAndGetReport(performanceCheckProps, execution);
            success = sessionResult.isSuccess();

            execution.setVariableLocal(ProcessVariables.performanceCheckReport, sessionResult.getReport());
            execution.setVariable(ProcessVariables.performanceCheckReport + "_" + localTeam, sessionResult.getReport());
        } catch (Exception e) {
            execution.setVariable(ProcessVariables.processError,
                    e.getMessage() + String.format("Stage: %s, dataset: %s", performanceCheckProps.getStageType(), performanceCheckProps.getDataSet())
            );
        }
        execution.setVariable(ProcessVariables.performanceCheckSuccess, success);
        log.info("RunPerformanceCheck success: {}", success);
    }

}
