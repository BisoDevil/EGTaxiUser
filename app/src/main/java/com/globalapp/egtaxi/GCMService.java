package com.globalapp.egtaxi;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;


import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;


import com.kinvey.android.push.KinveyGCMService;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by Smiley on 7/6/2016.
 */
public class GCMService extends KinveyGCMService {

    private static long time;
    public static final String ACTION = FeesCalculation.class.getName() + "LocationBroadcast";

    @Override
    public void onMessage(String message) {

        String msg;
        try {
            JSONObject details = new JSONObject(message);
            msg = details.getString("msg");


        } catch (JSONException e) {
            msg = message;
            displayNotification(msg, 0);
        }
        if (time + 3000 > System.currentTimeMillis()) {
            return;
        }
        time = System.currentTimeMillis();
        switch (msg) {
            case "Start Trip":
                displayNotification(msg, 0);
                startService(new Intent(this, FeesCalculation.class));
                sendState(getString(R.string.on_trip));
                ComingActivity.isFinished = true;
                break;


            case "Trip is Finished":
                displayNotification(msg, 0);
                stopService(new Intent(this, FeesCalculation.class));
                Intent feedback = new Intent(getApplicationContext(), FeedbackActivity.class);
                feedback.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(feedback);
                ComingActivity.isFinished = true;

                break;

            case "I arrived": {
                displayNotification(msg, 0);
                sendState(getString(R.string.arrived));
            }
            break;
            case "I'm on my way":
                try {
                    JSONObject details = new JSONObject(message);
                    String msgs = details.getString("msg");
                    String driver = details.getString("driver_name");
                    String phone = details.getString("driver_phone");
                    String URL = details.getString("url");
                    String car = details.getString("carNo");
                    FeedbackActivity.driverName = driver;
                    ComingActivity.Name = driver;
                    ComingActivity.Phone = phone;
                    ComingActivity.image = URL;
                    ComingActivity.car = car;
                    ComingActivity.state = getString(R.string.coming);
                    displayNotification(msgs, 1);
                    Intent coming_driver = new Intent(getApplicationContext(), ComingActivity.class);
                    coming_driver.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(coming_driver);

                } catch (JSONException e) {
                    e.printStackTrace();
                }


                break;
            default: {
                displayNotification(msg, 0);
            }
        }

    }

    @Override
    public void onError(String error) {
        displayNotification(error, 0);
    }

    @Override
    public void onDelete(String deleted) {
        displayNotification(deleted, 0);
    }

    @Override
    public void onRegistered(String gcmID) {
        displayNotification(gcmID, 0);
    }

    @Override
    public void onUnregistered(String oldID) {
        displayNotification(oldID, 0);
    }

    //This method will return the WakefulBroadcastReceiver class you define in the next step
    public Class getReceiver() {
        return GCMReceiver.class;
    }

    private void displayNotification(String message, int i) {
        // TODO: 12/5/2016 handle receiving data
        Intent starter;
        if (i == 0) {
            starter = new Intent(getApplicationContext(), MapActivity.class);
        } else {
            starter = new Intent(getApplicationContext(), ComingActivity.class);
        }
        PendingIntent Pending = PendingIntent.getActivity(getApplicationContext(), 0, starter, 0);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(Pending)
                .setContentTitle(getApplicationContext().getResources().getString(R.string.app_name))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)


                .setContentText(message);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());
    }

    private void sendState(String text) {
        Intent intent = new Intent(ACTION);
        intent.putExtra("text", text);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
