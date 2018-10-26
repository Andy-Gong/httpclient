package exception;

public class HttpResponseError extends RuntimeException {

    private final int statusCode;

    public HttpResponseError(final int statusCode, final String s) {
        super(s);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

}
