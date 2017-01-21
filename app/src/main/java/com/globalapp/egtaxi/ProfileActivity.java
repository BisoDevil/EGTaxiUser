package com.globalapp.egtaxi;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaActionSound;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.google.api.client.extensions.android.AndroidUtils;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.android.callback.KinveyUserManagementCallback;
import com.kinvey.java.File;
import com.kinvey.java.User;
import com.kinvey.java.Util;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.core.MediaHttpUploader;
import com.kinvey.java.core.UploaderProgressListener;
import com.kinvey.java.model.FileMetaData;
import com.kinvey.java.model.KinveyMetaData;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.webkit.ConsoleMessage.MessageLevel.LOG;

public class ProfileActivity extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    TextView txtName, txtPhone, txtPassword, txtMail;
    Client mKinveyClient;
    String access;
    static final int RequestedImage = 123, RESULT_LOAD_IMG = 124;
    Bitmap bitmap;
    AQuery aQuery;

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
        setContentView(R.layout.activity_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initView();
        txtName.setText(sharedPreferences.getString("UserName", ""));
        txtPhone.setText(sharedPreferences.getString("PhoneNumber", ""));
        txtMail.setText(sharedPreferences.getString("E_Mail", ""));
        txtPassword.setText(sharedPreferences.getString("Password", ""));
        access = sharedPreferences.getString("access", "");


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

    private void initView() {
        txtName = (TextView) findViewById(R.id.txtUpdateFullName);
        txtMail = (TextView) findViewById(R.id.txtUpdateEmail);
        txtPhone = (TextView) findViewById(R.id.txtUpdatePhone);
        txtPassword = (TextView) findViewById(R.id.txtUpdatePassword);

        mKinveyClient = new Client.Builder(getApplicationContext()).build();
        aQuery = new AQuery(this);
        String imageURL = sharedPreferences.getString("imageURL", "");
        if (!imageURL.equals("")) {
            aQuery.id(R.id.imgEditProfile).image(imageURL, false, true);


        }
    }


    public void updateUser(View view) {
        Snackbar.make(view, getString(R.string.updating), Snackbar.LENGTH_LONG).show();
        mKinveyClient.user().put("full_Name", txtName.getText().toString());
        mKinveyClient.user().put("Phone_Number", txtPhone.getText().toString());
        mKinveyClient.user().put("email", txtMail.getText().toString());
        mKinveyClient.user().put("Type", "Rider");
        mKinveyClient.user().set("username", txtPhone.getText().toString());
        mKinveyClient.user().set("password", txtPassword.getText().toString());
        mKinveyClient.user().put("_password", txtPassword.getText().toString());
        mKinveyClient.user().update(mKinveyClient.user(), new KinveyClientCallback<User>() {
            @Override
            public void onSuccess(User user) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("UserName", user.get("full_Name").toString());
                editor.putString("PhoneNumber", user.get("Phone_Number").toString());
                editor.putString("E_Mail", user.get("email").toString());
                editor.putString("Password", txtPassword.getText().toString());
                editor.putString("access", user.getUsername());
                editor.apply();
                saveImage();
            }

            @Override
            public void onFailure(Throwable throwable) {
            }
        });

    }

    private void saveImage() {
        FileMetaData metadata = new FileMetaData("avr-" + mKinveyClient.user().getId());
        KinveyMetaData.AccessControlList Acl = new KinveyMetaData.AccessControlList();
        Acl.setGloballyReadable(true);

        metadata.setPublic(true);
        metadata.setAcl(Acl);
        metadata.setFileName("UserAvatar.png");

        try {
            java.io.File file = getFile(bitmap, "UserAvatar.png");
            mKinveyClient.file().upload(metadata, file, new UploaderProgressListener() {
                @Override
                public void progressChanged(MediaHttpUploader mediaHttpUploader) throws IOException {

                }

                @Override
                public void onSuccess(FileMetaData fileMetaData) {
                    Toast.makeText(ProfileActivity.this, getString(R.string.updated), Toast.LENGTH_SHORT).show();
                    loadImageURL();

                }

                @Override
                public void onFailure(Throwable throwable) {

                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void loadImage(View view) {
        String[] order = {getString(R.string.take_pic), getString(R.string.load_gallery)};
        AlertDialog.Builder load = new AlertDialog.Builder(this);
        load.setItems(order, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    Intent TakePic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (TakePic.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(TakePic, RequestedImage);
                    }
                } else if (i == 1) {
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                    startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
                }
            }
        }).show();


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestedImage && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            bitmap = (Bitmap) extras.get("data");
            CircleImageView circleImageView = (CircleImageView) findViewById(R.id.imgEditProfile);
            circleImageView.setImageBitmap(bitmap);

        } else if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            // Get the cursor
            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            // Move to first row
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String imgDecodableString = cursor.getString(columnIndex);
            cursor.close();
            bitmap = BitmapFactory.decodeFile(imgDecodableString);
            CircleImageView circleImageView = (CircleImageView) findViewById(R.id.imgEditProfile);
            circleImageView.setImageBitmap(bitmap);

        }
    }

    private java.io.File getFile(Bitmap bitmap, String name) throws IOException {
        java.io.File filesDir = getFilesDir();
        java.io.File imageFile = new java.io.File(filesDir, name + ".png");

        OutputStream os;
        try {
            os = new FileOutputStream(imageFile);
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 200, 200, true);
            scaled.compress(Bitmap.CompressFormat.PNG, 75, os);
            os.flush();
            os.close();
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Error writing bitmap", e);
        }
        return imageFile;
    }

    private void loadImageURL() {
        mKinveyClient.file().downloadMetaData("avr-" + mKinveyClient.user().getId(), new KinveyClientCallback<FileMetaData>() {
            @Override
            public void onSuccess(FileMetaData fileMetaData) {
                if (bitmap != null) {
                    aQuery.cache(sharedPreferences.getString("imageURL", ""), 1);
                }
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
