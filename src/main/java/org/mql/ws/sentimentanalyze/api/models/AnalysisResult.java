package org.mql.ws.sentimentanalyze.api.models;
import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class AnalysisResult {
    private String sentiment;
    private double score;
}