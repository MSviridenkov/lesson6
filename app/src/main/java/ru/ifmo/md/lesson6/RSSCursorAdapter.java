package ru.ifmo.md.lesson6;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by MSviridenkov on 18.01.15.
 */
public class RSSCursorAdapter extends CursorAdapter {
    public RSSCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText(cursor.getString(cursor.getColumnIndex("title")));

        TextView discription = (TextView) view.findViewById(R.id.description);
        title.setText(cursor.getString(cursor.getColumnIndex("title")));

        TextView pubDate = (TextView) view.findViewById(R.id.pubdate);
        title.setText(cursor.getString(cursor.getColumnIndex("title")));
    }

    public RSSItem get(int position) {
        Cursor cursor = getCursor();
        RSSItem rssItem = new RSSItem();

        if(cursor.moveToPosition(position)) {
            rssItem.setTitle(cursor.getString(cursor.getColumnIndex("title")));
            rssItem.setDescription(cursor.getString(cursor.getColumnIndex("description")));
            rssItem.setLink(cursor.getString(cursor.getColumnIndex("link")));
            rssItem.setPubdate(cursor.getString(cursor.getColumnIndex("pubdate")));
        }

        return rssItem;
    }
}
