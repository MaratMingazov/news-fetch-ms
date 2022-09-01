package maratmingazov.news.fetch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import maratmingazov.news.fetch.model.google.GoogleNewsResponse;
import maratmingazov.news.fetch.service.google.GoogleNewsService;
import maratmingazov.news.fetch.service.mongo.MongoService;
import maratmingazov.news.fetch.service.neo4j.Neo4jCalcService;
import maratmingazov.news.fetch.service.neo4j.Neo4jDBService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Log4j2
@Service
@RequiredArgsConstructor
public class NewsFacade {

    private final GoogleNewsService googleNewsApi;
    private final ObjectMapper objectMapper;
    private final MongoService mongoService;
    private final Neo4jDBService neo4jService;
    private final Neo4jCalcService neo4jCalcService;

    @Value("${google-news.en-url}")
    private String enUrl;

    @Value("${google-news.ru-url}")
    private String ruUrl;


//    @Scheduled(fixedDelay = 3600000) // every hour
    @Scheduled(cron = "0 15 * * * *") // at 15 minute , on every hour
    public void fetchEnNews() {

        val news = googleNewsApi.getNews(enUrl);
        news.subscribe(
                success -> handleSuccessResponse(success, "EN"),
                this::handleErrorResponse
        );
    }

//    @Scheduled(fixedDelay = 3600000) // every hour
    @Scheduled(cron = "0 30 * * * *") // at 30 minute , on every hour
    public void fetchRuNews() {

        val news = googleNewsApi.getNews(ruUrl);
        news.subscribe(
                success -> handleSuccessResponse(success, "RU"),
                this::handleErrorResponse
        );
    }

    private void handleSuccessResponse(@NonNull String newsResponseJson, @NonNull String lang) {
        log.info("GoogleNewsFacade: starting analyze lang={}", lang);
        if(newsResponseJson.isEmpty()) {
            log.error("GoogleNewsFacade: empty newsResponseJson={}", newsResponseJson);
            return;
        }
        try {
            val initial = Instant.now();
            val response = objectMapper.readValue(newsResponseJson, GoogleNewsResponse.class);
            val articles = response.getArticles();
            val savedArticles = mongoService.saveArticles(articles);
            val pairs = neo4jCalcService.getPairs(savedArticles);
            neo4jService.updatePairs(pairs);
            val duration = Duration.between(initial, Instant.now());
            log.info("GoogleNewsFacade: successfully fetched news: articles={}, savedArticles={}, words={}, relations={}, duration={}", articles.size(), savedArticles.size(), pairs.getLeft().size(), pairs.getRight().size(), duration);
        } catch (JsonProcessingException e) {
            log.error("GoogleNewsFacade: json parsing exception e={}, json={}", e.getMessage(), newsResponseJson);
        }
    }


    private void handleErrorResponse(@NonNull Throwable response) {
        log.error("GoogleNewsFacade: news fetch error={}", response.getMessage());
    }

}
