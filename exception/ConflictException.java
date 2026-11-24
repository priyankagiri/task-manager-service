package exception;


public class ConflictException extends ApiException {

    private static final long serialVersionUID = 1L;
    public ConflictException(String message) {
        super(422, "CONFLICT", message);
    }
}
