package parser;

import exception.ServiceUnavailableException;

public final class ParserFactory {
    private ParserFactory() {
    }

    public static TaskParser create() {
        String key = System.getenv("LLM_API_KEY");
        if (key == null || key.isBlank())
            return new UnavailableTaskParser();
        return new LlmTaskParser(new HttpLlmClient(key, System.getenv("LLM_MODEL")));
    }
}
