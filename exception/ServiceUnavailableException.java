package exception;

public class ServiceUnavailableException extends ApiException {

    private static final long serialVersionUID = 1L;
    public ServiceUnavailableException(String message) {
        super(503, "SERVICE_UNAVAILABLE", message);
    }
}
