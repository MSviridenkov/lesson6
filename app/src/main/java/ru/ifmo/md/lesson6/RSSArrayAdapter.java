package ru.ifmo.md.lesson6;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by MSviridenkov on 19.10.2014.
 */
public class RSSArrayAdapter extends ArrayAdapter<RSSItem> {
    public RSSArrayAdapter(Context context, ArrayList<RSSItem> rssItems) {
        super(context, R.layout.list_item, rssItems);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RSSItem rssItem = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.list_item, null);
        }
        ((TextView) convertView.findViewById(R.id.title))
                .setText(rssItem.getTitle());
        ((TextView) convertView.findViewById(R.id.description))
                .setText(rssItem.getDescription());
        ((TextView) convertView.findViewById(R.id.pubdate))
                .setText(rssItem.getPubdate());

        return convertView;
    }
}
