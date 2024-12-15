package org.mql.ws.sentimentanalyze.api.business;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.mql.ws.sentimentanalyze.api.models.AnalysisResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class ApiServerDefault implements ApiServer {
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();


    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    @Override
    public AnalysisResult analyzeSentiment(String text) {
        try {
            MediaType mediaType = MediaType.parse("application/json");
            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "contents", List.of(Map.of(
                            "parts", List.of(Map.of(
                                    "text", "Analyze the sentiment of this text as positive, negative or neutral: " + text))
                    ))
            ));

            RequestBody body = RequestBody.create(requestBody, mediaType);
            Request request = new Request.Builder()
                    .url(apiUrl + "?key=" + apiKey)
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Gemini API error: " + response);
                }
                if (response.body() == null) {
                    throw new IOException("Gemini API response body was empty");
                }

                String responseBody = response.body().string();

                JsonNode root = objectMapper.readTree(responseBody);
                JsonNode candidates = root.get("candidates");

                if(candidates == null || !candidates.isArray() || candidates.isEmpty()){
                    throw new IOException("Gemini API Response body was malformed, no candidates object found");
                }

                JsonNode firstCandidate = candidates.get(0);

                if(firstCandidate == null){
                    throw new IOException("Gemini API Response body was malformed, no candidates object was empty");
                }
                JsonNode content = firstCandidate.get("content");

                if(content == null){
                    throw new IOException("Gemini API Response body was malformed, no content object");
                }
                JsonNode parts = content.get("parts");
                if(parts == null || !parts.isArray() || parts.isEmpty()){
                    throw new IOException("Gemini API Response body was malformed, no parts array");
                }
                JsonNode firstPart = parts.get(0);

                if(firstPart == null){
                    throw new IOException("Gemini API Response body was malformed, no parts array was empty");
                }


                String geminiResponseText = firstPart.get("text").asText().trim();
                System.out.println("Gemeni response text " + geminiResponseText);
                AnalysisResult result = parseGeminiResponse(geminiResponseText);
                return result;
            }
        } catch (IOException e) {
            // Handle API errors or exceptions properly
            System.out.println("Exception thrown by gemini api " + e);
            AnalysisResult errorResult = new AnalysisResult();
            errorResult.setSentiment("ERROR");
            errorResult.setScore(0.0);
            return errorResult;
        }
    }

    private AnalysisResult parseGeminiResponse(String geminiResponseText){
        AnalysisResult result = new AnalysisResult();
        if (geminiResponseText.toLowerCase().contains("positive")) {
            result.setSentiment("POSITIVE");
            result.setScore(1.0);
        } else if (geminiResponseText.toLowerCase().contains("negative")) {
            result.setSentiment("NEGATIVE");
            result.setScore(-1.0);
        } else {
            result.setSentiment("NEUTRAL");
            result.setScore(0.0);
        }
        return result;
    }
}


