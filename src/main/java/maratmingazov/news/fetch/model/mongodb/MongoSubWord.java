package maratmingazov.news.fetch.model.mongodb;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class MongoSubWord implements Comparable<MongoSubWord> {

    @NonNull
    @Field("value")
    String value;

    @NonNull
    @Field("count")
    Long count;

    @NonNull
    @Field("prev")
    List<MongoSubWord> subWords;

    @Override
    public int compareTo(@NotNull MongoSubWord word) {
        if(count.equals(word.count))
            return 0;
        else if(count > word.count)
            return 1;
        else
            return -1;
    }

}
