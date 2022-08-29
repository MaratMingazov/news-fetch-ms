package maratmingazov.news.fetch.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import maratmingazov.news.fetch.model.google.GoogleNewsArticle;
import maratmingazov.news.fetch.model.mongodb.MongoArticle;
import maratmingazov.news.fetch.model.mongodb.MongoQuintet;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class MongoServiceImpl implements MongoService {

    private final MongoTemplate mongoTemplate;

    @NonNull
    @Override
    public List<MongoArticle> saveArticles(@NonNull List<GoogleNewsArticle> articles) {

        List<MongoArticle> mongoArticles = new ArrayList<>();

        for (GoogleNewsArticle article : articles) {
            if(article.getTitle() == null || article.getPublishedAt() == null) {
                continue;
            }
            Query query = new Query();
            query.addCriteria(
                    new Criteria().andOperator(
                            Criteria.where("title").is(article.getTitle()),
                            Criteria.where("publishedAt").is(article.getPublishedAt())
                    )
            );
            val existingDocument = mongoTemplate.findOne(query, MongoArticle.class);
            if(existingDocument == null) {
                val mongoArticle = MongoArticle.builder()
                        .title(article.getTitle())
                        .description(article.getDescription())
                        .url(article.getUrl())
                        .publishedAt(article.getPublishedAt())
                        .build();
                val savedArticle = mongoTemplate.save(mongoArticle);
                mongoArticles.add(savedArticle);
            }
        }

        return mongoArticles;
    }

    @NonNull
    @Override
    public List<MongoQuintet> calculateQuintets(@NonNull List<MongoArticle> articles) {
        List<MongoQuintet> quintets = new ArrayList<>();

        for (MongoArticle article : articles) {
            quintets.addAll(calculateQuintets(article.getTitle()));
            quintets.addAll(calculateQuintets(article.getDescription()));
        }
        
        return quintets;
    }

    private List<MongoQuintet> calculateQuintets(@Nullable String sentence) {
        List<MongoQuintet> quintets = new ArrayList<>();

        if (sentence == null || sentence.isBlank()) {
            return quintets;
        }

        val words = Arrays.stream(sentence.split( " ")).collect(Collectors.toList());
        if (words.size() < 4) {
            return quintets;
        }

        if ("-".equals(words.get(words.size()-2))) {
            words.remove(words.size()-2);
            words.remove(words.size()-1);
        }

        if (words.size() < 4) {
            return quintets;
        }

        int middleIndex = 2;
        while(middleIndex + 2 < words.size() ) {
            val word_0 = words.get(middleIndex - 2);
            val word_1 = words.get(middleIndex - 1);
            val word_2 = words.get(middleIndex);
            val word_3 = words.get(middleIndex + 1);
            val word_4 = words.get(middleIndex + 2);

            quintets.add(new MongoQuintet(word_0, word_1, word_2, word_3, word_4));
            middleIndex++;
        }

        return quintets;
    }



    @Scheduled(fixedDelay = 60000)
    private void deleteOldArticles() {
        LocalDateTime now = LocalDateTime.now().minusDays(1);
        Query query = new Query();
        query.addCriteria(Criteria.where("publishedAt").lt(now));
        val removedArticles = mongoTemplate.findAllAndRemove(query, MongoArticle.class);
        log.info("MongoServiceImpl: successfully removed old articles={}", removedArticles.size());
    }
}
