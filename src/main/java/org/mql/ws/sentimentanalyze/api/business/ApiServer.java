package org.mql.ws.sentimentanalyze.api.business;

import org.mql.ws.sentimentanalyze.api.models.AnalysisResult;
import org.mql.ws.sentimentanalyze.api.models.Comment;

public interface ApiServer {
    public AnalysisResult analyzeSentiment(String text);
}
