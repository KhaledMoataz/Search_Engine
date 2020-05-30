package main.model;

public class ImageSearchResult extends SearchResult {
    private Integer id;
    private String url;
    private String pageUrl;
    private String title;

    public ImageSearchResult(Integer id, String url, String pageUrl, String title, Double score) {
        super(score);
        this.id = id;
        this.url = url;
        this.pageUrl = pageUrl;
        this.title = title;
    }

    @Override
    public Integer getID() {
        return id;
    }

    @Override
    public String getUrl() {
        return url;
    }

    public String getPageUrl() { return pageUrl; }

    public String getTitle() {
        return title;
    }

}
