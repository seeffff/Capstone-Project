package com.newwesterndev.gpsalarm.database;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class AlarmContract {

    public static final String CONTENT_AUTHORITY = "newwesterndev.alarmdatabase.alarms";


    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_ALARMS = "alarms";

    public static final class AlarmsEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ALARMS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ALARMS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ALARMS;

        public static final String TABLE_NAME = "alarms";

        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_ALARM_RANGE = "range";
        public static final String COLUMN_ALARM_RANGE_TYPE = "type";
        public static final String COLUMN_ALARM_DESTINATION = "destination";
        public static final String COLUMN_IS_ACTIVE = "active";
        public static final String COLUMN_VOLUME = "volume";
        public static final String COLUMN_VIBRATE = "vibrate";
        public static final String COLUMN_LON = "longitude";
        public static final String COLUMN_LAT = "latitude";

        public static final String DEFAULT_SORT = COLUMN_ID + " DESC";

        public static final String[] allColumns =
                {COLUMN_ID, COLUMN_ALARM_RANGE, COLUMN_ALARM_RANGE_TYPE, COLUMN_ALARM_DESTINATION, COLUMN_IS_ACTIVE,
                COLUMN_VOLUME, COLUMN_VIBRATE, COLUMN_LON, COLUMN_LAT};

        public static Uri buildAlarmUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }


    }

}
