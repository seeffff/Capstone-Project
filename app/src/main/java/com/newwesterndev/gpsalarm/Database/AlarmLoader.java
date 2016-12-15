package com.newwesterndev.gpsalarm.database;

import android.content.Context;
import android.content.CursorLoader;
import android.net.Uri;

public class AlarmLoader extends CursorLoader {
    public static AlarmLoader newAllAlarmsInstance(Context context) {
        return new AlarmLoader(context, AlarmContract.AlarmsEntry.CONTENT_URI);
    }

    public static AlarmLoader newInstanceForItemId(Context context, long itemId) {
        return new AlarmLoader(context, AlarmContract.AlarmsEntry.buildAlarmUri(itemId));
    }

    private AlarmLoader(Context context, Uri uri) {
        super(context, uri, Query.PROJECTION, null, null, AlarmContract.AlarmsEntry.DEFAULT_SORT);
    }

    public interface Query {
        String[] PROJECTION = AlarmContract.AlarmsEntry.allColumns;

        int COLUMN_ID = 0;
        int COLUMN_ALARM_RANGE = 1;
        int COLUMN_ALARM_RANGE_TYPE = 2;
        int COLUMN_ALARM_DESTINATION = 3;
        int COLUMN_IS_ACTIVE = 4;
        int COLUMN_VOLUME = 5;
        int COLUMN_VIBRATE = 6;
        int COLUMN_LON = 7;
        int COLUMN_LAT = 8;
    }
}