package ru.ifmo.md.lesson6;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

/**
 * Created by Mikhail on 19.01.15.
 */
public class AppResultsReceiver extends ResultReceiver {
    public static final int STATUS_RUNNING = 0;
    public static final int STATUS_ADDED = 1;
    public static final int STATUS_REFRESHED = 2;
    public static final int STATUS_INTERNET_ERROR = 3;
    public static final int STATUS_PARSE_ERROR = 4;
    public static final int STATUS_ALREADY_ADDED = 6;
    public static final int STATUS_INCORRECT_URL = 7;

    public interface Receiver {
        public void onReceiveResult(int resultCode, Bundle data);
    }

    private Receiver mReceiver;

    public AppResultsReceiver(Handler handler) {
        super(handler);
    }

    public void setReceiver(Receiver receiver) {
        mReceiver = receiver;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (mReceiver != null) {
            mReceiver.onReceiveResult(resultCode, resultData);
        }
    }
}
