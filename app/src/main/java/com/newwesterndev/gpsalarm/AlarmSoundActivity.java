package com.newwesterndev.gpsalarm;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.newwesterndev.gpsalarm.alarm.AlarmController;
import com.newwesterndev.gpsalarm.widget.AlarmWidget;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.newwesterndev.gpsalarm.database.AlarmContract.AlarmsEntry.COLUMN_ID;
import static com.newwesterndev.gpsalarm.database.AlarmContract.AlarmsEntry.COLUMN_IS_ACTIVE;
import static com.newwesterndev.gpsalarm.database.AlarmContract.AlarmsEntry.CONTENT_URI;
import static com.newwesterndev.gpsalarm.utility.AlarmAdapter.ALARM_ID_PREFERENCES;

public class AlarmSoundActivity extends AppCompatActivity {
    @BindView(R.id.music_stop) Button musicStop;
    private InterstitialAd mInterstitialAd;
    SharedPreferences mPrefs;
    int volume, vibrate;
    long id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_sound);
        ButterKnife.bind(this);

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        requestNewInterstitial();

        mPrefs = this.getSharedPreferences(ALARM_ID_PREFERENCES, MODE_PRIVATE);
        volume = mPrefs.getInt("Vol", 0);
        vibrate = mPrefs.getInt("Vib", 0);
        id = mPrefs.getLong("Id", 0);

        final AlarmController alarmController = new AlarmController(this);

        alarmController.playSound(volume, isVibrate(vibrate));

        musicStop.setOnClickListener(view -> {
            alarmController.stopSound();
            alarmController.releasePlayer();
            removeActiveAlarm(id);
            if(mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            }else{
                Log.e("AdLoadedSound", "Not loaded");
            }
            Intent k = new Intent(getApplicationContext(), MainActivity.class);
            updateWidget();

            k.putExtra("KillMe", "KillMe");
            startActivity(k);
        });
    }

    private void updateWidget(){
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, AlarmWidget.class));
        if (appWidgetIds.length > 0) {
            new AlarmWidget().onUpdate(this, appWidgetManager, appWidgetIds);
        }
    }

    public boolean isVibrate(int vib) {
        if (vib == 1) {
            return true;
        } else {
            return false;
        }
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();

        mInterstitialAd.loadAd(adRequest);
    }

    public void removeActiveAlarm(long id){
        String args[] = {String.valueOf(id)};
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_IS_ACTIVE, 0);
        getContentResolver().update(CONTENT_URI, cv, COLUMN_ID, args);
    }
}
