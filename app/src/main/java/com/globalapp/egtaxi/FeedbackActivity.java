package com.globalapp.egtaxi;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.json.GenericJson;
import com.kinvey.android.AsyncAppData;
import com.kinvey.android.Client;
import com.kinvey.java.core.KinveyClientCallback;

import java.util.Locale;

public class FeedbackActivity extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    public static String driverName = "null";

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
        setContentView(R.layout.activity_feedback);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.feedback_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.menu_send) {
            sendFeedback();
        }

        return super.onOptionsItemSelected(item);
    }

    private void sendFeedback() {
        Client mKinveyClient = new Client.Builder(getApplicationContext()).build();
        TextView txtMessage = (TextView) findViewById(R.id.txtFeedbackMessage);
        RatingBar rate = (RatingBar) findViewById(R.id.ratingBar);
        GenericJson appdata = new GenericJson();
        appdata.put("username", sharedPreferences.getString("UserName", ""));
        appdata.put("Message", txtMessage.getText().toString());
        appdata.put("Rate", rate.getRating());
        appdata.put("Driver", driverName);

        AsyncAppData<GenericJson> Feed = mKinveyClient.appData("Feedbacks", GenericJson.class);
        Feed.save(appdata, new KinveyClientCallback<GenericJson>() {
            @Override
            public void onSuccess(GenericJson genericJson) {
                Toast.makeText(FeedbackActivity.this, "Thanks", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Throwable throwable) {

            }
        });
    }
}
