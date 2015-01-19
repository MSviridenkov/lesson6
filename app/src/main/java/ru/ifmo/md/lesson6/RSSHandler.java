package ru.ifmo.md.lesson6;

import android.util.Log;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

/**
 * Created by MSviridenkov on 19.10.2014.
 */
public class RSSHandler extends DefaultHandler {
    public ArrayList<RSSItem> rssItems = new ArrayList<RSSItem>();
    private RSSItem rssItem;
    private boolean inItem = false;
    private final StringBuilder characters = new StringBuilder();

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (localName.equalsIgnoreCase("item")) {
            inItem = true;
            rssItem = new RSSItem();
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if (inItem) {
            final String content = characters.toString().trim();

            if (localName.equalsIgnoreCase("item")) {
                inItem = false;
                rssItems.add(rssItem);
                rssItem = null;
            } else if (localName.equalsIgnoreCase("title")) {
                rssItem.setTitle(content);
            } else if (localName.equalsIgnoreCase("link")) {
                rssItem.setLink(content);
            } else if (localName.equalsIgnoreCase("description")) {
                rssItem.setDescription(content);
            } else if (localName.equalsIgnoreCase("pubdate")) {
                rssItem.setPubdate(content);
            }

            characters.setLength(0);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (inItem) {
            Log.i("SMTH:", new String(ch, start, length));
            characters.append(new String(ch, start, length));
        }
    }

    public ArrayList<RSSItem> getRssItems() {
        return rssItems;
    }
}
