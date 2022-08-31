package maratmingazov.news.fetch.service.neo4j;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.neo4j.driver.Record;
import org.neo4j.driver.*;
import org.neo4j.driver.exceptions.Neo4jException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class Neo4jService implements CommandLineRunner {

    private final Driver driver;

    public void updateWords(@NonNull List<String> words) {
        try (Session session = driver.session(SessionConfig.forDatabase("neo4j"))) {
            Map<String, Long> wordsMap = words.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
            session.writeTransaction(updateWords(wordsMap));
            log.info("Neo4jService.UpdateWords: successfully updated words={}", wordsMap.keySet().size());
        } catch (Exception e) {
            log.error("Neo4JService.getAllWords: exception={}", e.getMessage());
            throw e;
        }
    }

    private TransactionWork<List<Record>> updateWords(Map<String, Long> words) {
        return tx -> {
            StringBuilder queryBuilder = new StringBuilder();
            words.forEach((value, count) -> {
                queryBuilder
                        .append("MERGE (w:Word {value: ")
                        .append(value)
                        .append("})\n")
                        .append("ON CREATE SET w.count = 1\n")
                        .append("ON MATCH SET w.count = w.count+")
                        .append(count)
                        .append("\n");
            });
            try {
                Result result = tx.run(queryBuilder.toString());
                return result.list();
            } catch (Neo4jException e) {
                log.error("Neo4JService.GetAllWordRecords: exception={}", e.getMessage());
                throw e;
            }
        };
    }

    @Override
    public void run(String... args) {}

}
