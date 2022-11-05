package maratmingazov.news.fetch.service.html;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.TextNode;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2

@Service
@RequiredArgsConstructor
public class HtmlParseService {

    /**
     * The method trying to parse the list of sentences in html of given url
     * @param url given url to parse html document
     * @return list of sentences from given url
     */
    public List<String> getSentencesFromHtml(@NonNull String url) {
        Document doc;
        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            log.error("HtmlParseServiceError: html parse exception e={}, url={}", e.getMessage(), url);
            return List.of();
        }

        val elements = doc.select("p");
        return elements.stream()
                .flatMap(element -> element.childNodes().stream())
                .filter(childNode -> childNode instanceof TextNode)
                .map(childNode -> (TextNode) childNode)
                .map(TextNode::text)
                .collect(Collectors.toList());
    }
}
