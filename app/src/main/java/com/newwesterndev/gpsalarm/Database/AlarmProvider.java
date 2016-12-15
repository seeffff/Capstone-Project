package com.newwesterndev.gpsalarm.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.newwesterndev.gpsalarm.utility.Alarm;

import java.util.ArrayList;

import static com.newwesterndev.gpsalarm.database.AlarmContract.AlarmsEntry.allColumns;

public class AlarmProvider extends ContentProvider{

    AlarmDbHelper dbHelper ;
    public static final String AUTHORITY = "newwesterndev.alarmdatabase.alarms";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    @Override
    public boolean onCreate() {
        dbHelper = new AlarmDbHelper(getContext());
        return true;
    }

    @Override
    public int delete(Uri uri, String where, String[] args) {
        String table = getTableName(uri);
        SQLiteDatabase dataBase = dbHelper.getWritableDatabase();
        return dataBase.delete(table, where + " = ?", args);
    }

    @Override
    public String getType(Uri arg0) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        String table = getTableName(uri);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        long value = database.insert(table, null, initialValues);
        return Uri.withAppendedPath(CONTENT_URI, String.valueOf(value));
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        String table = getTableName(uri);
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor cursor = database.query(table,  projection, selection, selectionArgs, null, null, sortOrder);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where,
                      String[] whereArgs) {
        String table = getTableName(uri);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        return database.update(table, values, where + " = ?", whereArgs);
    }

    public static String getTableName(Uri uri){
        String value = uri.getPath();
        value = value.replace("/", "");
        return value;
    }
}
