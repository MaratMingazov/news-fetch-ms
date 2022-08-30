package maratmingazov.news.fetch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import maratmingazov.news.fetch.model.google.GoogleNewsResponse;
import maratmingazov.news.fetch.service.GoogleNewsApi;
import maratmingazov.news.fetch.service.MongoService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Log4j2
@Service
@RequiredArgsConstructor
public class GoogleNewsFacade {

    private final GoogleNewsApi googleNewsApi;
    private final ObjectMapper objectMapper;
    private final MongoService mongoService;


    @Scheduled(fixedDelay = 3600000) // every hour
    public void fetchNews() {

        val news = googleNewsApi.getNews();
        news.subscribe(
                this::handleSuccessResponse,
                this::handleErrorResponse
        );

    }

    private void handleSuccessResponse(@NonNull String newsResponseJson) {
        if(newsResponseJson.isEmpty()) {
            log.error("GoogleNewsFacade: empty newsResponseJson={}", newsResponseJson);
            return;
        }
        try {
            val initial = Instant.now();
            val response = objectMapper.readValue(newsResponseJson, GoogleNewsResponse.class);
            val articles = response.getArticles();
            val savedArticles = mongoService.saveArticles(articles);
            val quintets = mongoService.calculateQuintets(savedArticles);
            val incrementedWords = mongoService.saveQuintets(quintets);
            val duration = Duration.between(initial, Instant.now());

            log.info("GoogleNewsFacade: successfully fetched news: articles={}, savedArticles={}, quintets={}, incrementedWords={}, duration={}", articles.size(), savedArticles.size(), quintets.size(), incrementedWords, duration);
        } catch (JsonProcessingException e) {
            log.error("GoogleNewsFacade: json parsing exception e={}, json={}", e.getMessage(), newsResponseJson);
        }
    }


    private void handleErrorResponse(@NonNull Throwable response) {
        log.error("GoogleNewsFacade: news fetch error={}", response.getMessage());
    }




}
