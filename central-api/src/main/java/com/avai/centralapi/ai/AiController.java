package com.avai.centralapi.ai;

import com.avai.centralapi.auth.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {
    private final AuthService auth;
    private final RestClient client;
    private final String apiKey;
    private final String model;

    public AiController(AuthService auth,
                        @Value("${openrouter.api-key:}") String apiKey,
                        @Value("${openrouter.model:openrouter/free}") String model) {
        this.auth = auth;
        this.apiKey = apiKey;
        this.model = model;
        this.client = RestClient.builder().baseUrl("https://openrouter.ai/api/v1").build();
    }

    @PostMapping(value = "/ask", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> ask(@RequestHeader(value = "Authorization", required = false) String authorization,
                                   @RequestBody AskRequest request) {
        String username = auth.requireUser(authorization);
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OpenRouter API key is not configured on the central server");
        }
        if (request.question() == null || request.question().isBlank()) {
            throw new IllegalArgumentException("Question is required");
        }

        Map<String, Object> payload = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", "You are AV AI. Answer clearly and helpfully."),
                        Map.of("role", "user", "content", request.question().trim())
                )
        );

        Map<?, ?> response = client.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(Map.class);

        Object answer = "";
        if (response != null) {
            Object choices = response.get("choices");
            if (choices instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof Map<?, ?> choice) {
                Object message = choice.get("message");
                if (message instanceof Map<?, ?> msg) {
                    Object content = msg.get("content");
                    answer = content == null ? "" : content;
                }
            }
        }
        return Map.of("success", true, "username", username, "answer", answer);
    }

    public record AskRequest(String question) {}
}
