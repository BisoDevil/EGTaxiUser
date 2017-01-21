package com.globalapp.egtaxi;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.kinvey.android.Client;


public class SplashActivity extends Activity {
    boolean isAppInstalled = false;
    SharedPreferences sharedPreferences;
    boolean statusOfGPS;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        sharedPreferences = getSharedPreferences("TaxiShared", Context.MODE_PRIVATE);
        isAppInstalled = sharedPreferences.getBoolean("isAppInstalled", false);

        //  create short code


    }

    @Override
    protected void onStart() {
        super.onStart();
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
         Location Loc = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (Loc == null) {
            Loc = manager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        }
        int SPLASH_DISPLAY_LENGTH = 3000;
        final Location finalLoc = Loc;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                if (finalLoc != null) {

                    justGo();
                } else {
                    Intent intent = new Intent(
                            Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            }
        }, SPLASH_DISPLAY_LENGTH);
    }

    private void justGo() {


        final Client mKinveyClient = new Client.Builder(getApplicationContext()).build();
        if (!isAppInstalled) {
            Intent intro = new Intent(getApplicationContext(), IntroActivity.class);
            startActivity(intro);

        } else {
            if (mKinveyClient.user().isUserLoggedIn()) {
                Intent mainIntent = new Intent(SplashActivity.this, MapActivity.class);
                SplashActivity.this.startActivity(mainIntent);
                SplashActivity.this.finish();
            } else {
                Intent mainIntent = new Intent(SplashActivity.this, UserActivity.class);
                SplashActivity.this.startActivity(mainIntent);
                SplashActivity.this.finish();
            }
        }
                /* Create an Intent that will start the Menu-Activity. */

    }
}

