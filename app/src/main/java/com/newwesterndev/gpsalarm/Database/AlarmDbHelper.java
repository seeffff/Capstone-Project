package com.newwesterndev.gpsalarm.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.support.v4.content.CursorLoader;

import com.newwesterndev.gpsalarm.database.AlarmContract.AlarmsEntry;
import com.newwesterndev.gpsalarm.utility.Alarm;

import java.util.ArrayList;

import static com.newwesterndev.gpsalarm.database.AlarmContract.AlarmsEntry.COLUMN_ID;

public class AlarmDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "alarms.db";

    public AlarmDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String CREATE_TABLE_ALARM = " create table " + AlarmsEntry.TABLE_NAME + " ( "
                + AlarmsEntry.COLUMN_ID + " integer primary key autoincrement, "
                + AlarmsEntry.COLUMN_ALARM_RANGE + " double not null, "
                + AlarmsEntry.COLUMN_ALARM_RANGE_TYPE + " text not null, "
                + AlarmsEntry.COLUMN_ALARM_DESTINATION + " text not null, "
                + AlarmsEntry.COLUMN_IS_ACTIVE + " integer not null, "
                + AlarmsEntry.COLUMN_VOLUME + " integer not null, "
                + AlarmsEntry.COLUMN_VIBRATE + " integer not null, "
                + AlarmsEntry.COLUMN_LON + " double not null, "
                + AlarmsEntry.COLUMN_LAT + " double not null " + " ); ";

        sqLiteDatabase.execSQL(CREATE_TABLE_ALARM);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + AlarmsEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public Cursor getAlarms(String id, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder sqliteQueryBuilder = new SQLiteQueryBuilder();
        sqliteQueryBuilder.setTables(AlarmsEntry.TABLE_NAME);

        if(id != null) {
            sqliteQueryBuilder.appendWhere("_id" + " = " + id);
        }

        if(sortOrder == null || sortOrder == "") {
            sortOrder = "COLUMN_ID";
        }
        Cursor cursor = sqliteQueryBuilder.query(getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
        return cursor;
    }

    public long addNewAlarm(ContentValues values) throws SQLException {
        long id = getWritableDatabase().insert(AlarmsEntry.TABLE_NAME, "", values);
        if(id <=0 ) {
            throw new SQLException("Failed to add an image");
        }

        return id;
    }

    public int deleteAlarms(String id) {
        if(id == null) {
            return getWritableDatabase().delete(AlarmsEntry.TABLE_NAME, null , null);
        } else {
            return getWritableDatabase().delete(AlarmsEntry.TABLE_NAME, "_id=?", new String[]{id});
        }
    }

    public int updateAlarms(String id, ContentValues values) {
        if(id == null) {
            return getWritableDatabase().update(AlarmsEntry.TABLE_NAME, values, null, null);
        } else {
            return getWritableDatabase().update(AlarmsEntry.TABLE_NAME, values, "_id=?", new String[]{id});
        }
    }
}
