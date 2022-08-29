package maratmingazov.news.fetch.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import maratmingazov.news.fetch.model.google.GoogleNewsArticle;
import maratmingazov.news.fetch.model.mongodb.MongoArticle;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Scheduled(fixedDelay = 60000)
    private void deleteOldArticles() {
        LocalDateTime now = LocalDateTime.now().minusDays(1);
        Query query = new Query();
        query.addCriteria(Criteria.where("publishedAt").lt(now));
        val removedArticles = mongoTemplate.findAllAndRemove(query, MongoArticle.class);
        log.info("MongoServiceImpl: successfully removed old articles={}", removedArticles.size());
    }
}
