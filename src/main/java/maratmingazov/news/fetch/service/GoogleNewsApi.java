package maratmingazov.news.fetch.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import maratmingazov.news.fetch.model.GoogleNewsResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Log4j2
@Service
@RequiredArgsConstructor
public class GoogleNewsApi implements NewsApiService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Override
    public void getNews() {

        val uriSpec = webClient.get();
        val headersSpec = uriSpec.uri("");
        val mono = headersSpec.exchangeToMono(response -> {
            if (response.statusCode().equals(HttpStatus.OK)) {
                return response.bodyToMono(String.class);
            } else if (response.statusCode().is4xxClientError()) {
                return Mono.just("Error response");
            } else {
                return response.createException().flatMap(Mono::error);
            }
        });

        mono.subscribe(
                this::handleSuccessResponse,
                this::handleErrorResponse
        );
    }

    private void handleSuccessResponse(@NonNull String responseJson) {
        try {
            val response = objectMapper.readValue(responseJson, GoogleNewsResponse.class);
            log.info("Successfully parsed");

        } catch (JsonProcessingException e) {
            log.error("Exception during parsing json= " + responseJson);
        }
    }

    private void handleErrorResponse(@NonNull Throwable response) {
        log.error(response.getMessage());
    }
}
