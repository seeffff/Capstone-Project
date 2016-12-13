package com.newwesterndev.gpsalarm;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.FrameLayout;

import com.newwesterndev.gpsalarm.database.AlarmContract;
import com.newwesterndev.gpsalarm.database.AlarmProvider;
import com.newwesterndev.gpsalarm.utility.Alarm;
import com.newwesterndev.gpsalarm.utility.AlarmAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.newwesterndev.gpsalarm.database.AlarmContract.AlarmsEntry.allColumns;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    @BindView(R.id.alarm_list) RecyclerView recyclerView;
    @BindView(R.id.new_alarm_fab) FloatingActionButton fab;
    private AlarmAdapter adapter;
    private static final String PROVIDER_NAME = "newwesterndev.alarmdatabase.alarms";
    private static final Uri CONTENT_URI = Uri.parse("content://" + PROVIDER_NAME + "/alarms");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        adapter = new AlarmAdapter(this, getAlarms());
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ActivityCompat.requestPermissions(this, new String[]{
                android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION
        }, 10);

        fab.setOnClickListener(view -> {
            Intent i = new Intent(this, AlarmDetailActivity.class);
            startActivity(i);
        });
    }

    public ArrayList<Alarm> getAlarms(){
        ArrayList<Alarm> alarms = new ArrayList<>();
        Cursor cursor = getContentResolver().query(CONTENT_URI, allColumns, null, null, null);

        for (cursor.moveToLast(); !cursor.isBeforeFirst(); cursor.moveToPrevious()) {
            long id = (cursor.getLong(cursor.getColumnIndex(AlarmContract.AlarmsEntry.COLUMN_ID)));
            double range = (cursor.getDouble(cursor.getColumnIndex(AlarmContract.AlarmsEntry.COLUMN_ALARM_RANGE)));
            String type = (cursor.getString(cursor.getColumnIndex(AlarmContract.AlarmsEntry.COLUMN_ALARM_RANGE_TYPE)));
            String destination = (cursor.getString(cursor.getColumnIndex(AlarmContract.AlarmsEntry.COLUMN_ALARM_DESTINATION)));
            int active = (cursor.getInt(cursor.getColumnIndex(AlarmContract.AlarmsEntry.COLUMN_IS_ACTIVE)));
            int volume = (cursor.getInt(cursor.getColumnIndex(AlarmContract.AlarmsEntry.COLUMN_VOLUME)));
            int vibrate = (cursor.getInt(cursor.getColumnIndex(AlarmContract.AlarmsEntry.COLUMN_VIBRATE)));
            double lon = (cursor.getDouble(cursor.getColumnIndex(AlarmContract.AlarmsEntry.COLUMN_LON)));
            double lat = (cursor.getDouble(cursor.getColumnIndex(AlarmContract.AlarmsEntry.COLUMN_LAT)));

            Alarm alarm = new Alarm(destination, active, volume, vibrate, lon, lat, range, type, id);
            alarms.add(alarm);

            Log.e("Alarm main details", alarm.getDestination());
        }

        cursor.close();
        return alarms;

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, CONTENT_URI,
                allColumns,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
