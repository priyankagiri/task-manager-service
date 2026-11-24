package parser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class HttpLlmClient implements LlmClient {
    private static final Duration TIMEOUT = Duration.ofSeconds(30);
    private final String apiKey;
    private final String model;
    private final String baseUrl;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();

    public HttpLlmClient(String apiKey, String model) {
        this(apiKey, model, null);
    }

    /** The base URL is injectable for local tests and defaults to Anthropic. */
    public HttpLlmClient(String apiKey, String model, String baseUrl) {
        this.apiKey = apiKey;
        this.model = model == null || model.isBlank() ? "claude-haiku-5-5" : model.trim();
        this.baseUrl = baseUrl == null || baseUrl.isBlank() ? "https://api.anthropic.com" : baseUrl;
    }

    @Override
    public String send(String prompt) throws IOException {
        HttpRequest request;
        try {
            request = buildRequest(prompt);
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid LLM configuration (check LLM_MODEL): " + e.getMessage(), e);
        }
        HttpResponse<String> response;
        try {
            response = http.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("LLM call interrupted", e);
        }
        if (response.statusCode() / 100 != 2) {
            int status = response.statusCode();
            if (status == 401 || status == 403) {
                throw new IOException("Anthropic rejected the key (HTTP " + status
                        + "): check LLM_API_KEY");
            }
            String detail = SimpleJson.firstStringValue(response.body(), "message");
            throw new IOException("Anthropic returned HTTP " + status
                    + (detail == null ? "" : ": " + detail));
        }
        String reply = SimpleJson.firstStringValue(response.body(), "text");
        if (reply == null) {
            throw new IOException("Anthropic response contained no text");
        }
        return reply;
    }

    private HttpRequest buildRequest(String prompt) {
        String quoted = SimpleJson.quote(prompt);
        return HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v1/messages"))
                .timeout(TIMEOUT)
                .header("Content-Type", "application/json")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .POST(HttpRequest.BodyPublishers.ofString(
                        "{\"model\":" + SimpleJson.quote(model) + ",\"max_tokens\":1024,"
                                + "\"messages\":[{\"role\":\"user\",\"content\":" + quoted + "}]}"))
                .build();
    }
}
