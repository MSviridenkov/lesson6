package ru.ifmo.md.lesson6;

/**
 * Created by MSviridenkov on 19.10.2014.
 */
public class RSSItem {
    private String title = null;
    private String link = null;
    private String discription = null;
    private String pubdate = null;

    public void setTitle(String value) {
        this.title = value;
    }

    public void setLink(String value) {
        this.link = value;
    }

    public void setDescription(String value) {
        this.discription = value;
    }

    public void setPubdate(String value) {
        this.pubdate = value;
    }

    public String getTitle() {
        return this.title;
    }

    public String getLink() {
        return this.link;
    }

    public String getDescription() {
        return this.discription;
    }

    public String getPubdate() {
        return this.pubdate;
    }
}
