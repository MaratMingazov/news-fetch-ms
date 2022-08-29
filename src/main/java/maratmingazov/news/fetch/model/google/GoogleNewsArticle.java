package maratmingazov.news.fetch.model.google;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleNewsArticle {

    @Nullable
    @JsonProperty("title")
    String title;

    @Nullable
    @JsonProperty("description")
    String description;

    @Nullable
    @JsonProperty("url")
    String url;

    @Nullable
    @JsonProperty("publishedAt")
    LocalDateTime publishedAt;
}
