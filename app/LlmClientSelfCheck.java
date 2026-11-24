package app;

import com.sun.net.httpserver.HttpServer;
import parser.HttpLlmClient;
import parser.LlmClient;
import parser.SimpleJson;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class LlmClientSelfCheck {
    private static String lastPath;
    private static String lastBody;
    private static final Map<String, String> lastHeaders = new HashMap<>();
    private static int failures;

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            lastPath = exchange.getRequestURI().getPath();
            lastBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            lastHeaders.clear();
            exchange.getRequestHeaders().forEach((key, value) -> lastHeaders.put(key.toLowerCase(), value.get(0)));
            int status = 200;
            String reply;
            String key = lastHeaders.getOrDefault("x-api-key", "");
            if (key.equals("bad")) {
                status = 401;
                reply = "{\"error\":{\"message\":\"Incorrect API key provided: sk-abc****xyz\"}}";
            } else if (key.equals("missing-model")) {
                status = 404;
                reply = "{\"error\":{\"message\":\"model: nope\"}}";
            } else {
                reply = "{\"content\":[{\"type\":\"text\",\"text\":\"{\\\"title\\\":\\\"Pay rent\\\"}\"}]}";
            }
            byte[] bytes = reply.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();
        String base = "http://127.0.0.1:" + server.getAddress().getPort();
        String prompt = "Line one\n\"quoted\" back\\slash\ttab and unicode é中";

        LlmClient client = new HttpLlmClient("k1", null, base);
        check("returns reply text", "{\"title\":\"Pay rent\"}", client.send(prompt));
        check("uses Anthropic path", "/v1/messages", lastPath);
        check("sends x-api-key", "k1", lastHeaders.get("x-api-key"));
        check("sends Anthropic version", "2023-06-01", lastHeaders.get("anthropic-version"));
        check("sends max_tokens", true, lastBody.contains("\"max_tokens\":"));
        check("prompt survives escaping", prompt, SimpleJson.firstStringValue(lastBody, "content"));
        check("401 becomes IOException", true, fails(new HttpLlmClient("bad", null, base)));
        check("401 does not echo key fragment", false,
                messageOf(new HttpLlmClient("bad", null, base)).contains("sk-abc"));
        check("404 includes provider explanation", true,
                messageOf(new HttpLlmClient("missing-model", null, base)).contains("model: nope"));
        check("connection refused becomes IOException", true,
                fails(new HttpLlmClient("k", null, "http://127.0.0.1:1")));

        server.stop(0);
        System.out.println();
        System.out.println(failures == 0 ? "All Anthropic LlmClient checks passed." : failures + " check(s) FAILED");
        if (failures > 0)
            System.exit(1);
    }

    private static boolean fails(LlmClient client) {
        return !messageOf(client).isEmpty();
    }

    private static String messageOf(LlmClient client) {
        try {
            client.send("x");
            return "";
        } catch (IOException e) {
            return e.getMessage() == null ? "(no message)" : e.getMessage();
        }
    }

    private static void check(String name, Object expected, Object actual) {
        boolean ok = expected == null ? actual == null : expected.equals(actual);
        System.out.println((ok ? "PASS  " : "FAIL  ") + name
                + (ok ? "" : "  (expected: " + expected + ", actual: " + actual + ")"));
        if (!ok)
            failures++;
    }
}
