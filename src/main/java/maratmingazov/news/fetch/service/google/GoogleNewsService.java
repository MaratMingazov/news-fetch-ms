package maratmingazov.news.fetch.service.google;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Log4j2
@Service
@RequiredArgsConstructor
public class GoogleNewsService {

    private final WebClient webClient;

    @NonNull
    public Mono<String> getNews(@NonNull String url) {

        val uriSpec = webClient.get();
        val headersSpec = uriSpec.uri(url);

        return headersSpec.exchangeToMono(response -> {
            if (response.statusCode().equals(HttpStatus.OK)) {
                return response.bodyToMono(String.class);
            } else if (response.statusCode().is4xxClientError()) {
                return Mono.just("");
            } else {
                return response.createException().flatMap(Mono::error);
            }
        });
    }
}
