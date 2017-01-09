package com.newwesterndev.gpsalarm;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.transition.Fade;
import android.transition.Transition;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.newwesterndev.gpsalarm.alarm.AlarmController;
import com.newwesterndev.gpsalarm.database.AlarmContract;
import com.newwesterndev.gpsalarm.utility.Alarm;
import com.newwesterndev.gpsalarm.utility.PlacesAutocompleteAdapter;
import com.newwesterndev.gpsalarm.widget.AlarmWidget;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AlarmDetailActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        OnMapReadyCallback, AdapterView.OnItemClickListener {

    @BindView(R.id.mapView) MapView mapView;
    @BindView(R.id.zoom_in) ImageButton zoomInButton;
    @BindView(R.id.zoom_out) ImageButton zoomOutButton;
    @BindView(R.id.details) LinearLayout detailLayout;
    @BindView(R.id.detail_destination_label) TextView destinationLabel;
    @BindView(R.id.detail_destination) AutoCompleteTextView destinationAuto;
    @BindView(R.id.radius_label) TextView radiusLabel;
    @BindView(R.id.distance_spinner) Spinner distanceSpinner;
    @BindView(R.id.distance_type_spinner) Spinner distanceTypeSpinner;
    @BindView(R.id.vibrate_label) TextView vibrateLabel;
    @BindView(R.id.vibrate_check) CheckBox vibrateCheck;
    @BindView(R.id.volume_label) TextView volumeLabel;
    @BindView(R.id.ringtone_volume) SeekBar ringtoneVolume;
    @BindView(R.id.test_button) Button testButton;
    @BindView(R.id.create_button) Button createButton;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LatLngBounds mBounds;
    LatLng autoPlaceLatLng;
    private PlacesAutocompleteAdapter mAdapter;
    private static final String PROVIDER_NAME = "newwesterndev.alarmdatabase.alarms";
    private static final Uri CONTENT_URI = Uri.parse("content://" + PROVIDER_NAME + "/alarms");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_detail);
        ButterKnife.bind(this);

        Transition enterTrans = new Fade(Fade.IN);
        enterTrans.setDuration(400);
        getWindow().setEnterTransition(enterTrans);

        passViews();

        final AlarmController alarmController = new AlarmController(this);
        populateSpinners();

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .build();
        }

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        mAdapter = new PlacesAutocompleteAdapter(this, mGoogleApiClient, mBounds, null);
        destinationAuto.setAdapter(mAdapter);
        destinationAuto.setOnItemClickListener(this);

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

        testButton.setOnClickListener(view -> {
            String test = testButton.getText().toString();
            String testString = getResources().getString(R.string.detail_test);
            String stopString = getResources().getString(R.string.detail_stop);

            int volume = ringtoneVolume.getProgress() / 10;

            if (test.equals(testString)) {
                createButton.setVisibility(View.GONE);
                alarmController.playSound(volume, vibrateCheck.isChecked());
                testButton.setText(R.string.detail_stop);
            } else if (test.equals(stopString)) {
                createButton.setVisibility(View.VISIBLE);
                alarmController.stopSound();
                testButton.setText(R.string.detail_test);
            }
        });

        distanceTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mapView.getMapAsync(googleMap -> {
                    drawCircle(googleMap);
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        distanceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mapView.getMapAsync(googleMap -> {
                    drawCircle(googleMap);
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
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
                    drawCircle(googleMap);
                    mapView.onResume();
                });

                mBounds = LatLngBounds.builder()
                        .include(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                        .include(new LatLng(mLastLocation.getLatitude() - 2, mLastLocation.getLongitude() - 2))
                        .include(new LatLng(mLastLocation.getLatitude() + 2, mLastLocation.getLongitude() + 2))
                        .build();
        }
    }

    public void drawCircle(GoogleMap googleMap){

        String distance = distanceSpinner.getSelectedItem().toString();
        String type = distanceTypeSpinner.getSelectedItem().toString();
        double circleRadius = 0;

        if(type.equals(getResources().getString(R.string.miles))){
            circleRadius = Double.parseDouble(distance)  * 1609.34;
        }else{
            circleRadius = Double.parseDouble(distance) * 1000;
        }

        final double finalCircle = circleRadius;

        if(mLastLocation != null) {
            LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(latLng));
            mapView.onResume();
            Circle circle = googleMap.addCircle(new CircleOptions()
                    .center(latLng)
                    .strokeColor(getResources().getColor(R.color.colorOn))
                    .strokeWidth(3)
                    .fillColor(getResources().getColor(R.color.colorOnBackTrans))
                    .radius(finalCircle));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setOnMapClickListener(latLng -> {
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(latLng));
            mLastLocation.setLatitude(latLng.latitude);
            mLastLocation.setLongitude(latLng.longitude);
            drawCircle(googleMap);
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

    public void createAlarm() {

        String destination = destinationAuto.getText().toString();
        int volume = ringtoneVolume.getProgress() / 10;
        double lon = mLastLocation.getLongitude();
        double lat = mLastLocation.getLatitude();
        int vibrate = vibrate();
        String rangeString = distanceSpinner.getSelectedItem().toString();
        String rangeType = distanceTypeSpinner.getSelectedItem().toString();

        if(destination.length() <= 30) {

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

            updateWidget();

            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
        }else{
            Toast.makeText(this, getResources().getString(R.string.detail_too_large), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateWidget(){
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, AlarmWidget.class));
        if (appWidgetIds.length > 0) {
            new AlarmWidget().onUpdate(this, appWidgetManager, appWidgetIds);
        }
    }

    public int vibrate() {
        if (vibrateCheck.isChecked()) {
            return 1;
        } else {
            return 0;
        }
    }

    public void passViews(){
        slideUp(mapView);
        slideUp(zoomOutButton);
        slideUp(destinationLabel);
        slideUp(destinationAuto);
        slideUp(radiusLabel);
        slideUp(distanceSpinner);
        slideUp(distanceTypeSpinner);
        slideUp(vibrateLabel);
        slideUp(vibrateCheck);
        slideUp(volumeLabel);
        slideUp(ringtoneVolume);
        slideUp(testButton);
        slideUp(createButton);
    }

    public void slideUp(View v) {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        v.setTranslationY(metrics.heightPixels);
        try {
            v.animate().setInterpolator(new AccelerateInterpolator())
                    .setDuration(600)
                    .translationYBy(-metrics.heightPixels)
                    .start();
        } catch (Exception ex) {

        }
    }


    public void populateSpinners() {
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
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

        final AutocompletePrediction item = mAdapter.getItem(position);
        final String placeId = item.getPlaceId();
        final CharSequence primaryText = item.getPrimaryText(null);

        PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                .getPlaceById(mGoogleApiClient, placeId);
        placeResult.setResultCallback(mUpdatePlaceDetailsCallback);

        Toast.makeText(getApplicationContext(), "Clicked: " + primaryText,
                Toast.LENGTH_SHORT).show();
    }

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                places.release();
                return;
            }
            final Place place = places.get(0);

            destinationAuto.setText(place.getName());
            LatLng autoLatLng = place.getLatLng();
            mLastLocation.setLongitude(autoLatLng.longitude);
            mLastLocation.setLatitude(autoLatLng.latitude);

            mapView.getMapAsync(googleMap -> {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(autoLatLng, 15));
                drawCircle(googleMap);
            });

            places.release();
        }
    };

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("Failed", "To connect");
    }

}