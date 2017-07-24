package com.example.android.inventoryapp;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InvContract.InvEntry;

import static android.R.attr.data;

/**
 * Created by Karnoha on 23.07.2017.
 */

public class EditorActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    // loader identifier
    private static final int EXISTING_INV_LOADER = 0;

    // create helper objects for all items in layout
    private Uri mCurrentInvUri;
    private EditText mNameEditText;
    private EditText mTypeEditText;
    private EditText mPriceEditText;
    private TextView mQuantityTextView;
    private TextView mSoldTextView;
    private EditText mSupplierEditText;
    private ImageView mPictureImageView;

    // this is changed to true when editing a pet instead of creating new one
    private boolean mInvHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mInvHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // this fetches uri for choosing whether we update an existing item or create a new one
        Intent intent = getIntent();
        mCurrentInvUri = intent.getData();

        // set title for the activity
        if (mCurrentInvUri == null) {
            setTitle(getString(R.string.editor_activity_title_new));
            //disables options menu to hide delete button
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit));
            getLoaderManager().initLoader(EXISTING_INV_LOADER, null, this);
        }
        // find all views
        mNameEditText = (EditText) findViewById(R.id.editor_name);
        mTypeEditText = (EditText) findViewById(R.id.editor_type);
        mPriceEditText = (EditText) findViewById(R.id.editor_price);
        mQuantityTextView = (TextView) findViewById(R.id.editor_quantity);
        mSoldTextView = (TextView) findViewById(R.id.editor_sold);
        mSupplierEditText = (EditText) findViewById(R.id.editor_supplier);
        mPictureImageView = (ImageView) findViewById(R.id.editor_image);

        // setup ontouch listeners so we can track if it's changed
        // before we exit so we can popup dialog
        mNameEditText.setOnTouchListener(mTouchListener);
        mTypeEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityTextView.setOnTouchListener(mTouchListener);
        mSoldTextView.setOnTouchListener(mTouchListener);
        mSupplierEditText.setOnTouchListener(mTouchListener);
        mPictureImageView.setOnTouchListener(mTouchListener);

        // todo nabindovat tlacitka
    }

    private void saveItem() {
        // read data from edit fields
        String nameString = mNameEditText.getText().toString().trim();
        String typeString = mTypeEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String supplierString = mSupplierEditText.getText().toString().trim();

        // if all fields are blank, return back
        if (mCurrentInvUri == null
                && TextUtils.isEmpty(nameString)
                && TextUtils.isEmpty(typeString)
                && TextUtils.isEmpty(priceString)
                && TextUtils.isEmpty(supplierString)) {
            return;
        }

        // create new contentvalues object with values
        ContentValues values = new ContentValues();
        values.put(InvEntry.COLUMN_INV_NAME, nameString);
        values.put(InvEntry.COLUMN_INV_TYPE, typeString);
        values.put(InvEntry.COLUMN_INV_PRICE, priceString);
        values.put(InvEntry.COLUMN_INV_SUPPLIER, supplierString);

        // check whether we create or update
        if (mCurrentInvUri == null) {
            // no uri => inserting new item
            Uri newUri = getContentResolver().insert(InvEntry.CONTENT_URI, values);
            //show toast if we're happy or not
            if (newUri == null) {
                Toast.makeText(this, getString(R.string.toast_insert_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.toast_insert_ok), Toast.LENGTH_SHORT).show();
            }
        } else {
            // uri exist => update current item
            int rowsAffected = getContentResolver().update(mCurrentInvUri, values, null, null);

            // show a toast if we're happy or not
            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.toast_update_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.toast_update_ok), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // inflate menu for this activity
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // if this is a new item, we don't have to show delete button.
        // There's nothing to delete.
        if (mCurrentInvUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // when user clicks on something from the menu
        switch (item.getItemId()) {
            case R.id.action_save:
                // save Item to db
                saveItem();
                // exit activity
                finish();
                return true;
            case R.id.action_delete:
                // ask if it's ok to delete and return back
                showDeleteConfirmationDialog();
                return true;
            case R.id.action_add_picture:
                // todo dodelat nahrani obrazku
                return true;
            case R.id.home:
                // this happens when user clicks the up arrow in the app bar
                if (!mInvHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                // if something is edited, popup a dialog
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // nothing changed, leave
        if (!mInvHasChanged) {
            super.onBackPressed();
            return;
        }

        // something changed, popup a dialog to make sure user wants this
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // this projection defines all columns from table
        String[] projection = {
                InvEntry._ID,
                InvEntry.COLUMN_INV_NAME,
                InvEntry.COLUMN_INV_TYPE,
                InvEntry.COLUMN_INV_PRICE,
                InvEntry.COLUMN_INV_QUANTITY,
                InvEntry.COLUMN_INV_SOLD,
                InvEntry.COLUMN_INV_SUPPLIER,
                InvEntry.COLUMN_INV_PICTURE};

        // loader will do the query on a background thread with specified uri and all columns
        return new CursorLoader(this,
                mCurrentInvUri,
                projection,
                null,
                null,
                null);
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // if cursor returns empty, return
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        // read data from the first line and get column indexes
        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(InvEntry.COLUMN_INV_NAME);
            int typeColumnIndex = cursor.getColumnIndex(InvEntry.COLUMN_INV_TYPE);
            int priceColumnIndex = cursor.getColumnIndex(InvEntry.COLUMN_INV_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InvEntry.COLUMN_INV_QUANTITY);
            int soldColumnIndex = cursor.getColumnIndex(InvEntry.COLUMN_INV_SOLD);
            int supplierColumnIndex = cursor.getColumnIndex(InvEntry.COLUMN_INV_SUPPLIER);
            int pictureColumnIndex = cursor.getColumnIndex(InvEntry.COLUMN_INV_PICTURE);

            String name = cursor.getString(nameColumnIndex);
            String type = cursor.getString(typeColumnIndex);
            String price = cursor.getString(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            int sold = cursor.getInt(soldColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            // jakej datovej typ bude obrazek?
            // TODO dodelat obrazek nacitani

            mNameEditText.setText(name);
            mTypeEditText.setText(type);
            mPriceEditText.setText(price);
            mQuantityTextView.setText(quantity);
            mSoldTextView.setText(sold);
            mSupplierEditText.setText(supplier);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // clear all data from all fields
        mNameEditText.setText("");
        mTypeEditText.setText("");
        mPriceEditText.setText("");
        mQuantityTextView.setText("");
        mSoldTextView.setText("");
        mSupplierEditText.setText("");
        mPictureImageView.setImageResource(0);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_discard_message);
        builder.setPositiveButton(R.string.dialog_discard_yes, discardButtonClickListener);
        builder.setNegativeButton(R.string.dialog_discard_no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //dismiss dialog
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // create and show alertdialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_delete_message);
        builder.setPositiveButton(R.string.dialog_delete_yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.dialog_delete_no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteItem() {
        if (mCurrentInvUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentInvUri, null, null);

            if (rowsDeleted == 0) {
                // if something goes wrong, popup a toast message
                Toast.makeText(this, "Error with deleting item " + mCurrentInvUri,
                        Toast.LENGTH_SHORT).show();
            } else {
                // delete okay
                Toast.makeText(this, "Item deleted." + mCurrentInvUri,
                        Toast.LENGTH_SHORT).show();

            }
        }
        finish();
    }
}
