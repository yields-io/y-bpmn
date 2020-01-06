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
public class RunDataCheckDelegate implements JavaDelegate {

    private final YieldsProperties yieldsProperties;

    public void execute(DelegateExecution execution) {
        log.info("STARTING RunDataCheck STEP");

        String localTeam = (String) execution.getVariable("localTeam");
        CheckProps dataCheckProps = yieldsProperties.getDataChecks().get(localTeam);
        log.debug("DatacheckProps: {}", dataCheckProps);
        StageDTO stage = ChironApi.getStage(dataCheckProps.getStageType(), dataCheckProps.getDataSet());
        StartSessionResponse startSessionResponse = ChironApi.startSession(stage.getId());

        boolean success = SessionsCheck.allSessionsCompletedWithSuccess(execution, startSessionResponse.getIds());
        execution.setVariable(ProcessVariables.dataCheckSuccess, success);
        execution.setVariable(ProcessVariables.dataCheckReportUrl,
                String.format(yieldsProperties.getDataCheckReportUrlTemplate(), stage.getId()));

        log.info("RunDataCheck success? {}", success);
    }

}
