package com.newwesterndev.gpsalarm.utility;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GetPlacesAsync extends AsyncTask<String, Void, ArrayList<PlaceSuggestion>>{

    private Context mContext;
    private Location mLocation;
    private RequestQueue requestQueue;
    private ShowPlacePicker showPlacePicker;
    ArrayList<PlaceSuggestion> mPlaceSuggestions = new ArrayList<>();

    public GetPlacesAsync(Context context, Location location){
        mContext = context;
        mLocation = location;
    }

    @Override
    protected ArrayList<PlaceSuggestion> doInBackground(String... string) {

        String place = string[0];
        requestQueue = Volley.newRequestQueue(mContext);
        fetchPosts(place);

        return mPlaceSuggestions;
    }

    private void setPlacesFromJson(String jsonString) throws JSONException{

        final String PLA_ITEMS = "results";
        final String PLA_ADDRESS = "formatted_address";
        final String PLA_GEOMETRY = "geometry";
        final String PLA_LOCATION = "location";
        final String PLA_LAT = "lat";
        final String PLA_LNG = "lng";
        final String PLA_ICON_URL = "icon";
        final String PLA_NAME = "name";

        JSONObject jsonObject = new JSONObject(jsonString);
        JSONArray placesArray = jsonObject.getJSONArray(PLA_ITEMS);

        for(int i = 0; i < placesArray.length(); i++){
            JSONObject place = placesArray.getJSONObject(i);
            JSONObject geoObject = place.getJSONObject(PLA_GEOMETRY);
            JSONObject locObject = geoObject.getJSONObject(PLA_LOCATION);

            mPlaceSuggestions.add(new PlaceSuggestion(
                    place.getString(PLA_NAME),
                    place.getString(PLA_ADDRESS),
                    Double.parseDouble(locObject.getString(PLA_LAT)),
                    Double.parseDouble(locObject.getString(PLA_LNG)),
                    place.getString(PLA_ICON_URL)
            ));
        }

        showPlacePicker = new ShowPlacePicker(mContext, mPlaceSuggestions);
        showPlacePicker.show();
    }

    public void closeDialog(){
        showPlacePicker.dismiss();
    }

    private void fetchPosts(String place) {

        String placesApiKey = "";

        try {
            ApplicationInfo ai = mContext.getPackageManager().getApplicationInfo(mContext.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            placesApiKey = bundle.getString("com.google.android.geo.API_KEY");
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("tag", "Failed to load meta-data, NameNotFound: " + e.getMessage());
        } catch (NullPointerException e) {
            Log.e("tag", "Failed to load meta-data, NullPointer: " + e.getMessage());
        }

        if(placesApiKey.length() > 1) {
            String ENDPOINT = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=";
            ENDPOINT = ENDPOINT + formatInput(place);
            ENDPOINT = ENDPOINT + "&location=" + Double.toString(mLocation.getLatitude()) + "," + Double.toString(mLocation.getLongitude());
            ENDPOINT = ENDPOINT + "&radius=10000&key=" + placesApiKey;

            Log.e("ENDPOINT", ENDPOINT);

            StringRequest request = new StringRequest(Request.Method.GET, ENDPOINT, onPostsLoaded, onPostsError);
            requestQueue.add(request);
        }

    }

    private String formatInput(String input){
        String output = "";

        for(int i = 0; i < input.length(); i++){
            if(input.charAt(i) == ' '){
                output = output + "+";
            }else {
                output = output + input.charAt(i);
            }
        }

        return output;
    }

    private final Response.Listener<String> onPostsLoaded = response -> {
        Log.e("PostActivity", response);
        try {
            setPlacesFromJson(response);
        } catch (JSONException je){

        }
    };

    private final Response.ErrorListener onPostsError = error -> {

    };
}
