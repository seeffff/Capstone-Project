package com.newwesterndev.gpsalarm.alarm;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.provider.Settings;
import android.widget.Toast;
import java.io.IOException;

public class AlarmController {

    private Context mContext;
    private MediaPlayer mMediaPlayer;
    private AudioManager mAudioManager;
    private int mUserVolume;
    private Vibrator mVibrator;

    public AlarmController(Context c) {
        this.mContext = c;
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mUserVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        mVibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        mMediaPlayer = new MediaPlayer();

    }
    public void playSound(int volume, boolean vibrate){

        Uri alarmSound = null;
        Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        Uri ringtone = Settings.System.DEFAULT_RINGTONE_URI;

        try{
            alarmSound = ringtone;
        }catch(Exception e){
            alarmSound = ringtoneUri;
        }
        finally{
            if(alarmSound == null){
                alarmSound = ringtoneUri;
            }
        }

        try {
            if(!mMediaPlayer.isPlaying()){
                mMediaPlayer.setDataSource(mContext, alarmSound);
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                mMediaPlayer.setLooping(true);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            }

            if(vibrate) {
                long pattern[] = {60, 120, 180, 240, 300, 360, 420, 480};
                for (int i = 0; i < 100; i++) {
                    mVibrator.vibrate(pattern, 1);
                }
            }


        } catch (IOException e) {
            Toast.makeText(mContext, "Your alarm sound was unavailable.", Toast.LENGTH_LONG).show();

        }
        mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, volume, AudioManager.FLAG_ALLOW_RINGER_MODES);

    }

    public void stopSound(){
        mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, mUserVolume, AudioManager.FLAG_ALLOW_RINGER_MODES);
        mMediaPlayer.stop();
        mMediaPlayer.reset();
        mVibrator.cancel();

    }
    public void releasePlayer(){
        mMediaPlayer.release();
    }
    
}
