package api;

import exception.ApiException;
import exception.FieldIssue;
import exception.ValidationException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * INSTRUCTOR-PROVIDED SIGNATURES ONLY (Step 3) - students write the bodies.
 *
 * The one place where the error shape is built. Target shape:
 *
 * {
 *   "error": {
 *     "code": "VALIDATION_ERROR",
 *     "message": "Validation failed",
 *     "details": [ { "field": "title", "issue": "must not be empty" } ]
 *   }
 * }
 *
 * Hints:
 *  - Build the body with LinkedHashMap / ArrayList (see ListMapEnumPrimer).
 *  - "code" and "message" are ALWAYS Strings.
 *  - "details" is only filled for ValidationException (use instanceof);
 *    for every other exception send an empty list.
 *  - The ApiResponse status comes from e.getStatus().
 *
 * When you copy this into your own project, change the package line to: package api;
 */
public class ErrorResponseFactory {

    private ErrorResponseFactory() {
    }

    /** Known failure (400 / 404 / 422 / 502 / 503). */
    public static ApiResponse from(ApiException e) {
        List<Map<String, String>> details = new ArrayList<>();
        if (e instanceof ValidationException validation) {
            for (FieldIssue issue : validation.getIssues()) {
                Map<String, String> detail = new LinkedHashMap<>();
                detail.put("field", issue.getField());
                detail.put("issue", issue.getIssue());
                details.add(detail);
            }
        } else if (e instanceof exception.BadGatewayException badGateway) {
            for (FieldIssue issue : badGateway.getIssues()) {
                Map<String, String> detail = new LinkedHashMap<>();
                detail.put("field", issue.getField());
                detail.put("issue", issue.getIssue());
                details.add(detail);
            }
        }
        return new ApiResponse(e.getStatus(), errorBody(e.getCode(), e.getMessage(), details));
    }

    /** Anything unexpected: 500, code "INTERNAL_ERROR", same shape. Never leak a stack trace. */
    public static ApiResponse fromUnexpected(Exception e) {
        return new ApiResponse(500, errorBody("INTERNAL_ERROR", "Internal error", List.of()));
    }

    private static Map<String, Object> errorBody(String code, String message,
                                                  List<Map<String, String>> details) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("code", String.valueOf(code));
        error.put("message", String.valueOf(message));
        error.put("details", details);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", error);
        return body;
    }
}
