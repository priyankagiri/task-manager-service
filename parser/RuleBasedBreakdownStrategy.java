package parser;

import model.TaskRequest;
import java.util.ArrayList;
import java.util.List;

public class RuleBasedBreakdownStrategy implements BreakdownStrategy {
    @Override
    public List<TaskRequest> breakDown(String title, String description) {
        List<TaskRequest> result = new ArrayList<>();
        for (String part : description.split("(?i)\\band\\b|,")) {
            String value = part.trim();
            if (!value.isEmpty())
                result.add(new TaskRequest(value, value, "false", null, "MEDIUM"));
        }
        return result;
    }
}
