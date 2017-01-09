package com.newwesterndev.gpsalarm.widget;

import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Binder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.newwesterndev.gpsalarm.MainActivity;
import com.newwesterndev.gpsalarm.R;
import com.newwesterndev.gpsalarm.alarm.MonitorLocationService;
import com.newwesterndev.gpsalarm.database.AlarmContract;
import com.newwesterndev.gpsalarm.database.AlarmLoader;
import com.newwesterndev.gpsalarm.database.AlarmProvider;

public class AlarmWidgetService extends RemoteViewsService {

    public final String LOG_TAG = AlarmWidgetService.class.getSimpleName();
    private static final String[] ALARM_COLUMNS = AlarmContract.AlarmsEntry.allColumns;
    private static final String PROVIDER_NAME = "newwesterndev.alarmdatabase.alarms";
    private static final Uri CONTENT_URI = Uri.parse("content://" + PROVIDER_NAME + "/alarms");

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {

            }

            @Override
            public void onDataSetChanged() {
                if(data != null){
                    data.close();
                }

                final long identityToken = Binder.clearCallingIdentity();

                data = getContentResolver().query(CONTENT_URI,
                        ALARM_COLUMNS,
                        null, null, null);

                if(data == null){
                    Log.e("Cursor", "is null");
                } else {
                    Log.e("Cursor", "aint null");
                }

                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
                public void onDestroy() {
                    if(data != null){
                        data.close();
                        data = null;
                    }
                }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)){
                    return null;
                }

                String label = "";
                int active = 0;

                if(data.moveToPosition(position)) {
                    label = data.getString(
                            data.getColumnIndex(AlarmContract.AlarmsEntry.COLUMN_ALARM_DESTINATION)
                    );

                    active = data.getInt(
                            data.getColumnIndex(AlarmContract.AlarmsEntry.COLUMN_IS_ACTIVE)
                    );
                }

                RemoteViews views = new RemoteViews(getPackageName(), R.layout.alarm_widget_item);
                views.setTextViewText(R.id.widget_text, label);
                views.setTextColor(R.id.widget_text, getResources().getColor(R.color.colorBlack));

                if(active == 1){
                    views.setViewVisibility(R.id.off_widget_button, View.GONE);
                    views.setViewVisibility(R.id.on_widget_button, View.VISIBLE);
                    views.setInt(R.id.widget_item, "setBackgroundResource", R.drawable.list_item_on_background);
                }

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, intent, 0);

                views.setOnClickPendingIntent(R.id.off_widget_button, pendingIntent);


                return views;
            }

            public void startAlarm(){
                Intent intent = new Intent(getApplicationContext(), MonitorLocationService.class);
                PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, intent, 0);



            }

            public void stopAlarm(){

            }

            @Override
            public RemoteViews getLoadingView() {
                return null;
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int i) {
                if(data.moveToPosition(i))
                    return data.getLong(
                            data.getColumnIndex(AlarmContract.AlarmsEntry.COLUMN_ID));
                return i;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
