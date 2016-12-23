package uk.gov.justice.digital.noms.hub.ports.http;

import uk.gov.justice.digital.noms.hub.domain.Article;

public class CreateArticleRequest {
    private String title;

    public String getTitle() {
        return title;
    }

    Article buildArticle() {
        return new Article(this.title);
    }
}
