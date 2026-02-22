package com.pulsedesk.triage.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsedesk.triage.dto.AiTriageResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class HuggingFaceService {

    @Value("${hf.api.key:}")
    private String apiKey;

    @Value("${hf.model:HuggingFaceTB/SmolLM3-3B:hf-inference}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    private static final String CHAT_URL = "https://router.huggingface.co/v1/chat/completions";

    public String analyze(String text) {
        return analyzeToResult(text).toJsonString();
    }

    public AiTriageResult analyzeToResult(String text) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("HF api key is missing. Set HF_API_KEY env variable.");
        }

        String raw1 = callChat(buildMainBody(text));
        AiTriageResult r1 = tryParseAiResult(raw1);
        if (r1 != null) return applyRules(text, r1);

        String raw2 = callChat(buildRepairBody(raw1));
        AiTriageResult r2 = tryParseAiResult(raw2);
        if (r2 != null) return applyRules(text, r2);

        AiTriageResult fallback = new AiTriageResult();
        fallback.setIsTicket(false);
        fallback.setTitle("");
        fallback.setCategory("other");
        fallback.setPriority("medium");
        fallback.setSummary(text != null ? text.substring(0, Math.min(500, text.length())) : "");
        return applyRules(text, fallback);
    }

    // --- BODY builders ---

    private Map<String, Object> buildMainBody(String text) {
        String schema =
                "{\"isTicket\":true|false," +
                "\"title\":\"...\", " +
                "\"category\":\"bug|feature|billing|account|other\", " +
                "\"priority\":\"low|medium|high\", " +
                "\"summary\":\"short summary, max 2 sentences\"}";

        String system =
                "You are a strict JSON generator for comment-to-ticket triage. " +
                "Output ONLY one JSON object, nothing else. No <think>, no explanation, no markdown. " +
                "Do NOT wrap JSON in quotes. The response MUST start with '{' and end with '}'. " +
                "summary must be a SHORT summary of the comment: maximum 2 sentences, concise. " +
                "Rules for isTicket: " +
                "isTicket=true if comment requires action: bug report (crash/error/not working/broken), " +
                "feature request (please add/would be nice), billing issue (charged/refund/invoice), " +
                "account issue (cannot login/password reset), support request (how do I/need help). " +
                "isTicket=false if compliment, general feedback without request, emotional reaction, or sharing without a problem.";

        String user =
                "Return JSON using this schema:\n" + schema + "\n\n" +
                "Comment:\n" + text;

        return Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", system),
                        Map.of("role", "user", "content", user)
                ),
                "max_tokens", 400,
                "temperature", 0,
                "stream", false
        );
    }

    private Map<String, Object> buildRepairBody(String previousRawResponse) {
        String system =
                "You fix outputs into strict JSON. Output ONLY one JSON object. " +
                "No other text. Start with '{' end with '}'. Do NOT wrap JSON in quotes.";

        String user =
                "Convert the following text into ONE valid JSON object with this schema.\n" +
                "summary = short summary of the issue, max 2 sentences.\n" +
                "{\"isTicket\":true|false,\"title\":\"...\",\"category\":\"bug|feature|billing|account|other\",\"priority\":\"low|medium|high\",\"summary\":\"...\"}\n\n" +
                "TEXT TO CONVERT:\n" +
                previousRawResponse;

        return Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", system),
                        Map.of("role", "user", "content", user)
                ),
                "max_tokens", 400,
                "temperature", 0,
                "stream", false
        );
    }

    // --- HTTP call ---

    private String callChat(Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(CHAT_URL, request, String.class);
        return response.getBody();
    }

    // --- Parsing ---

    private AiTriageResult tryParseAiResult(String rawResponse) {
        try {
            JsonNode root = mapper.readTree(rawResponse);
            JsonNode choices = root.get("choices");
            if (choices == null || !choices.isArray() || choices.isEmpty()) return null;

            JsonNode message = choices.get(0).get("message");
            if (message == null || message.get("content") == null) return null;

            String content = message.get("content").asText("");
            content = stripThinkTags(content);
            String json = extractJsonObject(content);
            if (json == null) return null;

            JsonNode jsonNode = mapper.readTree(json);

            if (!jsonNode.hasNonNull("isTicket")) return null;

            AiTriageResult r = new AiTriageResult();
            r.setIsTicket(jsonNode.get("isTicket").asBoolean(false));
            r.setTitle(asText(jsonNode, "title"));
            r.setCategory(asText(jsonNode, "category"));
            r.setPriority(asText(jsonNode, "priority"));
            r.setSummary(asText(jsonNode, "summary"));

            r.setCategory(normalizeCategory(r.getCategory()));
            r.setPriority(normalizePriority(r.getPriority()));

            return r;
        } catch (Exception e) {
            return null; 
        }
    }

    private String asText(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v == null ? "" : v.asText("");
    }

    private AiTriageResult applyRules(String originalText, AiTriageResult ai) {
        if (originalText == null || originalText.isBlank() || ai == null) {
            return ai;
        }

        String t = originalText.toLowerCase();

        boolean bugSignal =
                t.contains("crash") ||
                t.contains("error") ||
                t.contains("exception") ||
                t.contains("not working") ||
                t.contains("doesn't work") ||
                t.contains("doesnt work") ||
                t.contains("does not work") ||
                t.contains("won't work") ||
                t.contains("wont work") ||
                t.contains("bug");

        if (bugSignal) {
            ai.setIsTicket(true);

            if (ai.getCategory() == null || ai.getCategory().isBlank()) {
                ai.setCategory("bug");
            }
            if (ai.getPriority() == null || ai.getPriority().isBlank()) {
                ai.setPriority("high");
            }
            if (ai.getTitle() == null || ai.getTitle().isBlank()) {
                // Very simple title – galima patobulinti vėliau
                ai.setTitle("User reports crash / error");
            }
        }

        return ai;
    }

    private String stripThinkTags(String content) {
        if (content == null) return null;
        String t = content.trim();
        int open = t.indexOf("<think>");
        if (open < 0) return t;
        int close = t.indexOf("</think>", open);
        if (close < 0) return t;
        String after = t.substring(close + 8).trim();
        return after.isEmpty() ? t : after;
    }

    private String extractJsonObject(String content) throws Exception {
        if (content == null) return null;
        String t = content.trim();

        if (t.startsWith("\"") && t.endsWith("\"")) {
            JsonNode maybeTextNode = mapper.readTree(t);
            if (maybeTextNode.isTextual()) {
                t = maybeTextNode.asText("").trim();
            }
        }

        if (t.startsWith("{") && t.endsWith("}")) {
            return t;
        }

        int start = t.indexOf('{');
        int end = t.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return t.substring(start, end + 1).trim();
        }

        return null;
    }

    private String normalizeCategory(String c) {
        String t = (c == null ? "" : c.trim().toLowerCase());
        return switch (t) {
            case "bug", "feature", "billing", "account", "other" -> t;
            default -> "other";
        };
    }

    private String normalizePriority(String p) {
        String t = (p == null ? "" : p.trim().toLowerCase());
        return switch (t) {
            case "low", "medium", "high" -> t;
            default -> "medium";
        };
    }
}