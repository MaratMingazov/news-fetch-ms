package maratmingazov.news.fetch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableScheduling
public class WebConfiguration {

    @Bean
    WebClient webClient() {
        return WebClient.create();
    }
}
