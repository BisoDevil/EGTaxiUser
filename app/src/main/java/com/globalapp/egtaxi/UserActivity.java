package com.globalapp.egtaxi;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.androidquery.AQuery;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.kinvey.android.AsyncUserDiscovery;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyUserListCallback;
import com.kinvey.android.callback.KinveyUserManagementCallback;
import com.kinvey.java.User;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.model.FileMetaData;
import com.kinvey.java.model.UserLookup;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class UserActivity extends AppCompatActivity {

    EditText txtFullName, txtMail, txtPhoneNo, txtPassword;
    SharedPreferences sharedPreferences;
    TextView txtForget;
    Client mKinveyClient;
    ProgressDialog dialog;
    CallbackManager callbackManager;
    LoginButton loginButton;

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
        FacebookSdk.sdkInitialize(getApplicationContext());

        callbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_user);
        initViews();


    }

    public void switchUser(View view) {
        Button btnmain = (Button) findViewById(R.id.btnLogin);
        TextView txtSwitch = (TextView) findViewById(R.id.txtNewUser);
        if (txtSwitch.getText().equals(getString(R.string.new_user))) {
            setViewVisibility(View.VISIBLE);
            btnmain.setText(getString(R.string.register));
            txtSwitch.setText(getString(R.string.already_user));
            txtForget.setVisibility(View.GONE);
        } else {
            setViewVisibility(View.GONE);
            btnmain.setText(getString(R.string.login));
            txtSwitch.setText(getString(R.string.new_user));
            txtForget.setVisibility(View.VISIBLE);
        }
    }

    public void btnMain(View view) {
        Button btnmain = (Button) findViewById(R.id.btnLogin);
        if (btnmain.getText().equals(getString(R.string.login))) {
            userLogin();
        } else if (btnmain.getText().equals(getString(R.string.register))) {
            userRegister();
        }
    }

    private void initViews() {
        txtFullName = (EditText) findViewById(R.id.txtFullName);
        txtMail = (EditText) findViewById(R.id.txtEmail);
        txtPhoneNo = (EditText) findViewById(R.id.txtPhoneNO);
        txtPassword = (EditText) findViewById(R.id.txtPassword);
        loginButton = (LoginButton) findViewById(R.id.loginButton);
        mKinveyClient = new Client.Builder(this.getApplicationContext()).build();
        txtForget = (TextView) findViewById(R.id.txtForget);
        loginButton.setReadPermissions("email");
        getLoginDetails(loginButton);
    }

    private void userLogin() {
        dialog = new ProgressDialog(this);
        dialog.setTitle(getString(R.string.logging_in));
        dialog.setMessage(getString(R.string.please_wait));
        dialog.show();
        final Intent map = new Intent(getApplicationContext(), MapActivity.class);
        mKinveyClient = new Client.Builder(this.getApplicationContext()).build();
        mKinveyClient.user().logout().execute();
        mKinveyClient.user().login(txtPhoneNo.getText().toString(), txtPassword.getText().toString(), new KinveyClientCallback<User>() {
            @Override
            public void onSuccess(User user) {
                dialog.dismiss();
                Toast.makeText(getApplicationContext(), getString(R.string.welcome) + " " + user.get("full_Name").toString(), Toast.LENGTH_SHORT).show();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("UserName", user.get("full_Name").toString());
                editor.putString("PhoneNumber", user.get("Phone_Number").toString());
                editor.putString("E_Mail", user.get("email").toString());
                editor.putString("Password", txtPassword.getText().toString());
                editor.putString("access", user.getUsername());
                editor.apply();
                getUserImage();
                startActivity(map);
                finish();


            }

            @Override
            public void onFailure(Throwable throwable) {
                dialog.dismiss();
                Toast.makeText(UserActivity.this, throwable.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void userRegister() {
        dialog = new ProgressDialog(this);
        dialog.setTitle(getString(R.string.registering));
        dialog.setMessage(getString(R.string.please_wait));
        dialog.show();
        final Intent map = new Intent(getApplicationContext(), MapActivity.class);


        mKinveyClient = new Client.Builder(getApplicationContext()).build();
        mKinveyClient.user().logout().execute();
        mKinveyClient.user().put("full_Name", txtFullName.getText().toString());
        mKinveyClient.user().put("Phone_Number", txtPhoneNo.getText().toString());
        mKinveyClient.user().put("email", txtMail.getText().toString());
        mKinveyClient.user().put("Type", "Rider");
        mKinveyClient.user().put("_password", txtPassword.getText().toString());
        mKinveyClient.user().create(txtPhoneNo.getText().toString(), txtPassword.getText().toString(), new KinveyClientCallback<User>() {
            @Override
            public void onSuccess(User user) {
                dialog.dismiss();
                Toast.makeText(getApplicationContext(), user.get("full_Name").toString() + ", your account has been created.", Toast.LENGTH_SHORT).show();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("UserName", user.get("full_Name").toString());
                editor.putString("PhoneNumber", user.get("Phone_Number").toString());
                editor.putString("E_Mail", user.get("email").toString());
                editor.putString("Password", txtPassword.getText().toString());
                editor.putString("access", user.getUsername());
                editor.apply();
                startActivity(map);
                finish();
            }

            @Override
            public void onFailure(Throwable throwable) {
                dialog.dismiss();
                Toast.makeText(getApplicationContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();


            }
        });
    }

    private void setViewVisibility(int Visibility) {
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.dialog_in);
        txtFullName.setVisibility(Visibility);
        txtFullName.setAnimation(animation);
        txtFullName.requestFocus();
        txtFullName.setText("");
        txtPhoneNo.setAnimation(animation);
        txtMail.setAnimation(animation);
        txtMail.setVisibility(Visibility);
        txtMail.setText("");
        txtPassword.setAnimation(animation);
        loginButton.setVisibility(Visibility);
    }

    private void getLoginDetails(LoginButton loginButton) {
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {


            @Override
            public void onSuccess(final LoginResult loginResult) {
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(
                                    JSONObject object,
                                    GraphResponse response) {
                                try {
                                    txtFullName.setText(object.getString("first_name") + " " + object.getString("last_name"));

                                    txtMail.setText(object.getString("email"));
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    String url = object.getJSONObject("picture").getJSONObject("data").getString("url");
                                    editor.putString("imageURL", url);
                                    editor.apply();


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "email,first_name,last_name,picture.type(large){url}");
                request.setParameters(parameters);
                request.executeAsync();

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }

        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


    public void getPassword(View view) {

        mKinveyClient.user().resetPassword(txtPhoneNo.getText().toString(), new KinveyUserManagementCallback() {
            @Override
            public void onSuccess(Void aVoid) {
                showPassword(getString(R.string.reset_meesage));
            }

            @Override
            public void onFailure(Throwable throwable) {
                showPassword(getString(R.string.fill_your_phone));
            }
        });

    }

    private void showPassword(String message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(message)
                .setTitle(getString(R.string.forget_password))
                .setCancelable(false)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton(getString(R.string.submit), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void getUserImage() {

        mKinveyClient.file().downloadMetaData("avr-" + mKinveyClient.user().getId(), new KinveyClientCallback<FileMetaData>() {
            @Override
            public void onSuccess(FileMetaData fileMetaData) {


                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("imageURL", fileMetaData.getDownloadURL());
                editor.apply();
            }

            @Override
            public void onFailure(Throwable throwable) {

            }
        });

    }
}
