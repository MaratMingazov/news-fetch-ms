package maratmingazov.news.fetch.model.mongodb;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
@Document(collection = "article")
public class MongoArticle {

    @Id
    String id;

    @Field("title")
    String title;

    @Field("description")
    String description;

    @Field("url")
    String url;

    @Field("publishedAt")
    LocalDateTime publishedAt;

}
