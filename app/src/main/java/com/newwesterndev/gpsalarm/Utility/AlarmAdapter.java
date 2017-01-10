package com.newwesterndev.gpsalarm.utility;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.TransitionDrawable;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.newwesterndev.gpsalarm.MainActivity;
import com.newwesterndev.gpsalarm.R;
import com.newwesterndev.gpsalarm.alarm.MonitorLocationService;
import com.newwesterndev.gpsalarm.database.AlarmContract;
import com.newwesterndev.gpsalarm.database.AlarmLoader;
import com.newwesterndev.gpsalarm.database.AlarmProvider;
import com.newwesterndev.gpsalarm.widget.AlarmWidget;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;
import static com.newwesterndev.gpsalarm.database.AlarmContract.AlarmsEntry.COLUMN_ID;
import static com.newwesterndev.gpsalarm.database.AlarmContract.AlarmsEntry.COLUMN_IS_ACTIVE;
import static com.newwesterndev.gpsalarm.database.AlarmContract.AlarmsEntry.COLUMN_VIBRATE;
import static com.newwesterndev.gpsalarm.database.AlarmContract.AlarmsEntry.COLUMN_VOLUME;
import static com.newwesterndev.gpsalarm.database.AlarmContract.AlarmsEntry.CONTENT_URI;
import static com.newwesterndev.gpsalarm.database.AlarmContract.AlarmsEntry.TABLE_NAME;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.ViewHolder>{

    private static final String PROVIDER_NAME = "newwesterndev.alarmdatabase.alarms";
    private static final Uri CONTENT_URI = Uri.parse("content://" + PROVIDER_NAME + "/alarms");

    public static class ViewHolder extends RecyclerView.ViewHolder{

        public TextView destinationText;
        public TextView radiusText;
        public CheckBox vibrateCheck;
        public SeekBar ringtoneBar;
        public Button activeButton;
        public ImageButton deleteButton;
        public LinearLayout linearLayout;

        public ViewHolder(View itemView){
            super(itemView);

            destinationText = (TextView) itemView.findViewById(R.id.list_destination_text);
            radiusText = (TextView) itemView.findViewById(R.id.list_radius_text);
            vibrateCheck = (CheckBox) itemView.findViewById(R.id.list_vibrate_checkbox);
            ringtoneBar = (SeekBar) itemView.findViewById(R.id.list_ringtone_seekbar);
            activeButton = (Button) itemView.findViewById(R.id.active_button);
            deleteButton = (ImageButton) itemView.findViewById(R.id.delete_button);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.linear_list);
        }
    }

    private Context mContext;
    private Cursor mCursor;
    private InterstitialAd mInterstitialAd;
    public static final String ALARM_ID_PREFERENCES = "AlarmId";

    public AlarmAdapter(Cursor cursor, Context context){
        mCursor = cursor;
        mContext = context;

        mInterstitialAd = new InterstitialAd(getContext());
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        requestNewInterstitial();
    }

    private Context getContext(){
        return mContext;
    }

    @Override
    public long getItemId(int position){
        mCursor.moveToPosition(position);
        return mCursor.getLong(AlarmLoader.Query.COLUMN_ID);
    }

    @Override
    public AlarmAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View alarmView = inflater.inflate(R.layout.alarm_list_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(alarmView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(AlarmAdapter.ViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        long id = mCursor.getLong(AlarmLoader.Query.COLUMN_ID);
        double range = mCursor.getDouble(AlarmLoader.Query.COLUMN_ALARM_RANGE);
        String type = mCursor.getString(AlarmLoader.Query.COLUMN_ALARM_RANGE_TYPE);
        String destination = mCursor.getString(AlarmLoader.Query.COLUMN_ALARM_DESTINATION);
        int active = mCursor.getInt(AlarmLoader.Query.COLUMN_IS_ACTIVE);
        int volume = mCursor.getInt(AlarmLoader.Query.COLUMN_VOLUME);
        int vibrate = mCursor.getInt(AlarmLoader.Query.COLUMN_VIBRATE);
        double lon = mCursor.getDouble(AlarmLoader.Query.COLUMN_LON);
        double lat = mCursor.getDouble(AlarmLoader.Query.COLUMN_LAT);

        Alarm alarm = new Alarm(destination, active, volume, vibrate, lon, lat, range, type, id);

        Log.e("Id's in adapter", String.valueOf(alarm.getId()));

        TextView destinationText = holder.destinationText;
        TextView radiusText = holder.radiusText;
        CheckBox vibrateCheck = holder.vibrateCheck;
        SeekBar ringtoneSeek = holder.ringtoneBar;
        Button activeButton = holder.activeButton;
        ImageButton deleteButton = holder.deleteButton;
        LinearLayout linearList = holder.linearLayout;

        destinationText.setText(alarm.getDestination());
        radiusText.setText(Double.toString(alarm.getRange()) + " " + alarm.getRangeType());
        if(alarm.getVibrate() == 1) vibrateCheck.setChecked(true);
        else vibrateCheck.setChecked(false);

        if(alarm.getIsActive() == 1){
            activeButton.setText(getContext().getResources().getString(R.string.on));
            activeButton.setBackgroundColor(getContext().getResources().getColor(R.color.colorOn));
            activeBackground(linearList, true);
            linearList.setElevation(8);
            vibrateCheck.setEnabled(false);
            ringtoneSeek.setEnabled(false);
        }
        ringtoneSeek.setProgress(alarm.getVolume() * 10);

        activeButton.setOnClickListener(view -> {

            boolean locationEnabled = testLocationService();

            String currentState = activeButton.getText().toString();

            if(locationEnabled) {
                if (getActiveAlarms() == 1 && currentState.equals(getContext().getResources().getString(R.string.off))) {
                    Toast.makeText(getContext(), getContext().getResources().getString(R.string.multiple_alarms), Toast.LENGTH_SHORT).show();
                } else {
                    Intent l = new Intent(getContext(), MonitorLocationService.class);

                    l.putExtra("Dest", alarm.getDestination());
                    l.putExtra("Lon", alarm.getLon());
                    l.putExtra("Lat", alarm.getLat());
                    l.putExtra("Vol", alarm.getVolume());
                    l.putExtra("Range", alarm.getRange());
                    l.putExtra("Vib", alarm.getVibrate());

                    updateVolumeVibrate(alarm.getId(), alarm.getVolume(), alarm.getVibrate());
                    setActive(alarm.getId(), currentState);
                    updateWidget();

                    if (currentState.equals(getContext().getResources().getString(R.string.off))) {

                        setAlarmDetails(alarm.getId(), alarm.getVolume(), alarm.getVibrate());
                        getContext().startService(l);

                        activeButton.setText(getContext().getResources().getString(R.string.on));
                        activeButton.setBackgroundColor(getContext().getResources().getColor(R.color.colorOn));
                        activeBackground(linearList, true);
                        linearList.setElevation(8);
                        vibrateCheck.setEnabled(false);
                        ringtoneSeek.setEnabled(false);
                    } else {
                        adHandler();
                        getContext().stopService(l);

                        activeButton.setText(getContext().getResources().getString(R.string.off));
                        activeButton.setBackgroundColor(getContext().getResources().getColor(R.color.colorAccent));
                        activeBackground(linearList, false);
                        linearList.setElevation(0);
                        vibrateCheck.setEnabled(true);
                        ringtoneSeek.setEnabled(true);
                    }
                }
            } else {
                Toast.makeText(getContext(), getContext().getResources().getString(R.string.location_disabled), Toast.LENGTH_LONG).show();
            }
        });


        deleteButton.setOnClickListener(view -> {
            deleteDialog(alarm);
        });
    }

    public int getActiveAlarms(){
        int activeCount = 0;
        Cursor mActiveAlarms = getContext().getContentResolver().query(CONTENT_URI,
                new String[] {COLUMN_IS_ACTIVE}, COLUMN_IS_ACTIVE + " = ?", new String[] {"1"}, null);
        if(mActiveAlarms.getCount() >= 1) {
            mActiveAlarms.moveToFirst();
            while (!mActiveAlarms.isAfterLast()) {
                int activeState = mActiveAlarms.getInt(0);
                if (activeState == 1) {
                    activeCount++;
                }
                mActiveAlarms.moveToNext();
            }
        }
        mActiveAlarms.close();
        return activeCount;
    }

    private void setAlarmDetails(long id, int volume, int vibrate){
        SharedPreferences prefs = getContext().getSharedPreferences(ALARM_ID_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putLong("Id", id);
        prefsEditor.putInt("Vol", volume);
        prefsEditor.putInt("Vib", vibrate);
        prefsEditor.apply();
    }

    private void updateVolumeVibrate(long id, int volume, int vibrate){
        String args[] = {String.valueOf(id)};
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_VOLUME, volume);
        cv.put(COLUMN_VIBRATE, vibrate);
        getContext().getContentResolver().update(CONTENT_URI, cv, COLUMN_ID, args);
    }

    public void adHandler(){
            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
                mInterstitialAd = new InterstitialAd(getContext());
                mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
                requestNewInterstitial();
            }
    }

    public boolean testLocationService() {
        LocationManager lm = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = false;
        boolean networkEnabled = false;
        boolean bothEnabled = false;

        try {
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if(gpsEnabled && networkEnabled){
            bothEnabled = true;
        }

        return bothEnabled;
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    public void setActive(long id, String currentState){
        String args[] = {String.valueOf(id)};
        ContentValues cv = new ContentValues();
        if(currentState.equals("OFF")){
            Log.e("Inserted", "1");
            cv.put(COLUMN_IS_ACTIVE, 1);
        }else{
            Log.e("Inserted", "0");
            cv.put(COLUMN_IS_ACTIVE, 0);
        }
        getContext().getContentResolver().update(CONTENT_URI, cv, COLUMN_ID, args);
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();

        mInterstitialAd.loadAd(adRequest);
    }

    private void deleteDialog(Alarm alarm){

        new android.app.AlertDialog.Builder(getContext())
                .setTitle(R.string.dialog_delete_title)
                .setMessage(R.string.dialog_delete_text)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        String args[] = {String.valueOf(alarm.getId())};
                        getContext().getContentResolver().delete(CONTENT_URI, COLUMN_ID, args);
                        Intent j = new Intent(getContext(), MainActivity.class);
                        j.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getContext().startActivity(j);

                        Intent k = new Intent(getContext(), MainActivity.class);
                        k.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getContext().startActivity(k);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        return;
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void updateWidget(){
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getContext());
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(getContext(), AlarmWidget.class));
        if (appWidgetIds.length > 0) {
            new AlarmWidget().onUpdate(getContext(), appWidgetManager, appWidgetIds);
        }
    }

    public void activeBackground(LinearLayout li, boolean isNowActive){

        TransitionDrawable transition = (TransitionDrawable) li.getBackground();
        if(isNowActive) {
            transition.startTransition(250);
        }else{
            transition.reverseTransition(250);
        }
    }
}
