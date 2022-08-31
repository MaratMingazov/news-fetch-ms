package maratmingazov.news.fetch.service.neo4j;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import maratmingazov.news.fetch.model.mongodb.MongoArticle;
import maratmingazov.news.fetch.model.neo4j.NeoRelation;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.neo4j.driver.Record;
import org.neo4j.driver.*;
import org.neo4j.driver.exceptions.Neo4jException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class Neo4jService implements CommandLineRunner {

    private final Driver driver;

    public Pair<List<String>, List<NeoRelation>> getPairs(@NonNull List<MongoArticle> articles) {

        List<String> words = new ArrayList<>();
        List<NeoRelation> relations = new ArrayList<>();

        for (MongoArticle article : articles) {
            val titleWords = calculateWords(article.getTitle());
            val titleRelations = calculateNeoRelations(titleWords);
            val descriptionWords = calculateWords(article.getDescription());
            val descriptionRelations = calculateNeoRelations(descriptionWords);

            words.addAll(titleWords);
            words.addAll(descriptionWords);
            relations.addAll(titleRelations);
            relations.addAll(descriptionRelations);
        }

        return Pair.of(words, relations);
    }



    @NonNull
    List<NeoRelation> calculateNeoRelations(@NonNull List<String> words) {
        List<NeoRelation> relations = new ArrayList<>();
        for (int i = 0; i < words.size() - 1; i++) {
            val relation = new NeoRelation(words.get(i), words.get(i+1), 1L);
            relations.add(relation);
        }
        return  relations;
    }


    @NonNull
    private List<String> calculateWords(@Nullable String sentence) {
        List<String> words = new ArrayList<>();

        if (sentence == null || sentence.isBlank()) {
            return words;
        }

        sentence = sentence.replace(",","");
        sentence = sentence.replace(".","");
        sentence = sentence.replace("|","");
        sentence = sentence.replace("\"","");
        sentence = sentence.replace("?","");

        words = Arrays.stream(sentence.toLowerCase(Locale.ROOT).split( " ")).collect(Collectors.toList());
        words.removeAll(
                List.of(" ", "-",
                        "a", "an", "and", "as", "at",
                        "be", "by",
                        "has", "have", "his", "her",
                        "for",
                        "is", "in", "its",
                        "on", "of",
                        "that", "the", "to",
                        "was", "will", "with"
                )
        );

        if ("-".equals(words.get(words.size()-2))) {
            words.remove(words.size()-2);
            words.remove(words.size()-1);
        }

        return words;
    }

    public void updatePairs(@NonNull Pair<List<String>, List<NeoRelation>> pair) {
        try (Session session = driver.session(SessionConfig.forDatabase("neo4j"))) {
            Map<String, Long> wordsMap = pair.getLeft().stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

            log.info("START1");
            int counter = 0;
            Map<String, Long> batch = new HashMap<>();
            for (Map.Entry<String, Long> entry : wordsMap.entrySet()) {
                counter++;
                if (counter > 200) {
                    log.info(counter);
                    counter = 0;
                    session.writeTransaction(updateWords(batch));
                    batch.clear();
                }
                batch.put(entry.getKey(), entry.getValue());
            }
            session.writeTransaction(updateWords(batch));

            log.info("START2");
            counter = 0;
            List<NeoRelation> batchRelations = new ArrayList<>();
            val relations = pair.getRight();
            for (NeoRelation relation : relations) {
                counter++;
                if (counter > 50) {
                    log.info(counter);
                    counter = 0;
                    session.writeTransaction(updateRelations(batchRelations));
                    batchRelations.clear();
                }
                batchRelations.add(relation);
            }
            session.writeTransaction(updateRelations(batchRelations));

            log.info("START3");
        } catch (Exception e) {
            log.error("Neo4JService.updatePairs: exception={}", e.getMessage());
            throw e;
        }
    }

    //@Scheduled(fixedDelay = 3600000) // every hour
    public void deleteZeroWords() {
        try (Session session = driver.session(SessionConfig.forDatabase("neo4j"))) {
            val zeroNodes = session.writeTransaction(getZeroNodes());
            val zeroRelations = session.writeTransaction(getZeroRelations());
            session.writeTransaction(deleteZeroNodes());
            session.writeTransaction(deleteZeroRelations());
            log.info("Neo4jService.UpdateWords: successfully deleted zeroWords={}, zeroRelations={}", zeroNodes.size(), zeroRelations.size());
        } catch (Exception e) {
            log.error("Neo4JService.getAllWords: exception={}", e.getMessage());
            throw e;
        }
    }

    //@Scheduled(fixedDelay = 3600000) // every hour
    public void decrementWords() {
        try (Session session = driver.session(SessionConfig.forDatabase("neo4j"))) {
            session.writeTransaction(decrementNodes());
            session.writeTransaction(decrementRelations());
            log.info("Neo4jService.decrementWords: successfully decrement words and relations");
        } catch (Exception e) {
            log.error("Neo4JService.decrementWords: exception={}", e.getMessage());
            throw e;
        }
    }

    @NonNull
    private TransactionWork<List<Record>> updateWords(@NonNull Map<String, Long> words) {
        return tx -> {
//            String query  =
//                    "MERGE (w:Word {value: \"" + value + "\"})\n" +
//                    "ON CREATE SET w.count = " + count + "\n" +
//                    "ON MATCH SET w.count = w.count + " + count+"\n";
            String query = "";
            long increment = 0L;
            for (Map.Entry<String, Long> entry : words.entrySet()) {
                val value = entry.getKey();
                val count = entry.getValue();
                val variable = "w" + increment++;
                query +=
                        "MERGE (" + variable + ":Word {value: \"" + value + "\"})\n" +
                        "ON CREATE SET " + variable + ".count = " + count + "\n" +
                        "ON MATCH SET " + variable + ".count = " + variable + ".count + " + count+"\n";
            }
            try {
                Result result = tx.run(query);
                return result.list();
            } catch (Neo4jException e) {
                log.error("Neo4JService.updateWords: exception={}", e.getMessage());
                throw e;
            }
        };
    }

    @NonNull
    private TransactionWork<List<Record>> deleteZeroNodes() {
        return tx -> {
            String query =
                    "MATCH (w:Word)\n" +
                    "WHERE w.count < 1\n" +
                    "DETACH DELETE w";
            try {
                Result result = tx.run(query);
                return result.list();
            } catch (Neo4jException e) {
                log.error("Neo4JService.deleteZeroNodes: exception={}", e.getMessage());
                throw e;
            }
        };
    }

    @NonNull
    private TransactionWork<List<Record>> deleteZeroRelations() {
        return tx -> {
            String query =
                    "MATCH (:Word)-[r]->(:Word)\n" +
                    "WHERE r.count < 1\n" +
                    "DELETE  r";
            try {
                Result result = tx.run(query);
                return result.list();
            } catch (Neo4jException e) {
                log.error("Neo4JService.deleteZeroRelations: exception={}", e.getMessage());
                throw e;
            }
        };
    }

    @NonNull
    private TransactionWork<List<Record>> getZeroNodes() {
        return tx -> {
            String query =
                    "MATCH (w:Word)\n" +
                    "WHERE w.count < 1\n" +
                    "RETURN w";
            try {
                Result result = tx.run(query);
                return result.list();
            } catch (Neo4jException e) {
                log.error("Neo4JService.getZeroNodes: exception={}", e.getMessage());
                throw e;
            }
        };
    }

    @NonNull
    private TransactionWork<List<Record>> getZeroRelations() {
        return tx -> {
            String query =
                    "MATCH (:Word)-[r]->(:Word)\n" +
                            "WHERE r.count < 1\n" +
                            "RETURN  r";
            try {
                Result result = tx.run(query);
                return result.list();
            } catch (Neo4jException e) {
                log.error("Neo4JService.getZeroNodes: exception={}", e.getMessage());
                throw e;
            }
        };
    }

    private TransactionWork<List<Record>> updateRelations(@NonNull List<NeoRelation> relations) {
        return tx -> {
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

            try {
                Result result = tx.run(query);
                return result.list();
            } catch (Neo4jException e) {
                log.error("Neo4JService.updateRelations: exception={}", e.getMessage());
                throw e;
            }
        };
    }

    private TransactionWork<List<Record>> decrementNodes() {
        return tx -> {
            String query =
                    "MATCH (w:Word)\n" +
                    "SET w.count = w.count - 1\n" +
                    "RETURN w";
            try {
                Result result = tx.run(query);
                return result.list();
            } catch (Neo4jException e) {
                log.error("Neo4JService.decrementNodes: exception={}", e.getMessage());
                throw e;
            }
        };
    }

    private TransactionWork<List<Record>> decrementRelations() {
        return tx -> {
            String query =
                    "MATCH (:Word)-[r]->(:Word)\n" +
                    "SET r.count = r.count - 1\n" + "RETURN  r";
            try {
                Result result = tx.run(query);
                return result.list();
            } catch (Neo4jException e) {
                log.error("Neo4JService.decrementRelations: exception={}", e.getMessage());
                throw e;
            }
        };
    }




    @Override
    public void run(String... args) {}

}
