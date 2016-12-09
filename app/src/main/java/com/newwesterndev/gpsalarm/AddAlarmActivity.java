package com.newwesterndev.gpsalarm;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddAlarmActivity extends AppCompatActivity {

    @BindView(R.id.back)
    ImageButton backButton;
    @BindView(R.id.mapView)
    MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alarm);
        ButterKnife.bind(this);

        backButton.setOnClickListener(view -> {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
        });

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {

            @Override
            public void onMapReady(GoogleMap googleMap) {
//                LatLng coordinates = new LatLng(match.LocationLatitude, match.match.LocationLongitude);
//                googleMap.addMarker(new MarkerOptions().position(coordinates));
//                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 15));
                mapView.onResume();
            }
        });
    }
}
