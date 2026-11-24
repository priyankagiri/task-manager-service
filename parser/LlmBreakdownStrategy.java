package parser;

import exception.BadGatewayException;
import model.TaskRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LlmBreakdownStrategy implements BreakdownStrategy {
    private final LlmClient client;

    public LlmBreakdownStrategy(LlmClient client) {
        this.client = client;
    }

    @Override
    public List<TaskRequest> breakDown(String title, String description) {
        try {
            String reply = client.send("Break down this task into one subtask per line, exactly title|description. "
                    + "Task: " + title + " - " + description);
            List<TaskRequest> result = new ArrayList<>();
            for (String line : reply.split("\\R")) {
                String[] parts = line.trim().split("\\|", 2);
                if (parts.length != 2)
                    throw new IllegalArgumentException("Invalid breakdown line");
                result.add(new TaskRequest(parts[0].trim(), parts[1].trim(), "false", null, "MEDIUM"));
            }
            return result;
        } catch (IOException | IllegalArgumentException e) {
            throw new BadGatewayException("The language model breakdown was invalid or unavailable");
        }
    }
}
