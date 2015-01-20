package ru.ifmo.md.lesson6;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import ru.ifmo.md.lesson6.db.RSSContentProvider;
import ru.ifmo.md.lesson6.db.RSSDBHelper;

/**
 * Created by Mikhail on 19.01.15.
 */
public class ChannelIntentService extends IntentService {
    public ChannelIntentService() {
        super("RSSIntentService");
    }

    private String action;
    private String channelName;
    private String link;
    private ResultReceiver receiver;

    @Override
    protected void onHandleIntent(Intent intent) {
        receiver = intent.getParcelableExtra("receiver");

        action = intent.getStringExtra("action");
        channelName = intent.getStringExtra("channel_name");
        link = intent.getStringExtra("link");

        parseAndDownloadChannel();
    }

    public void parseAndDownloadChannel() {
        //receiver.send(AppResultsReceiver.STATUS_RUNNING, Bundle.EMPTY);
        ArrayList<RSSItem> rssItems = new ArrayList<RSSItem>();
        StringBuilder content = new StringBuilder();

        try {
            URL rssURL = new URL(link);
            URLConnection urlConnection = rssURL.openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line + "\n");
            }
            bufferedReader.close();

            SAXParserFactory mySAXParserFactory = SAXParserFactory.newInstance();
            SAXParser mySAXParser = mySAXParserFactory.newSAXParser();
            XMLReader myXMLReader = mySAXParser.getXMLReader();
            RSSHandler myRSSHandler = new RSSHandler();
            myXMLReader.setContentHandler(myRSSHandler);
            InputSource myInputSource = new InputSource(new StringReader(content.toString()));
            myXMLReader.parse(myInputSource);

            rssItems = myRSSHandler.getRssItems();

            boolean exists = false;

            if (getContentResolver().query(RSSContentProvider.CHANNEL_CONTENT_URL, null,
                    RSSDBHelper.COLUMN_NAME_CHANNEL_NAME + "=?", new String[] {channelName}, null).getCount() > 0) {
                exists = true;
            }

            if (action.equals("update")) {
                getContentResolver().delete(RSSContentProvider.NEWS_CONTENT_URL, RSSDBHelper.COLUMN_NAME_CHANNEL_NAME + "=?", new String[] {channelName});

                for (int i = 0; i < rssItems.size(); i++) {
                    ContentValues rssValues = new ContentValues();

                    rssValues.put(RSSDBHelper.COLUMN_NAME_CHANNEL_NAME, channelName);
                    rssValues.put(RSSDBHelper.COLUMN_NAME_TITLE, rssItems.get(i).getTitle());
                    rssValues.put(RSSDBHelper.COLUMN_NAME_LINK, rssItems.get(i).getLink());
                    rssValues.put(RSSDBHelper.COLUMN_NAME_DESCRIPTION, rssItems.get(i).getDescription());
                    rssValues.put(RSSDBHelper.COLUMN_NAME_PUBDATE, rssItems.get(i).getPubdate());

                    getContentResolver().insert(RSSContentProvider.NEWS_CONTENT_URL, rssValues);
                }

                receiver.send(AppResultsReceiver.STATUS_REFRESHED, Bundle.EMPTY);
            } else if (action.equals("add")) {
                if (!exists) {
                    ContentValues channelValues = new ContentValues();
                    channelValues.put(RSSDBHelper.COLUMN_NAME_CHANNEL_NAME, channelName);
                    channelValues.put(RSSDBHelper.COLUMN_NAME_LINK, link);

                    getContentResolver().insert(RSSContentProvider.CHANNEL_CONTENT_URL, channelValues);

                    for (int i = 0; i < rssItems.size(); i++) {
                        ContentValues rssValues = new ContentValues();

                        rssValues.put(RSSDBHelper.COLUMN_NAME_CHANNEL_NAME, channelName);
                        rssValues.put(RSSDBHelper.COLUMN_NAME_TITLE, rssItems.get(i).getTitle());
                        rssValues.put(RSSDBHelper.COLUMN_NAME_LINK, rssItems.get(i).getLink());
                        rssValues.put(RSSDBHelper.COLUMN_NAME_DESCRIPTION, rssItems.get(i).getDescription());
                        rssValues.put(RSSDBHelper.COLUMN_NAME_PUBDATE, rssItems.get(i).getPubdate());

                        getContentResolver().insert(RSSContentProvider.NEWS_CONTENT_URL, rssValues);
                    }

                    receiver.send(AppResultsReceiver.STATUS_ADDED, Bundle.EMPTY);
                } else {
                    receiver.send(AppResultsReceiver.STATUS_ALREADY_ADDED, Bundle.EMPTY);
                }
            }

        } catch (SAXException | ParserConfigurationException e) {
            receiver.send(AppResultsReceiver.STATUS_PARSE_ERROR, Bundle.EMPTY);
            e.printStackTrace();
        } catch (MalformedURLException e) {
            receiver.send(AppResultsReceiver.STATUS_INCORRECT_URL, Bundle.EMPTY);
            e.printStackTrace();
        } catch (IOException e) {
            receiver.send(AppResultsReceiver.STATUS_INTERNET_ERROR, Bundle.EMPTY);
            e.printStackTrace();
        }
    }
}
