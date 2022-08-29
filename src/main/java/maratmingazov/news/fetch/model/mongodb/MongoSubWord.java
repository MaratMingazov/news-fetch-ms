package maratmingazov.news.fetch.model.mongodb;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class MongoSubWord {

    @NonNull
    @Field("value")
    String value;

    @NonNull
    @Field("count")
    Long count;

    @NonNull
    @Field("prev")
    List<MongoSubWord> subWords;
}
