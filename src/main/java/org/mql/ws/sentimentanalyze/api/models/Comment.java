package org.mql.ws.sentimentanalyze.api.models;
import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class Comment {
    private String text;
}
