package com.newwesterndev.gpsalarm;

import android.Manifest;
import android.app.ActivityOptions;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Fade;
import android.transition.Transition;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.newwesterndev.gpsalarm.utility.AlarmAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.newwesterndev.gpsalarm.database.AlarmContract.AlarmsEntry.allColumns;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    @BindView(R.id.alarm_list) RecyclerView recyclerView;
    @BindView(R.id.new_alarm_fab) FloatingActionButton fab;
    @BindView(R.id.new_alarm_image) ImageView newAlarmImage;
    private static final int CURSOR_LOADER_ID = 0;
    private static final String PROVIDER_NAME = "newwesterndev.alarmdatabase.alarms";
    private static final Uri CONTENT_URI = Uri.parse("content://" + PROVIDER_NAME + "/alarms");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Intent postAlarm = getIntent();
        if(postAlarm.getStringExtra("KillMe") != null){
            this.finishAffinity();
        }

        if(savedInstanceState != null){
            getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);
        }
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);

        ActivityCompat.requestPermissions(this, new String[]{
                android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.INTERNET
        }, 10);

        fab.setOnClickListener(view -> {
            if(testServices()) {
                fadeOut();
            } else{
                Toast.makeText(this, getResources().getString(R.string.location_disabled),Toast.LENGTH_SHORT).show();
            }
        });
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
        AlarmAdapter adapter = new AlarmAdapter(data, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        if(adapter.getItemCount() != 0){
            newAlarmImage.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        recyclerView.setAdapter(null);
    }

    public void fadeOut(){

            Transition exitTrans = new Fade(Fade.OUT);
            getWindow().setExitTransition(exitTrans);
            exitTrans.setDuration(400);
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this);
            Intent i = new Intent(MainActivity.this, AlarmDetailActivity.class);
            startActivity(i, options.toBundle());
    }

    public boolean testServices() {
        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = false;
        boolean networkEnabled = false;
        boolean locationEnabled = false;

        try {
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if (gpsEnabled && networkEnabled) {
            locationEnabled = true;
        }

        return locationEnabled;
    }
}
