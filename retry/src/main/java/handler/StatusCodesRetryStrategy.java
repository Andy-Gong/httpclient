package handler;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class StatusCodesRetryStrategy implements ServiceUnavailableRetryStrategy {

    private static final Set RETRIABLE_HTTPCODES = new HashSet(Arrays.asList(
            HttpStatus.SC_REQUEST_TIMEOUT,
            HttpStatus.SC_CONFLICT,
            HttpStatus.SC_SERVICE_UNAVAILABLE,
            HttpStatus.SC_INTERNAL_SERVER_ERROR,
            HttpStatus.SC_GATEWAY_TIMEOUT
    ));

    /**
     * Maximum number of allowed retries if the server responds with a HTTP code
     * in our retry code list. Default value is 1.
     */
    private final int retryCount;

    /**
     * Retry interval between subsequent requests, in milliseconds.
     */
    private final long retryInterval;

    /**
     * random jitter time between subsequent requests, in milliseconds.
     */
    private final long jitterTime;

    public StatusCodesRetryStrategy(int retryCount, long retryInterval, long jitterTime) {
        super();
        Args.positive(retryCount, "Max retries");
        Args.positive(retryInterval, "Retry interval");
        Args.positive(jitterTime, "Random jitter");
        this.retryCount = retryCount;
        this.retryInterval = retryInterval;
        this.jitterTime = jitterTime;
    }

    /**
     * Determines if a method should be retried given the response from the target server.
     *
     * @param response       the response from the target server
     * @param executionCount the number of times this method has been
     *                       unsuccessfully executed
     * @param context        the context for the request execution
     * @return {@code true} if the method should be retried, {@code false}
     * otherwise
     */
    @Override
    public boolean retryRequest(HttpResponse response, int executionCount, HttpContext context) {
        return executionCount <= retryCount
                && RETRIABLE_HTTPCODES.contains(response.getStatusLine().getStatusCode());
    }

    /**
     * Jitter
     *
     * @return The interval between the subsequent auto-retries.
     */
    @Override
    public long getRetryInterval() {
        return this.retryInterval + (long) (Math.random() * jitterTime);
    }
}
