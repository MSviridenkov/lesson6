package ru.ifmo.md.lesson6;

/**
 * Created by Mikhail on 20.01.15.
 */
public class Channel {
    private String channelName = null;
    private String link = null;

    Channel(String channelName, String link) {
        this.channelName = channelName;
        this.link = link;
    }

    Channel() {
    }

    public void setChannelName(String value) {
        this.channelName = value;
    }

    public void setLink(String value) {
        this.link = value;
    }

    public String getChannelName() {
        return this.channelName;
    }

    public String getLink() {
        return this.link;
    }
}
