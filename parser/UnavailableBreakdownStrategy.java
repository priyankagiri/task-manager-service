package parser;

import exception.ServiceUnavailableException;
import model.TaskRequest;
import java.util.List;

public class UnavailableBreakdownStrategy implements BreakdownStrategy {
    @Override
    public List<TaskRequest> breakDown(String title, String description) {
        throw new ServiceUnavailableException("LLM_API_KEY is not set");
    }
}
