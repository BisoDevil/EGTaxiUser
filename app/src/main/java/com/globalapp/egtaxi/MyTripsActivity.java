package com.globalapp.egtaxi;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.json.GenericJson;
import com.kinvey.android.AsyncAppData;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.android.offline.SqlLiteOfflineStore;
import com.kinvey.java.Query;
import com.kinvey.java.cache.CachePolicy;
import com.kinvey.java.cache.InMemoryLRUCache;
import com.kinvey.java.offline.OfflinePolicy;

import java.util.Locale;

public class MyTripsActivity extends AppCompatActivity {
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPreferences = getSharedPreferences("TaxiShared", Context.MODE_PRIVATE);

        String languageToLoad = sharedPreferences.getString("language", "en");
        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_trips);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Client mKinveyClient = new Client.Builder(this.getApplicationContext()).build();

        Query query = new Query();
        query.equals("customer_phone", sharedPreferences.getString("PhoneNumber", ""));
        AsyncAppData<GenericJson> appData = mKinveyClient.appData("Payments", GenericJson.class);
        appData.setCache(new InMemoryLRUCache(), CachePolicy.NETWORKFIRST);
        appData.setOffline(OfflinePolicy.ONLINE_FIRST, new SqlLiteOfflineStore<GenericJson>(getApplicationContext()));
        appData.get(query, new KinveyListCallback<GenericJson>() {
            @Override
            public void onSuccess(GenericJson[] genericJsons) {
                if (genericJsons != null) {
                    SetupListView(genericJsons);
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                Toast.makeText(MyTripsActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
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
        }

        return super.onOptionsItemSelected(item);
    }

    private void SetupListView(final GenericJson[] genericJsons) {

        ListView list = (ListView) findViewById(R.id.list_trips);
        list.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return genericJsons.length;
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater linflater = getLayoutInflater();
                View view1 = linflater.inflate(R.layout.trips_ticket, null);
                TextView trip_username = (TextView) view1.findViewById(R.id.trip_username);
                TextView tripDistance = (TextView) view1.findViewById(R.id.tripDistance);
                TextView tripMoney = (TextView) view1.findViewById(R.id.tripMoney);
                TextView tripTime = (TextView) view1.findViewById(R.id.tripTime);
                trip_username.setText(genericJsons[position].get("UserName").toString());
                tripMoney.setText(genericJsons[position].get("Money").toString());
                tripTime.setText(genericJsons[position].get("Time").toString());
                tripDistance.setText(genericJsons[position].get("Distance").toString());
                if ( position % 2 != 0) {
                    view1.setBackgroundResource(R.drawable.back_border_2);
                }


                return view1;
            }
        });


    }
}
