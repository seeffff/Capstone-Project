package com.newwesterndev.gpsalarm.utility;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.newwesterndev.gpsalarm.MainActivity;
import com.newwesterndev.gpsalarm.R;

import java.util.ArrayList;

import static com.newwesterndev.gpsalarm.database.AlarmContract.AlarmsEntry.COLUMN_ID;

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

        public ViewHolder(View itemView){
            super(itemView);

            destinationText = (TextView) itemView.findViewById(R.id.list_destination_text);
            radiusText = (TextView) itemView.findViewById(R.id.list_radius_text);
            vibrateCheck = (CheckBox) itemView.findViewById(R.id.list_vibrate_checkbox);
            ringtoneBar = (SeekBar) itemView.findViewById(R.id.list_ringtone_seekbar);
            activeButton = (Button) itemView.findViewById(R.id.active_button);
            deleteButton = (ImageButton) itemView.findViewById(R.id.delete_button);
        }
    }

    private ArrayList<Alarm> mAlarms;
    private Context mContext;

    public AlarmAdapter(Context context, ArrayList<Alarm> alarms){
        mAlarms = alarms;
        mContext = context;
    }

    private Context getContext(){
        return mContext;
    }

    @Override
    public int getItemCount() {
        return mAlarms.size();
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
        Alarm alarm = mAlarms.get(position);

        Log.e("Alarm details", alarm.getDestination());

        TextView destinationText = holder.destinationText;
        TextView radiusText = holder.radiusText;
        CheckBox vibrateCheck = holder.vibrateCheck;
        SeekBar ringtoneSeek = holder.ringtoneBar;
        Button activeButton = holder.activeButton;
        ImageButton deleteButton = holder.deleteButton;

        destinationText.setText(alarm.getDestination());
        radiusText.setText(Double.toString(alarm.getRange()) + " " + alarm.getRangeType());
        if(alarm.getVibrate() == 1) vibrateCheck.setChecked(true);
        else vibrateCheck.setChecked(false);
        ringtoneSeek.setProgress(alarm.getVolume());

        activeButton.setOnClickListener(view -> {

        });

        deleteButton.setOnClickListener(view -> {
            String args[] = {String.valueOf(alarm.getId())};
            getContext().getContentResolver().delete(CONTENT_URI, COLUMN_ID, args);
            Intent i = new Intent(getContext(), MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(i);
        });
    }
}
