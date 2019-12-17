package io.yields.bpm.bnp;

import io.yields.bpm.bnp.chiron.ChironApi;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.core.ConditionTimeoutException;

import java.util.List;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.reset;
import static org.awaitility.Awaitility.with;


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
        log.debug("allSuccess? {}", allSuccess);
        return allSuccess;
    }

}
