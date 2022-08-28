package maratmingazov.news.fetch.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import maratmingazov.news.fetch.service.NewsApiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/news")
public class NewsController {

    private final NewsApiService newsApiService;

    @GetMapping()
    public ResponseEntity<String> refreshToken() {
        newsApiService.getNews();
        return ResponseEntity.ok("YES");
    }
}
