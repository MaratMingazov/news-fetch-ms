package maratmingazov.news.fetch.service;

import lombok.NonNull;
import maratmingazov.news.fetch.model.google.GoogleNewsArticle;
import maratmingazov.news.fetch.model.mongodb.MongoArticle;
import maratmingazov.news.fetch.model.mongodb.MongoQuintet;

import java.util.List;

public interface MongoService {

    @NonNull
    List<MongoArticle> saveArticles(@NonNull List<GoogleNewsArticle> articles);

    @NonNull
    List<MongoQuintet> calculateQuintets(@NonNull List<MongoArticle> articles);

    @NonNull
    Integer saveQuintets(@NonNull List<MongoQuintet> quintets);
}
