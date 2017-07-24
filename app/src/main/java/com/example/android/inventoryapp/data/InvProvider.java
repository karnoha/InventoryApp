package com.example.android.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Karnoha on 23.07.2017.
 */

public class InvProvider extends ContentProvider {

    // for log messages
    public static final String LOG_TAG = InvProvider.class.getSimpleName();

    private static final int INV = 100;
    private static final int INV_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(InvContract.CONTENT_AUTHORITY, InvContract.PATH_INV, INV);
        sUriMatcher.addURI(InvContract.CONTENT_AUTHORITY, InvContract.PATH_INV + "/#", INV_ID);
    }

    private InvDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new InvDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case INV:
                // query the whole table
                cursor = database.query(InvContract.InvEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case INV_ID:
                // query just one entry for specific ID
                selection = InvContract.InvEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(InvContract.InvEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INV:
                return InvContract.InvEntry.CONTENT_LIST_TYPE;
            case INV_ID:
                return InvContract.InvEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INV:
                return insertInv(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertInv(Uri uri, ContentValues values) {
        String name = values.getAsString(InvContract.InvEntry.COLUMN_INV_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Incorrect name: " + name);
        }

        String type = values.getAsString(InvContract.InvEntry.COLUMN_INV_TYPE);
        if (type == null) {
            throw new IllegalArgumentException("Incorrect type: " + type);
        }

        Integer price = values.getAsInteger(InvContract.InvEntry.COLUMN_INV_PRICE);
        if (price == null) {
            throw new IllegalArgumentException("Incorrect price: " + price);
        }
        // TODO dodelat check vsech podminek
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(InvContract.InvEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case INV:
                getContext().getContentResolver().notifyChange(uri, null);
                return database.delete(InvContract.InvEntry.TABLE_NAME, selection, selectionArgs);
            case INV_ID:
                selection = InvContract.InvEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                getContext().getContentResolver().notifyChange(uri, null);
                return database.delete(InvContract.InvEntry.TABLE_NAME, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Delete is not supported for " + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INV:
                return updateInv(uri, values, selection, selectionArgs);
            case INV_ID:
                selection = InvContract.InvEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateInv(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateInv(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // TODO dodelat check vsech podminek
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsUpdated = database.update(InvContract.InvEntry.TABLE_NAME, values, selection, selectionArgs);
        if (rowsUpdated <= 0) {
            Log.e(LOG_TAG, "Failed to update row for " + uri);
            return 0;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }
}
