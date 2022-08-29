package maratmingazov.news.fetch.model.mongodb;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;

@Value
@AllArgsConstructor
public class MongoQuintet {

    @NonNull
    String word_1;

    @NonNull
    String word_2;

    @NonNull
    String word_3;

    @NonNull
    String word_4;

    @NonNull
    String word_5;

}
