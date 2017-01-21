package com.globalapp.egtaxi;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class FavoriteActivity extends AppCompatActivity {
SharedPreferences sharedPreferences;
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
        setContentView(R.layout.activity_favorite);
        ListView list = (ListView) findViewById(R.id.listFavorites);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final FavoriteDB db = new FavoriteDB(this);
        list.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return db.getAddress().size();
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
                View view1 = linflater.inflate(R.layout.fav_item, null);
                TextView txtComment = (TextView) view1.findViewById(R.id.txtCommet);
                TextView txtAddress = (TextView) view1.findViewById(R.id.txtAddress);
                txtComment.setText(db.getComments().get(position).toString());
                txtAddress.setText(db.getAddress().get(position).toString());
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.dialog_in);
                view1.setAnimation(animation);
                return view1;
            }

        });
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView txtAddress = (TextView) view.findViewById(R.id.txtAddress);
                try {

                    MapActivity mapActivity = new MapActivity();
                    mapActivity.txtSearch.setText(txtAddress.getText());
                    finish();
                } catch (Exception e) {
                    Log.e("Error", e.getMessage());
                }


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
}
