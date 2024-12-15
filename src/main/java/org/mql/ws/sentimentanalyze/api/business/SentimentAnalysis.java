package org.mql.ws.sentimentanalyze.api.business;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;


@Data
public class SentimentAnalysis {
    private final Map<String, Integer> emojiCounts;
    private final Map<String, Map<String, Integer>> sentimentCounts;

    public SentimentAnalysis(Map<String, Integer> emojiCounts,
                             Map<String, Map<String, Integer>> sentimentCounts) {
        this.emojiCounts = emojiCounts;
        this.sentimentCounts = sentimentCounts;
    }

    public String generateReport() {
        StringBuilder report = new StringBuilder();
        report.append("Analyse complète des émojis\n");
        report.append("==========================\n\n");

        int totalEmojis = emojiCounts.values().stream().mapToInt(Integer::intValue).sum();
        report.append("Total des émojis trouvés: ").append(totalEmojis).append("\n\n");

        for (String sentiment : sentimentCounts.keySet()) {
            Map<String, Integer> emojis = sentimentCounts.get(sentiment);
            if (!emojis.isEmpty()) {
                report.append(sentiment).append(":\n");
                emojis.forEach((emoji, count) ->
                        report.append(String.format("  %s : %d fois\n", emoji, count))
                );
                report.append("\n");
            }
        }

        int positiveCount = getTotalCount(sentimentCounts.get("POSITIF"));
        int negativeCount = getTotalCount(sentimentCounts.get("NÉGATIF"));
        double sentimentScore = calculateSentimentScore(positiveCount, negativeCount);

        report.append("Score de sentiment: ").append(String.format("%.2f", sentimentScore))
                .append(" (-1 très négatif, +1 très positif)\n");

        return report.toString();
    }

    private int getTotalCount(Map<String, Integer> counts) {
        return counts.values().stream().mapToInt(Integer::intValue).sum();
    }

    private double calculateSentimentScore(int positive, int negative) {
        int total = positive + negative;
        if (total == 0) return 0.0;
        return (double)(positive - negative) / total;
    }

}
