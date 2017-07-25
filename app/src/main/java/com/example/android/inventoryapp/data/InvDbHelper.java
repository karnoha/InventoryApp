package com.example.android.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.android.inventoryapp.data.InvContract.InvEntry;

/**
 * Created by Karnoha on 23.07.2017.
 */

public class InvDbHelper extends SQLiteOpenHelper{
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "inventory.db";

    //constructor
    public InvDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_INV_TABLE = "CREATE TABLE "
                + InvEntry.TABLE_NAME + "("
                + InvEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + InvEntry.COLUMN_INV_NAME + " TEXT NOT NULL, "
                + InvEntry.COLUMN_INV_TYPE + " TEXT NOT NULL, "
                + InvEntry.COLUMN_INV_PRICE + " INTEGER DEFAULT 0, "
                + InvEntry.COLUMN_INV_QUANTITY + " INTEGER DEFAULT 0, "
                + InvEntry.COLUMN_INV_SOLD + " INTEGER DEFAULT 0, "
                + InvEntry.COLUMN_INV_SUPPLIER + " TEXT NOT NULL, "
                + InvEntry.COLUMN_INV_PICTURE + " BLOB NOT NULL);";

        db.execSQL(SQL_CREATE_INV_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
