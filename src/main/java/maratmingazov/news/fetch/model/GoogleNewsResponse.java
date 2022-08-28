package maratmingazov.news.fetch.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleNewsResponse {

    @JsonProperty("articles")
    List<GoogleNewsArticle> articles;
}
