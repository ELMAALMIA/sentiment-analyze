package org.mql.ws.sentimentanalyze.api.business;

import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.EmojiParser;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmojiSentimentService {
    private static final Logger logger = LoggerFactory.getLogger(EmojiSentimentService.class);
    private Map<String, String> emojiSentiments;

    public EmojiSentimentService() {
        this.emojiSentiments = new HashMap<>();
    }

    @PostConstruct
    public void init() {
        try {
            this.emojiSentiments = initializeEmojiSentiments();
            logger.info("EmojiSentimentService initialized successfully with {} emojis", emojiSentiments.size());
            emojiSentiments.entrySet().stream()
                    .limit(5)
                    .forEach(entry -> logger.info("Sample emoji mapping: {} -> {}",
                            entry.getKey(), entry.getValue()));
        } catch (Exception e) {
            logger.error("Failed to initialize EmojiSentimentService", e);
            this.emojiSentiments = new HashMap<>();
        }
    }

    private Map<String, String> initializeEmojiSentiments() {
        Map<String, String> sentiments = new HashMap<>();
        try {
            Collection<Emoji> allEmojis = EmojiManager.getAll();
            logger.info("Initializing sentiments for {} emojis", allEmojis.size());

            for (Emoji emoji : allEmojis) {
                String sentiment = determineEmojiSentiment(emoji);
                String unicode = emoji.getUnicode();
                sentiments.put(unicode, sentiment);
                logger.debug("Initialized emoji {} ({}) with sentiment {}",
                        emoji.getAliases(), unicode, sentiment);
            }
        } catch (Exception e) {
            logger.error("Error during emoji sentiment initialization", e);
        }
        return sentiments;
    }
//
//    private String determineEmojiSentiment(Emoji emoji) {
//        if (emoji == null) {
//            return "NEUTRE";
//        }
//
//        List<String> tags = new ArrayList<>(emoji.getTags());
//        String description = emoji.getDescription() != null ? emoji.getDescription().toLowerCase() : "";
//
//        logger.debug("Analyzing emoji: {} with tags: {} and description: {}",
//                emoji.getUnicode(), tags, description);
//
//        Set<String> positiveKeywords = Set.of(
//                "smile", "love", "joy", "happy", "heart", "celebration",
//                "party", "win", "victory", "success", "laugh", "sun", "star",
//                "gift", "trophy", "medal", "thumbsup", "yes", "ok"
//        );
//
//        Set<String> negativeKeywords = Set.of(
//                "angry", "sad", "cry", "tears", "worried", "fear", "scared",
//                "upset", "rage", "hate", "thumbsdown", "no", "broken",
//                "sick", "devil", "curse", "dead", "skull"
//        );
//
//        boolean hasPositive = tags.stream().anyMatch(positiveKeywords::contains) ||
//                positiveKeywords.stream().anyMatch(description::contains);
//
//        boolean hasNegative = tags.stream().anyMatch(negativeKeywords::contains) ||
//                negativeKeywords.stream().anyMatch(description::contains);
//
//        String sentiment;
//        if (hasPositive && !hasNegative) sentiment = "POSITIF";
//        else if (hasNegative && !hasPositive) sentiment = "NÉGATIF";
//        else if (hasPositive && hasNegative) sentiment = "AMBIGU";
//        else sentiment = "NEUTRE";
//
//        logger.debug("Determined sentiment {} for emoji {} (positive: {}, negative: {})",
//                sentiment, emoji.getUnicode(), hasPositive, hasNegative);
//
//        return sentiment;
//    }


    private String determineEmojiSentiment(Emoji emoji) {
        if (emoji == null) {
            return "NEUTRE";
        }

        List<String> tags = emoji.getTags();
        List<String> aliases = emoji.getAliases();
        String description = emoji.getDescription() != null ? emoji.getDescription().toLowerCase() : "";

        // Enhanced positive keywords
        Set<String> positiveKeywords = Set.of(
                "smile", "love", "joy", "happy", "heart", "celebration",
                "party", "win", "victory", "success", "laugh", "sun", "star",
                "gift", "trophy", "medal", "thumbsup", "yes", "ok", "wave",
                "hello", "welcome", "hand", "greet", "friendly", "hug", "kiss",
                "grin", "wink", "celebrate", "sparkles", "rainbow", "music",
                "dance", "clap", "tada", "cool", "awesome", "perfect", "good"
        );

        // Enhanced negative keywords
        Set<String> negativeKeywords = Set.of(
                "angry", "sad", "cry", "tears", "worried", "fear", "scared",
                "upset", "rage", "hate", "thumbsdown", "no", "broken",
                "sick", "devil", "curse", "dead", "skull", "disappointed",
                "frustrated", "mad", "annoyed", "hurt", "heartbreak", "pain",
                "tired", "exhausted", "confused", "wrong", "bad", "terrible"
        );

        // Check tags, aliases, and description
        boolean hasPositive =
                tags.stream().anyMatch(positiveKeywords::contains) ||
                        aliases.stream().anyMatch(alias -> positiveKeywords.stream().anyMatch(alias::contains)) ||
                        positiveKeywords.stream().anyMatch(description::contains);

        boolean hasNegative =
                tags.stream().anyMatch(negativeKeywords::contains) ||
                        aliases.stream().anyMatch(alias -> negativeKeywords.stream().anyMatch(alias::contains)) ||
                        negativeKeywords.stream().anyMatch(description::contains);

        logger.debug("Emoji analysis - Description: {}, Tags: {}, Aliases: {}",
                description, tags, aliases);
        logger.debug("Sentiment detection - Positive: {}, Negative: {}",
                hasPositive, hasNegative);

        // Special cases for common emojis
        if (aliases.contains("wave") || aliases.contains("raised_hand") ||
                description.contains("waving") || description.contains("hello")) {
            return "POSITIF";
        }

        if (hasPositive && !hasNegative) return "POSITIF";
        if (hasNegative && !hasPositive) return "NÉGATIF";
        if (hasPositive && hasNegative) return "AMBIGU";
        return "NEUTRE";
    }
    public SentimentAnalysis analyzeSentiment(String text) {
        if (text == null) {
            logger.info("Received null text");
            return createEmptySentimentAnalysis();
        }

        try {
            logger.info("Analyzing text: {}", text);

            // Debug text encoding
            logger.info("Text bytes: {}",
                    text.chars()
                            .mapToObj(ch -> String.format("\\u%04x", ch))
                            .collect(Collectors.joining()));

            List<String> emojis = EmojiParser.extractEmojis(text);
            logger.info("Extracted {} emojis: {}", emojis.size(), emojis);

            // Additional emoji detection check
            for (int i = 0; i < text.length(); i++) {
                String character = text.substring(i, Math.min(i + 2, text.length()));
                if (EmojiManager.isEmoji(character)) {
                    logger.info("Found emoji at position {}: {}", i, character);
                }
            }

            Map<String, Integer> emojiCounts = new HashMap<>();
            Map<String, Map<String, Integer>> sentimentCounts = initializeSentimentCountsMap();

            for (String emoji : emojis) {
                logger.info("Processing emoji: {} (hex: {})", emoji,
                        emoji.chars().mapToObj(ch -> String.format("\\u%04x", ch))
                                .collect(Collectors.joining()));

                emojiCounts.merge(emoji, 1, Integer::sum);
                String sentiment = emojiSentiments.getOrDefault(emoji, "NEUTRE");
                logger.info("Found sentiment {} for emoji {}", sentiment, emoji);
                sentimentCounts.get(sentiment).merge(emoji, 1, Integer::sum);
            }

            logger.info("Analysis complete - Emoji counts: {}, Sentiment counts: {}",
                    emojiCounts, sentimentCounts);

            return new SentimentAnalysis(emojiCounts, sentimentCounts);
        } catch (Exception e) {
            logger.error("Error analyzing sentiment for text: " + text, e);
            return createEmptySentimentAnalysis();
        }
    }

    private Map<String, Map<String, Integer>> initializeSentimentCountsMap() {
        Map<String, Map<String, Integer>> sentimentCounts = new HashMap<>();
        sentimentCounts.put("POSITIF", new HashMap<>());
        sentimentCounts.put("NÉGATIF", new HashMap<>());
        sentimentCounts.put("NEUTRE", new HashMap<>());
        sentimentCounts.put("AMBIGU", new HashMap<>());
        return sentimentCounts;
    }

    private SentimentAnalysis createEmptySentimentAnalysis() {
        return new SentimentAnalysis(
                new HashMap<>(),
                initializeSentimentCountsMap()
        );
    }
}