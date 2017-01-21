package com.globalapp.egtaxi;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ComingActivity extends Activity {
    public static String Name = "", Phone = "", image = "", car = "", state = "";
    public static TextView txtState, txtCounter;
    private  int secs, mins;
    AQuery aQuery;
    public static boolean isFinished = false;
    SharedPreferences sharedPreferences;
    CountDownTimer downTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences("TaxiShared", Context.MODE_PRIVATE);

        String languageToLoad = sharedPreferences.getString("language", "en");
        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
        setContentView(R.layout.activity_coming);
        aQuery = new AQuery(this);
        TextView txtDriverName = (TextView) findViewById(R.id.Coming_DriverName);
        TextView txtDriverPhone = (TextView) findViewById(R.id.Coming_driverPhone);
        txtState = (TextView) findViewById(R.id.txtState);
        txtState.setText(state);
        TextView txtCar = (TextView) findViewById(R.id.txtCarNo);
        txtCounter = (TextView) findViewById(R.id.txtCounter);
        aQuery.id(R.id.Coming_DriverImage).image(image, true, true);
        txtDriverName.setText(Name);
        txtDriverPhone.setText(Phone);
        txtCar.setText(car);
        loadState();


    }


    public void call(View view) {
        Intent phoneCallIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + Phone));
        startActivity(phoneCallIntent);
    }

    @Override
    public void onBackPressed() {
        if (isFinished) {
            super.onBackPressed();
            isFinished = false;
        }

    }

    private void loadState() {
        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {

                        String text = intent.getStringExtra("text");
                        txtState.setText(text);

                        if (text.equals(getString(R.string.arrived))) {
                            counter();
                        } else if (text.equals(getString(R.string.on_trip))) {
                            downTimer.cancel();
                            txtCounter.setText("");
                        }


                    }
                }, new IntentFilter(GCMService.ACTION)
        );
    }


    private void counter() {

        downTimer = new CountDownTimer(300000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                secs = (int) (millisUntilFinished / 1000);
                mins = secs / 60;
                mins = mins % 60;
                secs = secs % 60;

                String time = String.format("%02d", mins) + ":" + String.format("%02d", secs);
                txtCounter.setText(time);
            }

            @Override
            public void onFinish() {
                txtCounter.setText("");
            }
        };
        downTimer.start();
    }

}
