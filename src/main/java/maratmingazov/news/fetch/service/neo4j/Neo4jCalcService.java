package maratmingazov.news.fetch.service.neo4j;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import maratmingazov.news.fetch.model.mongodb.MongoArticle;
import maratmingazov.news.fetch.model.neo4j.NeoRelation;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class Neo4jCalcService {

    public Pair<List<String>, List<NeoRelation>> getPairs(@NonNull List<MongoArticle> articles) {

        List<String> words = new ArrayList<>();
        List<NeoRelation> relations = new ArrayList<>();

        for (MongoArticle article : articles) {
            val titleWords = calculateWords(article.getTitle());
            val titleRelations = calculateNeoRelations(titleWords);
            val descriptionWords = calculateWords(article.getDescription());
            val descriptionRelations = calculateNeoRelations(descriptionWords);

            words.addAll(titleWords);
            words.addAll(descriptionWords);
            relations.addAll(titleRelations);
            relations.addAll(descriptionRelations);
        }

        return Pair.of(words, relations);
    }

    @NonNull
    private List<String> calculateWords(@Nullable String sentence) {
        List<String> words = new ArrayList<>();

        if (sentence == null || sentence.isBlank()) {
            return words;
        }

        sentence = sentence.replace(",","");
        sentence = sentence.replace(".","");
        sentence = sentence.replace("|","");
        sentence = sentence.replace("\"","");
        sentence = sentence.replace("?","");
        sentence = sentence.replace("!","");
        sentence = sentence.replace("&","");
        sentence = sentence.replace("'","");
        sentence = sentence.replace(":","");

        words = Arrays.stream(sentence.toLowerCase(Locale.ROOT).split( " ")).collect(Collectors.toList());
        words.removeAll(
                List.of(" ", "-", "", "—", "–",
                        "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "14", "2022",
                        "a", "abc", "about", "after", "all", "an", "and", "are", "as", "at",
                        "bbc", "be", "been", "being", "between", "before", "but", "by",
                        "guardian",
                        "had", "has", "have", "his", "he", "her", "how",
                        "can", "cnn", "could",
                        "first", "for", "from",
                        "i", "is", "in", "into", "it", "its", "if",
                        "just",
                        "last", "latest", "launch", "live",
                        "may", "more",
                        "ndtv", "no", "now", "new", "news", "not",
                        "on", "one", "of", "over", "or", "out",
                        "said", "says", "she",
                        "than", "that", "the", "they", "there", "their", "this", "to", "today", "times",
                        "up", "you", "us",
                        "vs",
                        "was", "we", "were", "what", "when", "where", "which", "who", "will", "with",
                        "в",
                        "на", "не",
                        "и",
                        "по",
                        "с"
                )
        );

        if (words.size() > 2 &&  "-".equals(words.get(words.size()-2))) {
            words.remove(words.size()-2);
            words.remove(words.size()-1);
        }

        return words;
    }

    @NonNull
    List<NeoRelation> calculateNeoRelations(@NonNull List<String> words) {
        List<NeoRelation> relations = new ArrayList<>();
        for (int i = 0; i < words.size() - 1; i++) {
            val relation = new NeoRelation(words.get(i), words.get(i+1), 1L);
            relations.add(relation);
        }
        return  relations;
    }
}
