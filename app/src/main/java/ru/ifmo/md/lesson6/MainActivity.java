package ru.ifmo.md.lesson6;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;

import ru.ifmo.md.lesson6.db.RSSContentProvider;
import ru.ifmo.md.lesson6.db.RSSDBHelper;


public class MainActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor>, AppResultsReceiver.Receiver {
    public AppResultsReceiver mReceiver;

    private String currentChannelName;
    private String currentChannelLink;
    private String wantAddChannelName = "";
    private String wantAddChannelLink = "";
    private Toolbar toolbar;
    private ChannelCursorAdapter channelCursorAdapter;
    private RSSCursorAdapter rssCursorAdapter;
    private EditText nameAdd;
    private EditText linkAdd;
    private Button buttonAdd;
    private SharedPreferences prefs = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mReceiver = new AppResultsReceiver(new Handler());
        mReceiver.setReceiver(this);

        prefs = getSharedPreferences("ru.ifmo.md.lesson8", MODE_PRIVATE);

        toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        View spinnerContainer = LayoutInflater.from(this).inflate(R.layout.toolbar_spinner, toolbar, false);
        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        toolbar.addView(spinnerContainer, lp);

        channelCursorAdapter = new ChannelCursorAdapter(this, null);
        rssCursorAdapter = new RSSCursorAdapter(this, null);

        final Spinner spinner = (Spinner) spinnerContainer.findViewById(R.id.toolbar_spinner);
        spinner.setAdapter(channelCursorAdapter);

        final ListView listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(rssCursorAdapter);

