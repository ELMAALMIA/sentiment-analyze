
// ApiController.java
package org.mql.ws.sentimentanalyze.api.controller;

import org.mql.ws.sentimentanalyze.api.business.ApiServer;
import org.mql.ws.sentimentanalyze.api.business.CombinedAnalysisService;
import org.mql.ws.sentimentanalyze.api.business.EmojiSentimentService;
import org.mql.ws.sentimentanalyze.api.business.SentimentAnalysis;
import org.mql.ws.sentimentanalyze.api.models.AnalysisResult;
import org.mql.ws.sentimentanalyze.api.models.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final ApiServer geminiService;
    private final EmojiSentimentService emojiService;

    @Autowired
    public ApiController(ApiServer geminiService, EmojiSentimentService emojiService) {
        this.geminiService = geminiService;
        this.emojiService = emojiService;
    }

    @Autowired
    private CombinedAnalysisService combinedService;

    @PostMapping("/analyze/combined")
    public ResponseEntity<?> analyzeCombined(@RequestBody Comment comment) {
        try {
            Map<String, Object> result = combinedService.analyzeCombined(comment.getText());
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error in combined analysis: " + e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/analyze")
    public ResponseEntity<AnalysisResult> analyzeComment(@RequestBody Comment comment) {
        AnalysisResult result = geminiService.analyzeSentiment(comment.getText());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return new ResponseEntity<>("API is healthy", HttpStatus.OK);
    }

    @PostMapping("/analyze/emoji")
    public ResponseEntity<?> analyzeEmojis(@RequestBody Comment comment) {
        try {
            SentimentAnalysis analysis = emojiService.analyzeSentiment(comment.getText());
            Map<String, Object> response = new HashMap<>();
            response.put("emojiCounts", analysis.getEmojiCounts());
            response.put("sentimentCounts", analysis.getSentimentCounts());
            response.put("report", analysis.generateReport());

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error analyzing emojis: " + e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
