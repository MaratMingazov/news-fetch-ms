package maratmingazov.news.fetch.model.neo4j;

import lombok.NonNull;
import lombok.Value;


@Value
public class NeoRelation {

    @NonNull
    String first;

    @NonNull
    String second;

    @NonNull
    Long count;

    public NeoRelation(@NonNull String first,
                       @NonNull String second,
                       @NonNull Long count) {
        this.first = first;
        this.second = second;
        this.count = count;
    }
}
