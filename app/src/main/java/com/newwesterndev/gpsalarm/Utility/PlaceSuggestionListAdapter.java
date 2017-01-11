package com.newwesterndev.gpsalarm.utility;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.newwesterndev.gpsalarm.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PlaceSuggestionListAdapter extends ArrayAdapter<PlaceSuggestion> implements ListAdapter {

    public static String BROADCAST_ACTION = "com.newwesterndev.gpsalarm.suggestion";

    public static class ViewHolder {
        ImageView placeIcon;
        TextView placeName;
        TextView placeAddress;
        LinearLayout suggestionItem;
    }

    public PlaceSuggestionListAdapter(Context context, ArrayList<PlaceSuggestion> suggestions) {
        super(context, 0, suggestions);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final PlaceSuggestion suggestion = getItem(position);

        ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();

            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.place_list_item, parent, false);

            viewHolder.suggestionItem = (LinearLayout) convertView.findViewById(R.id.suggestion_list_item);
            viewHolder.placeIcon = (ImageView) convertView.findViewById(R.id.place_image);
            viewHolder.placeName = (TextView) convertView.findViewById(R.id.place_name);
            viewHolder.placeAddress = (TextView) convertView.findViewById(R.id.place_address);

            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (suggestion.getIconUrl() != null) {
            Picasso.with(getContext()).load(suggestion.getIconUrl()).into(viewHolder.placeIcon);
        }

        viewHolder.placeName.setText(suggestion.getPlaceName());
        viewHolder.placeAddress.setText(suggestion.getAddress());

        viewHolder.suggestionItem.setOnClickListener(view -> {
            sendBroadcast(suggestion.getPlaceName(), suggestion.getLat(), suggestion.getLon());
        });

        return convertView;
    }

    public void sendBroadcast(String name, double lat, double lon){
        Intent broadcast = new Intent();
        broadcast.setAction("getSuggestions");
        broadcast.putExtra("Name", name);
        broadcast.putExtra("Lat", lat);
        broadcast.putExtra("Lon", lon);
        getContext().sendBroadcast(broadcast);
    }

}
