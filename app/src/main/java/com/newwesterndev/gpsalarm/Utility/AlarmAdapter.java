package com.newwesterndev.gpsalarm.utility;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
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

import com.newwesterndev.gpsalarm.MainActivity;
import com.newwesterndev.gpsalarm.R;
import com.newwesterndev.gpsalarm.alarm.MonitorLocationService;
import com.newwesterndev.gpsalarm.database.AlarmContract;
import com.newwesterndev.gpsalarm.database.AlarmLoader;
import com.newwesterndev.gpsalarm.database.AlarmProvider;

import java.util.ArrayList;

import static com.newwesterndev.gpsalarm.database.AlarmContract.AlarmsEntry.COLUMN_ID;
import static com.newwesterndev.gpsalarm.database.AlarmContract.AlarmsEntry.COLUMN_IS_ACTIVE;
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

    private ArrayList<Alarm> mAlarms;
    private Context mContext;
    private Cursor mCursor, mActiveAlarms;

    public AlarmAdapter(Cursor cursor, Context context){
        mCursor = cursor;
        mContext = context;
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
            deleteButton.setBackgroundColor(getContext().getResources().getColor(R.color.colorOnBackground));
            linearList.setBackgroundColor(getContext().getResources().getColor(R.color.colorOnBackground));
            linearList.setElevation(8);
            vibrateCheck.setEnabled(false);
            ringtoneSeek.setEnabled(false);
        }
        ringtoneSeek.setProgress(alarm.getVolume() * 10);

        activeButton.setOnClickListener(view -> {

            String currentState = activeButton.getText().toString();

            if(getActiveAlarms() == 1 && currentState.equals(getContext().getResources().getString(R.string.off))){
                Toast.makeText(getContext(), "Only one alarm can be active at once!", Toast.LENGTH_SHORT).show();
            } else {

                Intent l = new Intent(getContext(), MonitorLocationService.class);

                l.putExtra("Lon", alarm.getLon());
                l.putExtra("Lat", alarm.getLat());
                l.putExtra("Vol", alarm.getVolume());
                l.putExtra("Range", alarm.getRange());
                l.putExtra("Vib", alarm.getVibrate());
                l.putExtra("Id", alarm.getId());

                Log.e("Stuffs", "Lon is " + String.valueOf(alarm.getLon()) + " Lat is " + String.valueOf(alarm.getLat())
                + " volume is " + alarm.getVolume() + " range is " + alarm.getVibrate() + " vib is " + alarm.getVibrate()
                + " id is " + alarm.getId());

                setActive(alarm.getId(), currentState);

                if (currentState.equals(getContext().getResources().getString(R.string.off))) {

                    getContext().startService(l);

                    activeButton.setText(getContext().getResources().getString(R.string.on));
                    activeButton.setBackgroundColor(getContext().getResources().getColor(R.color.colorOn));
                    deleteButton.setBackgroundColor(getContext().getResources().getColor(R.color.colorOnBackground));
                    linearList.setBackgroundColor(getContext().getResources().getColor(R.color.colorOnBackground));
                    linearList.setElevation(8);
                    vibrateCheck.setEnabled(false);
                    ringtoneSeek.setEnabled(false);
                } else {
                    getContext().stopService(l);

                    activeButton.setText(getContext().getResources().getString(R.string.off));
                    activeButton.setBackgroundColor(getContext().getResources().getColor(R.color.colorAccent));
                    deleteButton.setBackgroundColor(getContext().getResources().getColor(R.color.colorLightGray));
                    linearList.setBackgroundColor(getContext().getResources().getColor(R.color.colorLightGray));
                    linearList.setElevation(0);
                    vibrateCheck.setEnabled(true);
                    ringtoneSeek.setEnabled(true);
                }
            }
        });

        deleteButton.setOnClickListener(view -> {
            String args[] = {String.valueOf(alarm.getId())};
            getContext().getContentResolver().delete(CONTENT_URI, COLUMN_ID, args);
            Intent i = new Intent(getContext(), MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(i);
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
}
