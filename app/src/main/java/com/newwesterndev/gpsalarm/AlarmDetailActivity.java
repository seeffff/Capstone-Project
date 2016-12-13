package com.newwesterndev.gpsalarm;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.newwesterndev.gpsalarm.database.AlarmContract;
import com.newwesterndev.gpsalarm.utility.Alarm;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AlarmDetailActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {

    @BindView(R.id.mapView) MapView mapView;
    @BindView(R.id.zoom_in) ImageButton zoomInButton;
    @BindView(R.id.zoom_out) ImageButton zoomOutButton;
    @BindView(R.id.details) LinearLayout detailLayout;
    @BindView(R.id.detail_destination) EditText destinationEdit;
    @BindView(R.id.distance_spinner) Spinner distanceSpinner;
    @BindView(R.id.distance_type_spinner) Spinner distanceTypeSpinner;
    @BindView(R.id.vibrate_check) CheckBox vibrateCheck;
    @BindView(R.id.ringtone_volume) SeekBar ringtoneVolume;
    @BindView(R.id.test_button) Button testButton;
    @BindView(R.id.create_button) Button createButton;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    private static final String PROVIDER_NAME = "newwesterndev.alarmdatabase.alarms";
    private static final Uri CONTENT_URI = Uri.parse("content://" + PROVIDER_NAME + "/alarms");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_detail);
        ButterKnife.bind(this);

        populateSpinners();

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        zoomInButton.setOnClickListener(view -> {
            detailLayout.setVisibility(View.GONE);
            zoomOutButton.setVisibility(View.VISIBLE);
            zoomInButton.setVisibility(View.GONE);
        });

        zoomOutButton.setOnClickListener(view -> {
            detailLayout.setVisibility(View.VISIBLE);
            zoomOutButton.setVisibility(View.GONE);
            zoomInButton.setVisibility(View.VISIBLE);
        });

        createButton.setOnClickListener(view -> createAlarm());
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            mapView.getMapAsync(googleMap -> {
                googleMap.addMarker(new MarkerOptions().position(latLng));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                mapView.onResume();
            });
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setOnMapClickListener(latLng -> {
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(latLng));
            mLastLocation.setLatitude(latLng.latitude);
            mLastLocation.setLongitude(latLng.longitude);
        });
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    public void createAlarm(){

        String destination = destinationEdit.getText().toString();
        int volume = ringtoneVolume.getProgress() / 10;
        double lon = mLastLocation.getLongitude();
        double lat = mLastLocation.getLatitude();
        int vibrate = vibrate();
        String rangeString = distanceSpinner.getSelectedItem().toString();
        String rangeType = distanceTypeSpinner.getSelectedItem().toString();

        ContentValues cv = new ContentValues();
        cv.put(AlarmContract.AlarmsEntry.COLUMN_ALARM_DESTINATION, destination);
        cv.put(AlarmContract.AlarmsEntry.COLUMN_VOLUME, volume);
        cv.put(AlarmContract.AlarmsEntry.COLUMN_LON, lon);
        cv.put(AlarmContract.AlarmsEntry.COLUMN_LAT, lat);
        cv.put(AlarmContract.AlarmsEntry.COLUMN_VIBRATE, vibrate);
        cv.put(AlarmContract.AlarmsEntry.COLUMN_ALARM_RANGE, rangeString);
        cv.put(AlarmContract.AlarmsEntry.COLUMN_ALARM_RANGE_TYPE, rangeType);
        cv.put(AlarmContract.AlarmsEntry.COLUMN_IS_ACTIVE, 0);
        getContentResolver().insert(CONTENT_URI, cv);

        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);

        Alarm newAlarm = new Alarm(destination, 0, volume, vibrate(), lon, lat, Double.parseDouble(rangeString), rangeType);
    }

    public int vibrate() {
        if (vibrateCheck.isChecked()) {
            return 1;
        } else {
            return 0;
        }
    }

    public void populateSpinners(){
        ArrayList<Double> ranges = new ArrayList<>();
        ArrayList<String> rangeTypes = new ArrayList<>();

        ranges.add(0.25D);
        ranges.add(0.50D);
        ranges.add(0.75D);
        ranges.add(1D);
        ranges.add(2D);
        ranges.add(3D);
        ranges.add(5D);
        ranges.add(10D);

        ArrayAdapter<Double> rangeAdapter = new ArrayAdapter<Double>(this, R.layout.spinner_item, ranges);
        rangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        distanceSpinner.setAdapter(rangeAdapter);

        rangeTypes.add("miles");
        rangeTypes.add("kilometers");

        ArrayAdapter<String> rangeTypeAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, rangeTypes);
        rangeTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        distanceTypeSpinner.setAdapter(rangeTypeAdapter);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}