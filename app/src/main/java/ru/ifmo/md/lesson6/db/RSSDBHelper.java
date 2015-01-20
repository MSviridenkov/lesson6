package ru.ifmo.md.lesson6.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Mikhail on 18.01.15.
 */
public class RSSDBHelper extends SQLiteOpenHelper{
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "rssreader";

    public static final String TABLE_CHANNEL = "channels";
    public static final String TABLE_NEWS = "news";

    public static final String _ID = "_id";
    public static final String COLUMN_NAME_CREATED_AT = "created_at";

    public static final String COLUMN_NAME_CHANNEL_NAME = "channel_name";

    public static final String COLUMN_NAME_TITLE = "title";
    public static final String COLUMN_NAME_LINK = "link";
    public static final String COLUMN_NAME_DESCRIPTION = "description";
    public static final String COLUMN_NAME_PUBDATE = "pubdate";

    private static final String CREATE_TABLE_CHANNEL = "CREATE TABLE "
            + TABLE_CHANNEL + " (" +
            _ID + " INTEGER PRIMARY KEY," +
            COLUMN_NAME_CHANNEL_NAME + " TEXT," +
            COLUMN_NAME_LINK + " TEXT," +
            COLUMN_NAME_CREATED_AT + " DATETIME" + " );";

    private static final String CREATE_TABLE_NEWS = "CREATE TABLE "
            + TABLE_NEWS + " (" +
            _ID + " INTEGER PRIMARY KEY," +
            COLUMN_NAME_CHANNEL_NAME + " TEXT," +
            COLUMN_NAME_TITLE + " TEXT," +
            COLUMN_NAME_LINK + " TEXT," +
            COLUMN_NAME_DESCRIPTION + " TEXT," +
            COLUMN_NAME_PUBDATE + " TEXT," +
            COLUMN_NAME_CREATED_AT + " DATETIME" + " );";

    public RSSDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_CHANNEL);
        db.execSQL(CREATE_TABLE_NEWS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHANNEL);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NEWS);

        onCreate(db);
    }
}
