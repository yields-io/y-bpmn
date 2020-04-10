package io.yields.bpm.client_name.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.core.ConditionTimeoutException;

import java.util.concurrent.Callable;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.with;


@UtilityClass
@Slf4j
public class RetryUtil {

    public boolean checkWithRetry(Callable<Boolean> conditionEvaluator,
                                  String timeoutMessage) {

        boolean result;
        try {
            with().pollInterval(3, SECONDS)
                    .await()
                    .atMost(5, MINUTES)
                    .until(conditionEvaluator);

            result = true;
        } catch (ConditionTimeoutException e) {
            log.warn(timeoutMessage);
            result = false;
        }

        return result;
    }

}
