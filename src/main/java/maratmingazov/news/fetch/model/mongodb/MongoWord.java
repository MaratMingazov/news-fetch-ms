package maratmingazov.news.fetch.model.mongodb;

import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "word")
public class MongoWord implements Comparable<MongoWord> {

    @Id
    String id;

    @NonNull
    @Field("value")
    String value;

    @NonNull
    @Field("count")
    Long count;

    @NonNull
    @Field("prev")
    List<MongoSubWord> prev = new ArrayList<>();

    @NonNull
    @Field("next")
    List<MongoSubWord> next = new ArrayList<>();

    public MongoWord(@NonNull MongoQuintet quintet) {

        val word_0 = MongoSubWord.builder().value(quintet.getWord_0()).count(1L).subWords(List.of()).build();
        val word_4 = MongoSubWord.builder().value(quintet.getWord_4()).count(1L).subWords(List.of()).build();
        val word_1 = MongoSubWord.builder().value(quintet.getWord_1()).count(1L).subWords(List.of(word_0)).build();
        val word_3 = MongoSubWord.builder().value(quintet.getWord_3()).count(1L).subWords(List.of(word_4)).build();

        this.value = quintet.getWord_2();
        this.count = 1L;
        this.prev = List.of(word_1);
        this.next = List.of(word_3);
    }


    @Override
    public int compareTo(@NotNull MongoWord word) {
        if(count.equals(word.count))
            return 0;
        else if(count > word.count)
            return -1;
        else
            return 1;
    }
}