        getSupportLoaderManager().initLoader(0, null, this);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                Channel channel = channelCursorAdapter.get(pos);
                currentChannelName = channel.getChannelName();
                currentChannelLink = channel.getLink();
                getSupportLoaderManager().restartLoader(1, null, MainActivity.this);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }

        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String rssItemStringURL = rssCursorAdapter.get(position).getLink();
                URL rssItemURL = null;
                try {
                    rssItemURL = new URL(rssItemStringURL);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                if (rssItemStringURL != null && rssItemURL != null) {
                    Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
                    intent.putExtra("RSSItemURL", rssItemStringURL);
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.rss_item_url_error), Toast.LENGTH_SHORT).show();
                }
            }
        });

        nameAdd = (EditText) findViewById(R.id.channel_name_add);
        linkAdd = (EditText) findViewById(R.id.channel_link_add);
        buttonAdd = (Button) findViewById(R.id.button_add);
        nameAdd.setVisibility(View.GONE);
        linkAdd.setVisibility(View.GONE);
        buttonAdd.setVisibility(View.GONE);

        linkAdd.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    wantAddChannelLink = linkAdd.getText().toString();

                    return true;
                }
                return false;
            }
        });

        nameAdd.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    wantAddChannelName = nameAdd.getText().toString();

                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mReceiver = new AppResultsReceiver(new Handler());
        mReceiver.setReceiver(this);

        if (prefs.getBoolean("firstrun", true)) {
            refresh(new Channel("BBC", "http://feeds.bbci.co.uk/news/rss.xml"), "add");

            prefs.edit().putBoolean("firstrun", false).apply();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                linkAdd.setText("");
                linkAdd.setHint("Channel link");
                linkAdd.setVisibility(View.VISIBLE);
                nameAdd.setText("");
                nameAdd.setHint("Channel name");
                nameAdd.setVisibility(View.VISIBLE);
                buttonAdd.setVisibility(View.VISIBLE);
                break;
            case R.id.action_refresh:
                if (channelCursorAdapter.getCount() > 0) {
                    refresh(new Channel(currentChannelName, currentChannelLink), "update");
                } else {
                    Toast.makeText(this, getResources().getString(R.string.there_is_no_any_channel), Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                break;
        }

        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
        CursorLoader loader;
        if (id == 0) {
            loader = new CursorLoader(this,
                    RSSContentProvider.CHANNEL_CONTENT_URL, null, null, null, null);
        } else {
            loader = new CursorLoader(this,
                    RSSContentProvider.NEWS_CONTENT_URL, null, RSSDBHelper.COLUMN_NAME_CHANNEL_NAME + "=?", new String[] {currentChannelName}, null);
        }

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
        if (arg0.getId() == 0) {
            channelCursorAdapter.swapCursor(cursor);
        } else if (arg0.getId() == 1) {
            rssCursorAdapter.swapCursor(cursor);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        if (arg0.getId() == 0) {
            channelCursorAdapter.swapCursor(null);
        } else if (arg0.getId() == 2) {
            rssCursorAdapter.swapCursor(null);
        }
    }

    public void refresh(Channel channel, String update) {
        Intent intent = new Intent(this, ChannelIntentService.class);
        intent.putExtra("channel_name", channel.getChannelName());
        intent.putExtra("link", channel.getLink());
        intent.putExtra("action", update);
        intent.putExtra("receiver", mReceiver);
        this.startService(intent);
    }

    class ChannelCursorAdapter extends CursorAdapter {
        public ChannelCursorAdapter(Context context, Cursor cursor) {
            super(context, cursor, 0);
        }

        public class ViewHolder {
            public ImageButton button;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = LayoutInflater.from(context).inflate(R.layout.toolbar_spinner_item_actionbar, parent, false);

            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final String channelNameStr = cursor.getString(cursor.getColumnIndex("channel_name"));

            TextView channelName = (TextView) view.findViewById(R.id.text1);
            channelName.setText(channelNameStr);

            ViewHolder holder = (ViewHolder) view.getTag();

            if (holder != null) {
                holder.button.setOnClickListener(new View.OnClickListener() {

                    public void onClick(View arg0) {
                        MainActivity.this.getContentResolver().delete(RSSContentProvider.CHANNEL_CONTENT_URL, RSSDBHelper.COLUMN_NAME_CHANNEL_NAME + "=?", new String[]{channelNameStr});
                        MainActivity.this.getContentResolver().delete(RSSContentProvider.NEWS_CONTENT_URL, RSSDBHelper.COLUMN_NAME_CHANNEL_NAME + "=?", new String[]{channelNameStr});
                        notifyDataSetChanged();
                        MainActivity.this.getSupportLoaderManager().restartLoader(0, null, MainActivity.this);
                        MainActivity.this.getSupportLoaderManager().restartLoader(1, null, MainActivity.this);
                        MainActivity.this.getSupportLoaderManager().restartLoader(2, null, MainActivity.this);
                    }
                });
            }
        }

        @Override
        public View newDropDownView(Context context, Cursor cursor, ViewGroup parent) {
            ViewHolder holder = new ViewHolder();
            View view = LayoutInflater.from(context).inflate(R.layout.toolbar_spinner_item_dropdown, parent, false);
            holder.button = (ImageButton) view.findViewById(R.id.button_delete);
            view.setTag(holder);

            return view;
        }

        public Channel get(int position) {
            Cursor cursor = getCursor();
            Channel channel = new Channel();

            if(cursor.moveToPosition(position)) {
                channel.setChannelName(cursor.getString(cursor.getColumnIndex("channel_name")));
                channel.setLink(cursor.getString(cursor.getColumnIndex("link")));
            }

            return channel;
        }
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle data) {
        switch (resultCode) {
            case AppResultsReceiver.STATUS_RUNNING:
                Toast.makeText(this, getResources().getString(R.string.waiting), Toast.LENGTH_SHORT).show();
            case AppResultsReceiver.STATUS_REFRESHED:
                getSupportLoaderManager().restartLoader(0, null, this);
                getSupportLoaderManager().restartLoader(1, null, this);
                Toast.makeText(this, getResources().getString(R.string.current_channel_refreshed), Toast.LENGTH_SHORT).show();
                break;
            case AppResultsReceiver.STATUS_ADDED:
                Toast.makeText(this, getResources().getString(R.string.channel_added), Toast.LENGTH_SHORT).show();
                getSupportLoaderManager().restartLoader(0, null, this);
                break;
            case AppResultsReceiver.STATUS_INTERNET_ERROR:
                Toast.makeText(this, getResources().getString(R.string.internet_problem), Toast.LENGTH_SHORT).show();
                break;
            case AppResultsReceiver.STATUS_PARSE_ERROR:
                Toast.makeText(this, getResources().getString(R.string.parse_problem), Toast.LENGTH_SHORT).show();
                break;
            case AppResultsReceiver.STATUS_ALREADY_ADDED:
                Toast.makeText(this, getResources().getString(R.string.already_added), Toast.LENGTH_SHORT).show();
                break;
            case AppResultsReceiver.STATUS_INCORRECT_URL:
                Toast.makeText(this, getResources().getString(R.string.incorrect_url), Toast.LENGTH_SHORT).show();
                break;

            default:
                break;
        }
    }

    public void onClick(View view) {
        wantAddChannelLink = linkAdd.getText().toString();
        wantAddChannelName = nameAdd.getText().toString();
        linkAdd.setVisibility(View.GONE);
        nameAdd.setVisibility(View.GONE);
        buttonAdd.setVisibility(View.GONE);
        if (!wantAddChannelName.equals("")) {
            if(!wantAddChannelLink.startsWith("http://")) {
                wantAddChannelLink = "http://" + wantAddChannelLink;
            }
            refresh(new Channel(wantAddChannelName, wantAddChannelLink), "add");
        } else {
            Toast.makeText(this, getResources().getString(R.string.incorrect_channel_name), Toast.LENGTH_SHORT).show();
        }
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