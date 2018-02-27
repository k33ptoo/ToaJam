package com.keeptoo.toajam.authetication;

/**
 * Created by keeptoo on 11/08/2017.
 */

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Class for Shared Preference
 */
public class SessionManager {

    Context context;


    public SessionManager(Context context) {
        this.context = context;
    }

    public void saveLoginDetails(String uuid, String email, String country) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginDetails", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Email", email);
        editor.putString("UUID", uuid);
        editor.putString("Country", country);
        editor.commit();
    }

    public String getEmail() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginDetails", Context.MODE_PRIVATE);
        return sharedPreferences.getString("Email", "");
    }


    public void setCountry(String country) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginDetails", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Country", country);
        editor.apply();

    }

    public String getCountry() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginDetails", Context.MODE_PRIVATE);
        return sharedPreferences.getString("Country", "");
    }


    public boolean isUserLogedOut() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginDetails", Context.MODE_PRIVATE);
        boolean isEmailEmpty = sharedPreferences.getString("Email", "").isEmpty();
        boolean isValidUUID = sharedPreferences.getString("UUID", "").isEmpty();
        return isEmailEmpty || isValidUUID;
    }

    public void logOut() {
        SharedPreferences preferences = context.getSharedPreferences("LoginDetails", 0);
        preferences.edit().clear().apply();
    }
}