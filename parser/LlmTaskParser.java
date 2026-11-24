package parser;

import exception.BadGatewayException;
import java.io.IOException;
import java.util.Map;

public class LlmTaskParser implements TaskParser {
    private final LlmClient client;

    public LlmTaskParser(LlmClient client) {
        this.client = client;
    }

    @Override
    public ParsedTask parse(String text, String today) {
        String prompt = "Today is " + today + ". Convert the user's request into ONLY a flat JSON object "
                + "with string fields title, description, dueDate, recurrence. Use null when absent. User: " + text;
        try {
            Map<String, String> values = SimpleJson.parseFlat(client.send(prompt));
            return new ParsedTask(values.get("title"), values.get("description"),
                    values.get("dueDate"), values.get("recurrence"));
        } catch (IOException | IllegalArgumentException e) {
            throw new BadGatewayException("The language model response was invalid or unavailable");
        }
    }
}
