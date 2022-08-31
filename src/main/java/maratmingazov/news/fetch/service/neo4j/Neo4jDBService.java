package maratmingazov.news.fetch.service.neo4j;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import maratmingazov.news.fetch.model.neo4j.NeoRelation;
import org.apache.commons.lang3.tuple.Pair;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.TransactionWork;
import org.neo4j.driver.exceptions.Neo4jException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class Neo4jDBService implements CommandLineRunner {

    private final Driver driver;

    public void updatePairs(@NonNull Pair<List<String>, @NonNull List<NeoRelation>> pair) {
        try (Session session = driver.session(SessionConfig.forDatabase("neo4j"))) {
            updateWords(pair.getLeft(), session);
            updateRelations(pair.getRight(), session);
        } catch (Exception e) {
            log.error("Neo4JService.updatePairs: exception={}", e.getMessage());
            throw e;
        }
    }

    @Scheduled(cron = "0 0 03 * * *") // at 03:00 UTC, on every day
    public void deleteZeroWordsAndRelations() {
        try (Session session = driver.session(SessionConfig.forDatabase("neo4j"))) {
            val zeroWords = session.readTransaction(executeQuery("MATCH (w:Word)\n WHERE w.count < 1\n RETURN w"));
            val zeroRelations = session.readTransaction(executeQuery("MATCH (:Word)-[r]->(:Word)\n WHERE r.count < 1\n RETURN  r"));
            session.writeTransaction(executeQuery("MATCH (w:Word)\n WHERE w.count < 1\n DETACH DELETE w"));
            session.writeTransaction(executeQuery("MATCH (:Word)-[r]->(:Word)\n WHERE r.count < 1\n DELETE  r"));
            log.info("Neo4jService.deleteZeroWords: successfully deleted zeroWords={}, zeroRelations={}", zeroWords.size(), zeroRelations.size());
        } catch (Exception e) {
            log.error("Neo4JService.deleteZeroWords: exception={}", e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 05 * * *") // at 05:00 UTC, on every day
    public void decrementWordsAndRelations() {
        try (Session session = driver.session(SessionConfig.forDatabase("neo4j"))) {
            session.writeTransaction(executeQuery("MATCH (w:Word)\n SET w.count = w.count - 1\n RETURN w"));
            session.writeTransaction(executeQuery("MATCH (:Word)-[r]->(:Word)\n SET r.count = r.count - 1\n RETURN  r"));
            log.info("Neo4jService.decrementWords: successfully decrement words and relations");
        } catch (Exception e) {
            log.error("Neo4JService.decrementWords: exception={}", e.getMessage());
        }
    }

    private void updateWords(@NonNull List<String> words, @NonNull Session session) {
        Map<String, Long> wordsMap = words.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        int counter = 0;
        Map<String, Long> batchOfWords = new HashMap<>();
        for (Map.Entry<String, Long> entry : wordsMap.entrySet()) {
            counter++;
            if (counter > 200) {
                counter = 0;
                val batchOfWordsQuery = mergeBatchOfWordsQuery(batchOfWords);
                session.writeTransaction(executeQuery(batchOfWordsQuery));
                batchOfWords.clear();
            }
            batchOfWords.put(entry.getKey(), entry.getValue());
        }
        val batchOfWordsQuery = mergeBatchOfWordsQuery(batchOfWords);
        session.writeTransaction(executeQuery(batchOfWordsQuery));
    }

    private void updateRelations(@NonNull List<NeoRelation> relations, @NonNull Session session) {
        int counter = 0;
        List<NeoRelation> batchRelations = new ArrayList<>();
        for (NeoRelation relation : relations) {
            counter++;
            if (counter > 50) {
                counter = 0;
                val batchOfRelationsQuery = mergeBatchOfRelationsQuery(batchRelations);
                session.writeTransaction(executeQuery(batchOfRelationsQuery));
                batchRelations.clear();
            }
            batchRelations.add(relation);
        }
        val batchOfRelationsQuery = mergeBatchOfRelationsQuery(batchRelations);
        session.writeTransaction(executeQuery(batchOfRelationsQuery));
    }

    @NonNull
    private TransactionWork<List<Record>> executeQuery(@NonNull String query) {
        return tx -> {
            try {
                Result result = tx.run(query);
                return result.list();
            } catch (Neo4jException e) {
                log.error("Neo4JService.executeQuery: exception, query={}, e={}", query, e.getMessage());
                return List.of();
            }
        };
    }

    @NonNull
    private String mergeBatchOfWordsQuery(@NonNull Map<String, Long> words) {
        String query = "";
        long increment = 0L;
        for (Map.Entry<String, Long> entry : words.entrySet()) {
            val value = entry.getKey();
            val count = entry.getValue();
            val variable = "w" + increment++;
            query +=
                    "MERGE (" + variable + ":Word {value: \"" + value + "\"})\n" +
                    "ON CREATE SET " + variable + ".count = " + count + "\n" +
                    "ON MATCH SET " + variable + ".count = " + variable + ".count + " + count+"\n"
            ;
        }
        return query;
    }

    @NonNull
    private String mergeBatchOfRelationsQuery(@NonNull List<NeoRelation> relations) {
        String query = "";
        int counter = 0;
        for (NeoRelation relation : relations) {
            val first = relation.getFirst();
            val second = relation.getSecond();
            val count = relation.getCount();

            counter++;
            val f = "f" + counter;
            val s = "s" + counter;

            query += "MATCH (" + f + ":Word {value: \"" + first +"\"})\n" +
                    "MATCH (" + s + ":Word {value: \"" + second +"\"})\n";
        }
        counter = 0;
        for (NeoRelation relation : relations) {
            val count = relation.getCount();

            counter++;
            val f = "f" + counter;
            val s = "s" + counter;
            val r = "r" + counter;

            query += "MERGE (" + f + ")-[" + r + ":NEXT]->(" + s + ")\n" +
                    "ON CREATE SET " + r + ".count = 1\n" +
                    "ON MATCH SET " + r + ".count = " + r + ".count +" + count +"\n";
        }
        return query;
    }


    @Override
    public void run(String... args) {}

}
