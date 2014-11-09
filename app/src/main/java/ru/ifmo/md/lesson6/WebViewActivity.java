package ru.ifmo.md.lesson6;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by Миша on 20.10.2014.
 */
public class WebViewActivity extends Activity {
    WebView myWebView;
    String rssItemStringURL;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        rssItemStringURL = getIntent().getStringExtra("RSSItemURL");

        myWebView = (WebView) findViewById(R.id.webview);
        myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.setWebViewClient(new myWebViewClient());
        myWebView.loadUrl(rssItemStringURL);
    }

    class myWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
}
