package handler;

import org.apache.http.HttpRequest;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.HttpMethod;

public class IOExceptionRetryHandler implements HttpRequestRetryHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(IOExceptionRetryHandler.class);
    /**
     * the number of times a method will be retried
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

    /**
     * There are two types of exceptions when executing http methods,
     * see here http://hc.apache.org/httpclient-3.x/exception-handling.html#Protocol_exceptions
     * <p>
     * 1. transport exceptions
     * 2. protocol exceptions
     * <p>
     * Here, ONLY when transport exceptions happen, we will retry the http method.
     */
    private final Set<Class<? extends IOException>> retriableClasses = new HashSet<Class<? extends IOException>>(
            Arrays.asList(
                    NoHttpResponseException.class,
                    ConnectTimeoutException.class,
                    HttpHostConnectException.class,
                    SocketTimeoutException.class));

    private final Set<String> nonIdempotentMethods = new HashSet<String>(
            Arrays.asList(
                    HttpMethod.POST));

    /**
     * Create the request retry handler using the specified IOException classes
     *
     * @param retryCount how many times to retry; 0 means no retries
     */
    public IOExceptionRetryHandler(final int retryCount, long retryInterval, long jitterTime) {
        super();
        Args.positive(retryCount, "Max retries");
        Args.positive(retryInterval, "Retry interval");
        Args.positive(jitterTime, "Random jitter");
        this.retryCount = retryCount;
        this.retryInterval = retryInterval;
        this.jitterTime = jitterTime;
    }

    /**
     * Determines if a method should be retried after an IOException
     * occurs during execution.
     *
     * @param exception      the exception that occurred
     * @param executionCount the number of times this method has been
     *                       unsuccessfully executed
     * @param context        the context for the request execution
     * @return {@code true} if the method should be retried, {@code false}
     * otherwise
     */
    @Override
    public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
        Args.notNull(exception, "Exception parameter");
        Args.notNull(context, "HTTP context");
        if (executionCount > this.retryCount) {
            // Do not retry if over max retry count
            return false;
        }
        if (!this.retriableClasses.contains(exception.getClass())) {
            boolean isInstance = false;
            for (final Class<? extends IOException> retriableException : this.retriableClasses) {
                if (retriableException.isInstance(exception)) {
                    isInstance = true;
                    break;
                }
            }
            if (!isInstance) {
                return false;
            }
        }
        final HttpClientContext clientContext = HttpClientContext.adapt(context);
        final HttpRequest request = clientContext.getRequest();

        if (!handleAsIdempotent(request)) {
            // Retry if the request isn't considered idempotent
            return false;
        }
        long sleepingTime = getWaitTime();
        try {
            Thread.sleep(sleepingTime);
        } catch (InterruptedException e) {
            LOGGER.info("Ignore InterruptException");
        }
        // otherwise retry
        return true;
    }

    private long getWaitTime() {
        return this.retryInterval + (long) (Math.random() * jitterTime);
    }

    /**
     * @return the maximum number of times a method will be retried
     */
    public int getRetryCount() {
        return retryCount;
    }

    /**
     * http post and patch methods are non-idempotent, so we don't retry them.
     *
     * @param request
     * @return
     */
    protected boolean handleAsIdempotent(final HttpRequest request) {
        return !nonIdempotentMethods.contains(request.getRequestLine().getMethod());
    }
}
