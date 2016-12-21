package com.newwesterndev.gpsalarm;

import android.content.ContentValues;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.newwesterndev.gpsalarm.alarm.AlarmController;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.newwesterndev.gpsalarm.database.AlarmContract.AlarmsEntry.COLUMN_ID;
import static com.newwesterndev.gpsalarm.database.AlarmContract.AlarmsEntry.COLUMN_IS_ACTIVE;
import static com.newwesterndev.gpsalarm.database.AlarmContract.AlarmsEntry.CONTENT_URI;

public class AlarmSoundActivity extends AppCompatActivity {
    @BindView(R.id.music_stop)
    Button musicStop;
    int volume, vibrate;
    long id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_sound);
        ButterKnife.bind(this);

        Intent i = getIntent();
        volume = i.getIntExtra("Vol", 100);
        vibrate = i.getIntExtra("Vib", 1);
        id = i.getLongExtra("Id", 0);

        Log.e("idin", String.valueOf(id));
        Log.e("idin", String.valueOf(volume));
        Log.e("idin", String.valueOf(vibrate));

        final AlarmController alarmController = new AlarmController(this);

        alarmController.playSound(volume, isVibrate(vibrate));

        musicStop.setOnClickListener(view -> {
            alarmController.stopSound();
            alarmController.releasePlayer();
            removeActiveAlarm(id);
            Intent k = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(k);
        });
    }

    public boolean isVibrate(int vib) {
        if (vib == 1) {
            return true;
        } else {
            return false;
        }
    }

    public void removeActiveAlarm(long id){
        String args[] = {String.valueOf(id)};
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_IS_ACTIVE, 0);
        Log.e("in", String.valueOf(id));
        getContentResolver().update(CONTENT_URI, cv, COLUMN_ID, args);
    }
}
