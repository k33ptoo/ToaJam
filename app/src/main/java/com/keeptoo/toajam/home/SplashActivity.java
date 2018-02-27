package com.keeptoo.toajam.home;

/**
 * Created by keeptoo on 11/22/2017.
 */

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Start home activity
        startActivity(new Intent(SplashActivity.this, HomeActivity.class), ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
        // close splash activity
        finish();
    }
}