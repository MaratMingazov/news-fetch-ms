package maratmingazov.news.fetch.model.neo4j;

import lombok.NonNull;
import lombok.Value;

@Value
public class Word {

    @NonNull
    String value;

    @NonNull
    Long count;

    public Word(@NonNull String value, @NonNull Long count) {
        this.value = value;
        this.count = count;
    }
}
