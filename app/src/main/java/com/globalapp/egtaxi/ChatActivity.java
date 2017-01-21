package com.globalapp.egtaxi;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.google.common.eventbus.EventBus;
import com.kinvey.android.Client;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {
    static public ChatAdapter adapter;
    private EditText messageET;
    private ListView messagesContainer;
    private ArrayList<ChatMessage> chatHistory;
    SharedPreferences sharedPreferences;
    PubNub pubNub;
    Client mKinveyClient;
    private static String TAG = "ChatApp";
    String user = "";

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
        setContentView(R.layout.activity_chat);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initViews();


    }

    public void initViews() {
        messagesContainer = (ListView) findViewById(R.id.list_chat);
        messageET = (EditText) findViewById(R.id.txt_chat);
        adapter = new ChatAdapter(ChatActivity.this, new ArrayList<ChatMessage>());
        messagesContainer.setAdapter(adapter);
        mKinveyClient = new Client.Builder(this.getApplicationContext()).build();
        user = sharedPreferences.getString("UserName", "");

    }

    public void sendMessage(View view) {
        String messageText = messageET.getText().toString();

        if (TextUtils.isEmpty(messageText)) {
            return;
        }
        addMessage(messageText, true, user);
        publishMessage();

        messageET.setText("");


    }

    public void displayMessage(ChatMessage message) {
        adapter.add(message);
        adapter.notifyDataSetChanged();

        scroll();
    }


    private void scroll() {
        messagesContainer.setSelection(messagesContainer.getCount() - 1);
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

    @Override
    protected void onStart() {
        super.onStart();
        initPubNub();
    }

    private void initPubNub() {
        PNConfiguration pnConfiguration = new PNConfiguration();
        pnConfiguration.setSubscribeKey(getString(R.string.pubnub_sub_key));
        pnConfiguration.setPublishKey(getString(R.string.pupnub_puplish_key));
        pubNub = new PubNub(pnConfiguration);
        pubNub.subscribe()
                .channels(Arrays.asList("5863cd358d466b1d658cae0a"))
                .execute();
        pubNub.addListener(new SubscribeCallback() {
            @Override
            public void status(PubNub pubNub, PNStatus pnStatus) {

            }

            @Override
            public void message(PubNub pubNub, PNMessageResult pnMessageResult) {
                String id = pnMessageResult.getMessage().getAsJsonObject().get("ID").getAsString();
                String message = pnMessageResult.getMessage().getAsJsonObject().get("message").getAsString();
                String user = pnMessageResult.getMessage().getAsJsonObject().get("UserName").getAsString();
                if (!id.equals(mKinveyClient.user().getId())) {

                    addMessage(message, false, user);
                }

            }

            @Override
            public void presence(PubNub pubNub, PNPresenceEventResult pnPresenceEventResult) {

            }
        });

    }

    private void publishMessage() {
        Message data = new Message();
        data.ID = mKinveyClient.user().getId();
        data.message = messageET.getText().toString();
        data.UserName = user;
        pubNub.publish()
                .channel("5863cd358d466b1d658cae0a")
                .message(data)
                .shouldStore(false)
                .usePOST(true)
                .async(new PNCallback<PNPublishResult>() {
                    @Override
                    public void onResponse(PNPublishResult pnPublishResult, PNStatus pnStatus) {


                    }
                });
    }

    private void addMessage(final String message, final boolean Me, final String username) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String date = new SimpleDateFormat("h:m a", Locale.US).format(new Date());
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setId(122);
                chatMessage.setMessage(message);
                chatMessage.setDate(date);
                chatMessage.setMe(Me);
                chatMessage.setUsername(username);
                displayMessage(chatMessage);

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        pubNub.unsubscribe()
                .channels(Arrays.asList("5863cd358d466b1d658cae0a"))
                .execute();
    }

    class Message {
        String ID;
        String message;
        String UserName;
    }
}
