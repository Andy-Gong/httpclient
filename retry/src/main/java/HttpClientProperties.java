public class HttpClientProperties {
    private int maxThreads;
    private int connectionTimeout;
    private int socketTimeout;
    private int connectionRequestTimeout;
    private int connectionTimeToLive;
    private int maxRetries;
    private long retryInterval;
    private long jitterTime;
    private String coreBaseUrl;
    private String usageBaseUrl;

    public int getMaxThreads() {
        return maxThreads;
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public long getRetryInterval() {
        return retryInterval;
    }

    public void setRetryInterval(long retryInterval) {
        this.retryInterval = retryInterval;
    }

    public long getJitterTime() {
        return jitterTime;
    }

    public void setJitterTime(long jitterTime) {
        this.jitterTime = jitterTime;
    }

    public String getCoreBaseUrl() {
        return coreBaseUrl;
    }

    public void setCoreBaseUrl(String coreBaseUrl) {
        this.coreBaseUrl = coreBaseUrl;
    }

    public String getUsageBaseUrl() {
        return usageBaseUrl;
    }

    public void setUsageBaseUrl(String usageBaseUrl) {
        this.usageBaseUrl = usageBaseUrl;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public int getConnectionRequestTimeout() {
        return connectionRequestTimeout;
    }

    public void setConnectionRequestTimeout(int connectionRequestTimeout) {
        this.connectionRequestTimeout = connectionRequestTimeout;
    }

    public int getConnectionTimeToLive() {
        return connectionTimeToLive;
    }

    public void setConnectionTimeToLive(int connectionTimeToLive) {
        this.connectionTimeToLive = connectionTimeToLive;
    }
}
