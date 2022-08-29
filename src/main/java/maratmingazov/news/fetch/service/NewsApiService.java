package maratmingazov.news.fetch.service;

import lombok.NonNull;
import reactor.core.publisher.Mono;

public interface NewsApiService {

    @NonNull
    Mono<String> getNews();
}
