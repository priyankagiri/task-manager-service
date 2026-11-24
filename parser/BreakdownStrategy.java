package parser;

import model.TaskRequest;
import java.util.List;

public interface BreakdownStrategy {
    List<TaskRequest> breakDown(String title, String description);
}
