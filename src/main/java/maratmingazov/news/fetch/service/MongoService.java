package maratmingazov.news.fetch.service;

import lombok.NonNull;
import maratmingazov.news.fetch.model.google.GoogleNewsArticle;
import maratmingazov.news.fetch.model.mongodb.MongoArticle;

import java.util.List;

public interface MongoService {

    @NonNull
    List<MongoArticle> saveArticles(@NonNull List<GoogleNewsArticle> articles);
}
