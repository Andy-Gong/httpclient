import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import exception.HttpResponseError;
import handler.IOExceptionRetryHandler;
import handler.StatusCodesRetryStrategy;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.Args;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class HttpClient {
    private static final Logger logger = LoggerFactory.getLogger(HttpClient.class);
    protected final HttpClientProperties httpClientProperties;
    protected final CloseableHttpClient closeableHttpClient;
    protected static final ObjectMapper jsonMapper = new ObjectMapper();

    static {
        jsonMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public HttpClient(HttpClientProperties httpClientProperties) {
        this.httpClientProperties = httpClientProperties;
        closeableHttpClient = newHttpClient();
    }

    public <T> T get(String url, Class<T> clazz) {
        Args.notNull(url, "url is null");
        Args.notNull(clazz, "clazz is null");
        HttpUriRequest request = new HttpGet(url);
        CloseableHttpResponse response = null;
        try {
            response = closeableHttpClient.execute(request);
            StatusLine status = response.getStatusLine();
            HttpEntity entity = response.getEntity();
            if (status.getStatusCode() == HttpStatus.SC_OK) {
                String content = EntityUtils.toString(entity, StandardCharsets.UTF_8.name());
                return jsonMapper.readValue(content, clazz);
            } else {
                throw new HttpResponseError(status.getStatusCode(), status.getReasonPhrase());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error happens when parsing http response body.", e);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    logger.info("Ignore IOException when closing response.");
                }
            }
        }
    }

    private CloseableHttpClient newHttpClient() {
        PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager();
        poolingHttpClientConnectionManager.setMaxTotal(httpClientProperties.getMaxThreads());
        poolingHttpClientConnectionManager.setDefaultMaxPerRoute(httpClientProperties.getMaxThreads());
        /**
         * ConnectionTimeout:
         *    the time to establish the connection with the remote host
         * SocketTimeout:
         *    the time waiting for data – after the connection was established; maximum time of inactivity between two data packets
         * ConnectionRequestTimeout:
         *    the time requesting a connection from the connection manager
         */
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setSocketTimeout(httpClientProperties.getSocketTimeout())
                .setConnectTimeout(httpClientProperties.getConnectionTimeout())
                .setConnectionRequestTimeout(httpClientProperties.getConnectionRequestTimeout())
                .build();
        return HttpClients.custom()
                .setDefaultRequestConfig(defaultRequestConfig)
                .setConnectionTimeToLive(httpClientProperties.getConnectionTimeToLive(), TimeUnit.MILLISECONDS)
                .setConnectionManager(poolingHttpClientConnectionManager)
                .setServiceUnavailableRetryStrategy(
                        new StatusCodesRetryStrategy(
                                httpClientProperties.getMaxRetries(),
                                httpClientProperties.getRetryInterval(),
                                httpClientProperties.getJitterTime()))
                .setRetryHandler(
                        new IOExceptionRetryHandler(
                                httpClientProperties.getMaxRetries(),
                                httpClientProperties.getRetryInterval(),
                                httpClientProperties.getJitterTime()))
                .build();
    }
}
