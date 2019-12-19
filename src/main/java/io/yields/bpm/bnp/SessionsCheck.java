package io.yields.bpm.bnp;

import io.yields.bpm.bnp.chiron.ChironApi;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.List;


@Slf4j
@UtilityClass
class SessionsCheck {

    public boolean allSessionsCompletedWithSuccess(List<String> sessionIds) {
        return RetryUtil.checkWithRetry(
                () -> allSessionsSuccessful(sessionIds),
                String.format("Waiting for sessions to complete %s timeouted without success", sessionIds)
        );
    }

    private boolean allSessionsSuccessful(List<String> sessionIds) {
        log.debug("Checking sessions {} status", sessionIds);
        boolean allSuccess = !sessionIds.stream()
                    .map(sessionId -> ChironApi.getSessionDetails(sessionId))
                    .map(sessionDetailsDTO -> sessionDetailsDTO.getStatus())
                    .filter(status -> !status.equals("Success"))
                    .findAny()
                    .isPresent();
        log.info("Checking sessions. allSuccess? {}", allSuccess);
        return allSuccess;
    }

}
