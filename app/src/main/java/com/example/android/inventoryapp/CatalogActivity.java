package com.example.android.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.inventoryapp.data.InvContract.InvEntry;

import java.util.List;

public class CatalogActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int INV_LOADER = 0;
    InvCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        ListView invListView = (ListView) findViewById(R.id.list);
        View emptyView = findViewById(R.id.empty_view);
        invListView.setEmptyView(emptyView);

        mCursorAdapter = new InvCursorAdapter(this, null);
        invListView.setAdapter(mCursorAdapter);

        invListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                Uri currentInvUri = ContentUris.withAppendedId(InvEntry.CONTENT_URI, id);
                intent.setData(currentInvUri);
                startActivity(intent);
            }
        });
        getLoaderManager().initLoader(INV_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_insert_dummy_data:
                insertDummyData();
                return true;
            case R.id.new_item:
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_delete_all_entries:
                deleteAllData();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void insertDummyData() {
        ContentValues values = new ContentValues();
        values.put(InvEntry.COLUMN_INV_NAME, "Banana");
        values.put(InvEntry.COLUMN_INV_TYPE, "Fruit");
        values.put(InvEntry.COLUMN_INV_PRICE, "10 CZK");
        values.put(InvEntry.COLUMN_INV_QUANTITY, "7");
        values.put(InvEntry.COLUMN_INV_SOLD, "13");
        values.put(InvEntry.COLUMN_INV_SUPPLIER, "800115435");

        Uri newUri = getContentResolver().insert(InvEntry.CONTENT_URI, values);
    }

    private void deleteAllData() {
        int rowsDeleted = getContentResolver().delete(InvEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from database");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                InvEntry._ID,
                InvEntry.COLUMN_INV_NAME,
                InvEntry.COLUMN_INV_TYPE,
                InvEntry.COLUMN_INV_PRICE,
                InvEntry.COLUMN_INV_QUANTITY,
                InvEntry.COLUMN_INV_SOLD};

        return new CursorLoader(this,
                InvEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // update adapter with new cursor containing new data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // called when needed to delete data
        mCursorAdapter.swapCursor(null);
    }
}
