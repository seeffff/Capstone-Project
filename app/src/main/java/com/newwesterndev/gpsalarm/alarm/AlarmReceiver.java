package com.newwesterndev.gpsalarm.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import com.newwesterndev.gpsalarm.AlarmSoundActivity;

public class AlarmReceiver extends BroadcastReceiver{

    int volume, vibrate;
    long id;

    @Override
    public void onReceive(Context context, Intent intent) {

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();

        volume = intent.getIntExtra("Vol", 100);
        vibrate = intent.getIntExtra("Vib", 1);
        id = intent.getLongExtra("Id", 0);

        Intent i = new Intent(context, AlarmSoundActivity.class);
        i.putExtra("Vol", volume);
        i.putExtra("Vib", vibrate);
        i.putExtra("Id", id);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);

        wl.release();
    }
}
