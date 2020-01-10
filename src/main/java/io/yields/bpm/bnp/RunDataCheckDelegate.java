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
public class RunDataCheckDelegate implements JavaDelegate {

    private final YieldsProperties yieldsProperties;

    public void execute(DelegateExecution execution) {
        log.info("STARTING RunDataCheck STEP");
        boolean success = false;

        try {
            String localTeam = (String) execution.getVariable("localTeam");
            CheckProps dataCheckProps = yieldsProperties.getDataChecks().get(localTeam);
            log.debug("DatacheckProps: {}", dataCheckProps);

            SessionRunResult sessionResult = SessionRunner.runSessionAndGetReport(dataCheckProps, execution);
            success = sessionResult.isSuccess();
            execution.setVariableLocal(ProcessVariables.dataCheckReport, sessionResult.getReport());
            execution.setVariableLocal(ProcessVariables.dataCheckReport + "_" + localTeam , sessionResult.getReport());
        } catch (Exception e) {
            execution.setVariableLocal(ProcessVariables.processError, e.getMessage());
        }

        execution.setVariable(ProcessVariables.dataCheckSuccess, success);
        log.info("RunDataCheck success? {}", success);
    }

}
