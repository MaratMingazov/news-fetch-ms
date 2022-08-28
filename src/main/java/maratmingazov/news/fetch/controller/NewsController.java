package maratmingazov.news.fetch.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/news")
public class NewsController {

    @GetMapping()
    public ResponseEntity<String> refreshToken() {
        return ResponseEntity.ok("YES");
    }
}
