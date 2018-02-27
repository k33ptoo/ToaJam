package com.keeptoo.toajam.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.view.View;
import android.widget.Toast;

/**
 * Created by keeptoo on 11/09/2017.
 */

public class InteractionUtils extends Activity {

    public InteractionUtils() {

    }

    //show snackbar
    public void showSnackBar(View view, String msg, int duration) {
        Snackbar.make(view, msg, duration).show();
    }

    // show Toast
    public void showToast(Context context, String msg, int duration) {
        Toast.makeText(context, msg, duration).show();
    }


    public boolean validationForName(TextInputEditText textInputEditText) {
        if (textInputEditText.getText().toString().isEmpty()) {
            textInputEditText.setError("Enter Name");

            return false;
        } else {
            // textInputEditText.setErrorEnabled(false);
        }
        return true;
    }

    public boolean validationForLastName(TextInputEditText textInputEditText) {
        if (textInputEditText.getText().toString().isEmpty()) {
            textInputEditText.setError("Enter Last Name");

            return false;
        } else {
            //  textInputEditText.setErrorEnabled(false);
        }
        return true;
    }

    public boolean validationForMobile(TextInputEditText textInputEditText) {
        if (textInputEditText.getText().toString().isEmpty() || textInputEditText.getText().length() < 10) {
            textInputEditText.setError("Enter Valid Mobile");

            return false;
        } else {
        }
        return true;
    }


    public boolean getFirstTimeRun(Context context, String name) {
        SharedPreferences prefs = context.getSharedPreferences(name, MODE_PRIVATE);
        boolean firstTimeRun = prefs.getBoolean("firstRun", true);
        return firstTimeRun;
    }

    public void storeFirstTimeRun(Context context, String name) {
        SharedPreferences prefs = context.getSharedPreferences(name, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("firstRun", false);
        editor.apply();
    }

}
