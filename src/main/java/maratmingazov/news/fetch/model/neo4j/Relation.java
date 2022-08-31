package maratmingazov.news.fetch.model.neo4j;

import lombok.NonNull;
import lombok.Value;

@Value
public class Relation {

    @NonNull
    Word firstWord;

    @NonNull
    Word secondWord;

    @NonNull
    Long count;

    public Relation(@NonNull Word firstWord,
                    @NonNull Word secondWord,
                    @NonNull Long count) {
        this.firstWord = firstWord;
        this.secondWord = secondWord;
        this.count = count;
    }
}
