// CombinedAnalysisService.java
package org.mql.ws.sentimentanalyze.api.business;

import org.mql.ws.sentimentanalyze.api.models.AnalysisResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.vdurmont.emoji.EmojiParser;
import java.util.HashMap;
import java.util.Map;

@Service
public class CombinedAnalysisService {
    private static final Logger logger = LoggerFactory.getLogger(CombinedAnalysisService.class);

    private final ApiServer geminiService;
    private final EmojiSentimentService emojiService;

    @Autowired
    public CombinedAnalysisService(ApiServer geminiService, EmojiSentimentService emojiService) {
        this.geminiService = geminiService;
        this.emojiService = emojiService;
    }

    public Map<String, Object> analyzeCombined(String text) {
        Map<String, Object> result = new HashMap<>();

        try {
            // Get text analysis
            String textWithoutEmojis = EmojiParser.removeAllEmojis(text);
            AnalysisResult textAnalysis = geminiService.analyzeSentiment(textWithoutEmojis);

            // Get emoji analysis
            SentimentAnalysis emojiAnalysis = emojiService.analyzeSentiment(text);

            // Combine results
            result.put("textAnalysis", textAnalysis);
            result.put("emojiAnalysis", Map.of(
                    "emojiCounts", emojiAnalysis.getEmojiCounts(),
                    "sentimentCounts", emojiAnalysis.getSentimentCounts(),
                    "report", emojiAnalysis.generateReport()
            ));

            // Calculate combined sentiment
            String combinedSentiment = calculateCombinedSentiment(textAnalysis, emojiAnalysis);
            result.put("combinedSentiment", combinedSentiment);

            logger.info("Combined analysis completed successfully");
        } catch (Exception e) {
            logger.error("Error in combined analysis", e);
            result.put("error", "Error performing combined analysis: " + e.getMessage());
        }

        return result;
    }

    private String calculateCombinedSentiment(AnalysisResult textAnalysis, SentimentAnalysis emojiAnalysis) {
        // Get text sentiment
        String textSentiment = textAnalysis.getSentiment();

        // Count positive and negative emojis
        Map<String, Map<String, Integer>> emojiSentiments = emojiAnalysis.getSentimentCounts();
        int positiveEmojis = getTotalCount(emojiSentiments.get("POSITIF"));
        int negativeEmojis = getTotalCount(emojiSentiments.get("NÉGATIF"));

        // If there are no emojis, return text sentiment
        if (positiveEmojis == 0 && negativeEmojis == 0) {
            return textSentiment;
        }

        // If there's no text, use emoji sentiment
        if (textSentiment.equals("NEUTRAL") && (positiveEmojis > 0 || negativeEmojis > 0)) {
            return positiveEmojis > negativeEmojis ? "POSITIVE" :
                    negativeEmojis > positiveEmojis ? "NEGATIVE" : "NEUTRAL";
        }

        // Combine both analyses
        boolean isTextPositive = textSentiment.equals("POSITIVE");
        boolean isTextNegative = textSentiment.equals("NEGATIVE");

        if ((isTextPositive && positiveEmojis > negativeEmojis) ||
                (positiveEmojis > 0 && positiveEmojis > negativeEmojis * 2)) {
            return "VERY POSITIVE";
        } else if ((isTextNegative && negativeEmojis > positiveEmojis) ||
                (negativeEmojis > 0 && negativeEmojis > positiveEmojis * 2)) {
            return "VERY NEGATIVE";
        }

        return textSentiment; // Default to text sentiment if no strong indicators
    }

    private int getTotalCount(Map<String, Integer> counts) {
        return counts != null ? counts.values().stream().mapToInt(Integer::intValue).sum() : 0;
    }
}