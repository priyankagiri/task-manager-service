package parser;

import exception.ServiceUnavailableException;

public class UnavailableTaskParser implements TaskParser {
    @Override
    public ParsedTask parse(String text, String today) {
        throw new ServiceUnavailableException("LLM_API_KEY is not set");
    }
}
