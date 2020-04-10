package io.yields.bpm.client_name.util;

import com.google.common.collect.Maps;
import io.yields.bpm.client_name.ProcessVariables;
import io.yields.bpm.client_name.chiron.ChironApi;
import io.yields.bpm.client_name.chiron.SessionDetailsDTO;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Slf4j
@UtilityClass
public class SessionsCheck {

    public boolean allSessionsCompletedWithSuccess(DelegateExecution execution, List<String> sessionIds) {
        boolean success = false;
        try {
            success = RetryUtil.checkWithRetry(
                    () -> {
                        // sessionId -> sessionStatus
                        Map<String, String> sessionStatuses = findSessionStatuses(sessionIds);
                        breakIfError(sessionStatuses);
                        boolean allSuccess = allSessionsSuccessful(sessionStatuses.values());
                        return allSuccess;
                    },
                    String.format("Waiting for sessions %s to complete timeouted without success", sessionIds)
            );
        } catch (Exception e) {
            log.error("Session error", e);
            execution.setVariableLocal(ProcessVariables.processError, e.getMessage());
        }

        return success;
    }

    private static Map<String, String> findSessionStatuses(List<String> sessionIds) {
        Map<String, String> result = Maps.newHashMap();
        sessionIds.forEach(sessionId -> {
            SessionDetailsDTO sessionDetails = ChironApi.getSessionDetails(sessionId);
            result.put(sessionId, sessionDetails.getStatus());
        });
        return result;
    }

    private boolean allSessionsSuccessful(Collection<String> statuses) {
        boolean allSuccess = !statuses.stream()
                    .filter(status -> !status.equals("Success"))
                    .findAny()
                    .isPresent();
        log.info("Checking sessions. allSuccess? {}", allSuccess);
        return allSuccess;
    }

    private void breakIfError(Map<String, String> sessionStatuses) {
        Optional<Map.Entry<String, String>> error = sessionStatuses.entrySet().stream()
                .filter(entry -> entry.getValue().equals("Error"))
                .findAny();
        if (error.isPresent()) {
            throw new RuntimeException("Session error. Session id: " + error.get().getKey());
        }
    }

}
