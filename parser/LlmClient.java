package parser;

import java.io.IOException;


public interface LlmClient {

    String send(String prompt) throws IOException;
}
