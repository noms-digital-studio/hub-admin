package uk.gov.justice.digital.noms.hub.ports.http;

import uk.gov.justice.digital.noms.hub.domain.Article;

public class CreateArticleRequest {
    private String title;

    public CreateArticleRequest(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Article buildArticle() {
        return new Article(this.title);
    }
}
