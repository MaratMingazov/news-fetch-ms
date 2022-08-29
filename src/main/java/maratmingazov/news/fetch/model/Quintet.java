package maratmingazov.news.fetch.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class Quintet {

    @NonNull
    String word1;

    @NonNull
    String word2;

    @NonNull
    String word3;

    @NonNull
    String word4;

    @NonNull
    String word5;

}
