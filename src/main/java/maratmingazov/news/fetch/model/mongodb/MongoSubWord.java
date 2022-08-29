package maratmingazov.news.fetch.model.mongodb;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.mongodb.core.mapping.Field;

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

    @Nullable
    @Field("prev")
    MongoSubWord prev;

    @Nullable
    @Field("next")
    MongoSubWord next;
}
