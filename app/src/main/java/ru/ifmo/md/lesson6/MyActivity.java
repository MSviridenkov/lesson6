package ru.ifmo.md.lesson6;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


public class MyActivity extends ListActivity {
    EditText input;
    SimpleDateFormat format;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        format = new SimpleDateFormat(getResources().getString(R.string.date_format), Locale.ENGLISH);
        input = (EditText) findViewById(R.id.editText1);

        input.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    if (input.getText().toString().isEmpty()) {
                        showToast(R.string.correct_url, Toast.LENGTH_LONG);
                    } else {
                        updateList(input.getText().toString());
                    }
                    return true;
                }
                return false;
            }
        });
    }

    public void onReadClick(View view) {
        if (input.getText().toString().isEmpty()) {
            showToast(R.string.correct_url, Toast.LENGTH_LONG);
        } else {
            updateList(input.getText().toString());
        }
    }

    public void onBBCClick(View view) {
        updateList(getResources().getString(R.string.bbc_feed));
    }

    public void updateList(String rssStringURL) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);

        if(!rssStringURL.startsWith("http://")){
            rssStringURL = "http://" + rssStringURL;
        }
        if (isNetworkConnected()) {
            new DownloadAndParseXMLTask().execute(rssStringURL);
            input.setText("");
        } else {
            showToast(R.string.no_internet_connection, Toast.LENGTH_LONG);
        }
    }

    class DownloadAndParseXMLTask extends AsyncTask<String, Void, ArrayList<RSSItem>> {
        private boolean correctURL = true;
        private boolean canOpen = true;
        private boolean canParse = true;
        private boolean isSorted = true;

        @Override
        protected ArrayList<RSSItem> doInBackground(String... arg0) {
            String rssStringURL = arg0[0];
            ArrayList<RSSItem> rssItems = new ArrayList<RSSItem>();
            StringBuilder content = new StringBuilder();
            publishProgress();

            try {
                URL rssURL = new URL(rssStringURL);
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
            } catch (SAXException e) {
                canParse = false;
                e.printStackTrace();
                return null;
            } catch (ParserConfigurationException e) {
                canParse = false;
                e.printStackTrace();
                return null;
            } catch (MalformedURLException e) {
                correctURL = false;
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                canOpen = false;
                e.printStackTrace();
                return null;
            }

            if (rssItems != null && !rssItems.isEmpty()) {
                if (dateParse(rssItems.get(0).getPubdate()) != null) {
                    Collections.sort(rssItems, new RSSItemComparator());
                    Collections.reverse(rssItems);
                } else {
                    isSorted = false;
                }
            } else {
                canParse = false;
            }

            return rssItems;
        }

        @Override
        protected void onProgressUpdate(Void... arg0) {
            showToast(R.string.waiting, Toast.LENGTH_SHORT);
        }

        @Override
        protected void onPostExecute(ArrayList<RSSItem> result) {
            final ArrayList<RSSItem> rssItems = result;
            if (!correctURL) {
                showToast(R.string.correct_url, Toast.LENGTH_LONG);
            } else if (!canOpen) {
                showToast(R.string.cannot_open, Toast.LENGTH_LONG);
            } else if (!canParse) {
                showToast(R.string.cannot_parse, Toast.LENGTH_LONG);
            } else {
                if (!isSorted) {
                    showToast(R.string.cannot_parse_date, Toast.LENGTH_LONG);
                }
                ArrayAdapter<RSSItem> adapter = new RSSArrayAdapter(getApplicationContext(), rssItems);
                setListAdapter(adapter);
                final ListView myListView = getListView();
                myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        String rssItemStringURL = rssItems.get(position).getLink();
                        URL rssItemURL = null;
                        try {
                            rssItemURL = new URL(rssItemStringURL);
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                        if (rssItemStringURL != null && rssItemURL != null) {
                            Intent intent = new Intent(MyActivity.this, WebViewActivity.class);
                            intent.putExtra("RSSItemURL", rssItemStringURL);
                            startActivity(intent);
                        } else {
                            showToast(R.string.rss_item_url_error, Toast.LENGTH_LONG);
                        }
                    }
                });
            }
        }
    }

    class RSSItemComparator implements Comparator<RSSItem> {
        public int compare(RSSItem firstItem, RSSItem secondItem) {
            Date firstDate = dateParse(firstItem.getPubdate());
            Date secondDate = dateParse(secondItem.getPubdate());

            return firstDate.compareTo(secondDate);
        }
    }

    public Date dateParse(String str) {
        try {
            return format.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void showToast(int message, int time) {
        Toast toast = Toast.makeText(getApplicationContext(), message, time);
        toast.show();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo == null) {
            return false;
        } else
            return true;
    }

    /*public boolean isInternetAvailable() {
        try {
            InetAddress inAddr = InetAddress.getByName("www.google.com");
            

            if (inAddr.equals("")) {
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }*/
}