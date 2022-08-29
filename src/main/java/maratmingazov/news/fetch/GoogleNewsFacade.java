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

@Log4j2
@Service
@RequiredArgsConstructor
public class GoogleNewsFacade {

    private final GoogleNewsApi googleNewsApi;
    private final ObjectMapper objectMapper;
    private final MongoService mongoService;


    @Scheduled(fixedDelay = 60000)
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
            val response = objectMapper.readValue(newsResponseJson, GoogleNewsResponse.class);
            val articles = response.getArticles();
            val savedArticles = mongoService.saveArticles(articles);

            log.info("GoogleNewsFacade: successfully fetched news: articles={}, savedArticles={}", articles.size(), savedArticles.size());
        } catch (JsonProcessingException e) {
            log.error("GoogleNewsFacade: json parsing exception e={}, json={}", e.getMessage(), newsResponseJson);
        }
    }


    private void handleErrorResponse(@NonNull Throwable response) {
        log.error("GoogleNewsFacade: news fetch error={}", response.getMessage());
    }



    //
//    private void analyzeGoogleNewsResponse(GoogleNewsResponse response) {
//        val articles = response.getArticles();
//        articles.forEach(this::analyzeArticle);
//    }
//
//    private void analyzeArticle(GoogleNewsArticle article) {
//        val title = article.getTitle();
//        System.err.println("title= " + title);
//        if (title == null) {
//            return;
//        }
//        val words = Arrays.stream(title.split( " ")).collect(Collectors.toList());
//
//        if(words.size() < 4) {
//            return;
//        }
//
//        int middleIndex = 2;
//        while(middleIndex + 2 < words.size() ) {
//            val word_0 = words.get(middleIndex - 2);
//            val word_1 = words.get(middleIndex - 1);
//            val word_2 = words.get(middleIndex);
//            val word_3 = words.get(middleIndex + 1);
//            val word_4 = words.get(middleIndex + 2);
//
//            System.err.println(word_0 + "|" + word_1 + "|" + word_2 + "|" + word_3 + "|" + word_4);
//            middleIndex++;
//        }
//    }
}
