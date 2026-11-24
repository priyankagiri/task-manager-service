package exception;

import java.util.ArrayList;
import java.util.List;


public class BadGatewayException extends ApiException {

    private static final long serialVersionUID = 1L;
    private final List<FieldIssue> issues;

    public BadGatewayException(String message) {
        this(message, new ArrayList<>());
    }

    public BadGatewayException(String message, List<FieldIssue> issues) {
        super(502, "BAD_GATEWAY", message);
        this.issues = new ArrayList<>(issues);
    }

    public List<FieldIssue> getIssues() {
        return new ArrayList<>(issues);
    }
}
