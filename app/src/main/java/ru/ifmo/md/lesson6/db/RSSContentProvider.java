package ru.ifmo.md.lesson6.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Created by MSviridenkov on 18.01.15.
 */
public class RSSContentProvider extends ContentProvider {
    private static String AUTHORITY = "ru.ifmo.md.lesson6.db.RSSContentProvider";

    public static final Uri CHANNEL_CONTENT_URL = Uri.parse("content://" + AUTHORITY + "/channels");
    public static final Uri NEWS_CONTENT_URL = Uri.parse("content://" + AUTHORITY + "/news");

    private RSSDBHelper rssDbHelper;

    private String getTableName(Uri uri) {
        return getTableName(uri.getLastPathSegment());
    }

    private String getTableName(String type) {
        String tableName;
        if(type.equals("channels")) {
            tableName = rssDbHelper.TABLE_CHANNEL;
        } else if(type.equals("news")) {
            tableName = rssDbHelper.TABLE_NEWS;
        } else {
            throw new UnsupportedOperationException("Invalid data type");
        }
        return tableName;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = rssDbHelper.getWritableDatabase();
        return db.delete(getTableName(uri), selection, selectionArgs);
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = rssDbHelper.getWritableDatabase();
        String tableName = getTableName(uri);
        long id = db.insert(tableName, null, values);
        return Uri.parse("content://" + AUTHORITY + "/" + tableName + "/" + Long.toString(id));
    }

    @Override
    public boolean onCreate() {
        rssDbHelper = new RSSDBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = rssDbHelper.getReadableDatabase();
        return db.query(getTableName(uri), projection, selection, selectionArgs, null, null, sortOrder);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        SQLiteDatabase db = rssDbHelper.getWritableDatabase();
        return db.update(getTableName(uri), values, selection, selectionArgs);
    }
}
