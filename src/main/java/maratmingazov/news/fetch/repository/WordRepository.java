package maratmingazov.news.fetch.repository;

import maratmingazov.news.fetch.model.mongodb.MongoWord;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface WordRepository extends MongoRepository<MongoWord, String> {
}
