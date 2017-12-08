package com.keeptoo.toajam.geoupdates.utililies;

/**
 * Created by keeptoo on 11/20/2017.
 */

import android.location.Location;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A helper class to look up location by IP.
 */
public abstract class GeoIpLocation {
    private static final String TAG = GeoIpLocation.class.getSimpleName();

    /**
     * The URL of the geo location API endpoint.
     */
    private static final String GEO_IP_URL = "http://ip-api.com/json";

    /**
     * Makes a request to the geo location API and returns the current location or {@code null} on
     * error.
     */
    public static Location getLocation() {
        String response = Network.get(GEO_IP_URL);
        if (response == null) {
            Log.e(TAG, "Empty response.");
            return null;
        }

        // Parse the latitude and longitude from the response JSON.
        try {
            JSONObject responseJson = new JSONObject(response);
            double latitude = responseJson.getDouble("lat");
            double longitude = responseJson.getDouble("lon");
            Location location = new Location("");
            location.setLatitude(latitude);
            location.setLongitude(longitude);
            return location;
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse geo location JSON.");
            return null;
        }
    }
}