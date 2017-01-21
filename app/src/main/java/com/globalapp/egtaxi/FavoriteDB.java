package com.globalapp.egtaxi;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by Smiley on 7/6/2016.
 */
public class FavoriteDB extends SQLiteOpenHelper {
    public FavoriteDB(Context context) {
        super(context, "Favorites.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("Create table IF NOT EXISTS Favorites (id INTEGER primary key,comment TEXT,address TEXT)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("Drop table if EXISTS Places");
        onCreate(db);

    }

    public void InsertRow(String comment, String address) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues Values = new ContentValues();
        Values.put("comment", comment);
        Values.put("address", address);
        db.insert("Favorites", null, Values);
    }

    public ArrayList getComments() {
        ArrayList array_list = new ArrayList();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from Favorites", null);
        res.moveToFirst();
        while (res.isAfterLast() == false) {

            array_list.add(res.getString(res.getColumnIndex("comment")));


            res.moveToNext();
        }
        return array_list;
    }

    public ArrayList getAddress() {
        ArrayList array_list = new ArrayList();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from Favorites", null);
        res.moveToFirst();
        while (res.isAfterLast() == false) {

            array_list.add(res.getString(res.getColumnIndex("address")));


            res.moveToNext();
        }
        return array_list;
    }
}
