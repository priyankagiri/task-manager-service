package app;

import parser.HttpLlmClient;
import parser.LlmClient;

import java.io.IOException;

public class LlmTest {

    public static void main(String[] args) {
        String key = System.getenv("LLM_API_KEY");
        if (key == null || key.isBlank()) {
            System.out
                    .println("LLM_API_KEY is not set. In IntelliJ: Run > Edit Configurations > Environment variables.");
            System.out.println("(In your app this case must become a 503 - see Step 4.)");
            return;
        }
        String model = System.getenv("LLM_MODEL");
        System.out.println("provider = Anthropic"
                + ", model = " + (model == null ? "(Anthropic default)" : model)
                + ", key = " + key.length() + " characters (never print the key itself)");

        LlmClient client = new HttpLlmClient(key, model);
        try {
            String reply = client.send("Reply with the single word OK and nothing else.");
            System.out.println("Model replied: " + reply.trim());
        } catch (IOException e) {
            System.out.println("Call failed: " + e.getMessage());
            System.out.println();
            System.out.println("What to check:");
            System.out.println("  rejected the key (401/403) -> check the Anthropic API key");
            System.out.println("  HTTP 404 or 400            -> the model name is not available: set LLM_MODEL");
            System.out.println("  HTTP 429                   -> rate limit or no credit on the account");
            System.out.println("  timeout / UnknownHost      -> no internet, or a proxy/VPN is in the way");
            System.out.println("(In your app every one of these must become a 502 - see Step 4.)");
        }
    }
}
