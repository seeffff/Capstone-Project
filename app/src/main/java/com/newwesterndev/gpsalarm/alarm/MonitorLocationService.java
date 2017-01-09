package com.newwesterndev.gpsalarm.alarm;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.newwesterndev.gpsalarm.MainActivity;
import com.newwesterndev.gpsalarm.R;


public class MonitorLocationService extends Service {
    private static final String TAG = "GPS_SERVICE";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final int LOCATION_DISTANCE = 0;
    private static Location alarmStop = new Location("alarm stop");
    private static double alarmRange;
    private static String alarmDestination;
    private Context mContext;
    private NotificationManager mNotificationManager;

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public LocationListener(String provider) {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.e(TAG, "onLocationChanged: " + location);

            float distanceToGo = location.distanceTo(alarmStop);

            float kilometersToGo = distanceToGo/1000;
            float kmToMi = (float)0.621371;

            float milesToGo = kilometersToGo/kmToMi;

            if (milesToGo <= alarmRange){

                AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                int alarmType = AlarmManager.ELAPSED_REALTIME_WAKEUP;
                long timeOrLengthOfWait = 1000;

                Intent i = new Intent(getApplicationContext(), AlarmReceiver.class);
                PendingIntent alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, i, 0);
                alarmManager.set(alarmType, timeOrLengthOfWait, alarmIntent);

                onDestroy();
            }

            mLastLocation.set(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("start", "started");
        mContext = this;

        double lon = intent.getDoubleExtra("Lon", 0);
        double lat = intent.getDoubleExtra("Lat", 0);
        alarmRange = intent.getDoubleExtra("Range", 0);
        alarmDestination = intent.getStringExtra("Dest");

        if(alarmDestination == null){
            alarmDestination = "null";
        }

        alarmStop.setLatitude(lat);
        alarmStop.setLongitude(lon);

        showNotification();

        Log.e(TAG, "onStartCommand");
        onCreate();
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");
        initializeLocationManager();

        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
            Log.e("GPS", " " + Integer.toString(LOCATION_INTERVAL) + " " + Integer.toString(LOCATION_DISTANCE));
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
            Log.e("Network", " " + Integer.toString(LOCATION_INTERVAL) + " " + Integer.toString(LOCATION_DISTANCE));
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }

        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return  ;
        }

        Location location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if(location != null)
        Log.e("Location", "Lat " + Double.toString(location.getLatitude()) + "Long " + Double.toString(location.getLongitude()));

    }

    @Override
    public void onDestroy() {
        mNotificationManager.cancel(1);
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (SecurityException ex) {
                    Log.i(TAG, "fail to remove location listeners, ignore", ex);
                }
            }
        }
        super.onDestroy();
    }

    private void showNotification() {
        mNotificationManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

        Notification.Builder builder = new Notification.Builder(this);

        builder.setAutoCancel(false);
        builder.setContentTitle(getResources().getString(R.string.app_name));
        builder.setContentText(getResources().getString(R.string.notif_text) + " " +
        alarmDestination + " " + getResources().getString(R.string.notif_text_two));
        builder.setSmallIcon(R.drawable.ic_stat_notify_icon);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
        builder.setContentIntent(pendingIntent);
        builder.setOngoing(true);
        builder.build();

        Notification notification = builder.getNotification();
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        mNotificationManager.notify(1, notification);
    }

    private void initializeLocationManager() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        }
    }
}

