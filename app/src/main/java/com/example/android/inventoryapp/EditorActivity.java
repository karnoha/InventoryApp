package com.example.android.inventoryapp;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InvContract.InvEntry;

import java.io.ByteArrayOutputStream;

import static java.lang.Integer.parseInt;

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
    private Button mOrderButton;
    private Button mQMinusButton;
    private Button mQPlusButton;
    private Button mAddPicture;

    // this is changed to true when editing a pet instead of creating new one
    private boolean mInvHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mInvHasChanged = true;
            return false;
        }
    };

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private boolean mhasImage = false;
    private Bitmap mBitmap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // this fetches uri for choosing whether we update an existing item or create a new one
        Intent intent = getIntent();
        mCurrentInvUri = intent.getData();
        mOrderButton = (Button) findViewById(R.id.editor_order_button);
        mQMinusButton = (Button) findViewById(R.id.editor_q_minus);
        mQPlusButton = (Button) findViewById(R.id.editor_q_plus);

        // set title for the activity
        if (mCurrentInvUri == null) {
            setTitle(getString(R.string.editor_activity_title_new));
            //disables options menu to hide delete button
            invalidateOptionsMenu();
            mOrderButton.setEnabled(false);
            mQMinusButton.setEnabled(false);
            mQPlusButton.setEnabled(false);
        } else {
            setTitle(getString(R.string.editor_activity_title_edit));
            getSupportLoaderManager().initLoader(EXISTING_INV_LOADER, null, this);
        }
        // find all views
        mNameEditText = (EditText) findViewById(R.id.editor_name);
        mTypeEditText = (EditText) findViewById(R.id.editor_type);
        mPriceEditText = (EditText) findViewById(R.id.editor_price);
        mQuantityTextView = (TextView) findViewById(R.id.editor_quantity);
        mSoldTextView = (TextView) findViewById(R.id.editor_sold);
        mSupplierEditText = (EditText) findViewById(R.id.editor_supplier);
        mPictureImageView = (ImageView) findViewById(R.id.editor_image);
        mAddPicture = (Button) findViewById(R.id.editor_add_picture);
        mhasImage = false;
        mBitmap = null;

        // setup ontouch listeners so we can track if it's changed
        // before we exit so we can popup dialog
        mNameEditText.setOnTouchListener(mTouchListener);
        mTypeEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityTextView.setOnTouchListener(mTouchListener);
        mSoldTextView.setOnTouchListener(mTouchListener);
        mSupplierEditText.setOnTouchListener(mTouchListener);
        mPictureImageView.setOnTouchListener(mTouchListener);
        mOrderButton.setOnTouchListener(mTouchListener);
        mQMinusButton.setOnTouchListener(mTouchListener);
        mQPlusButton.setOnTouchListener(mTouchListener);
        mAddPicture.setOnTouchListener(mTouchListener);

        mOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callSupplier(getSupplierNumber());
            }
        });

        mQMinusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quantityMinusClicked();
            }
        });

        mQPlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quantityPlusClicked();
            }
        });

        mAddPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });
    }

    private boolean saveItem() {
        // read data from edit fields
        String nameString = mNameEditText.getText().toString().trim();
        String typeString = mTypeEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityTextView.getText().toString().trim();
        String supplierString = mSupplierEditText.getText().toString().trim();

        // if all fields are blank, return back
        if (mCurrentInvUri == null
                && TextUtils.isEmpty(nameString)
                && TextUtils.isEmpty(typeString)
                && TextUtils.isEmpty(priceString)
                && TextUtils.isEmpty(quantityString)
                && TextUtils.isEmpty(supplierString)) {
            return true;
        }
        //check for empty fields and warn user if he skips a field
        else if (TextUtils.isEmpty(nameString)) {
            Toast.makeText(this, R.string.editor_check_before_save_name, Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(typeString)) {
            Toast.makeText(this, R.string.editor_check_before_save_type, Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(priceString)) {
            Toast.makeText(this, R.string.editor_check_before_save_price, Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(quantityString)) {
            Toast.makeText(this, R.string.editor_check_before_save_quantity, Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(supplierString)) {
            Toast.makeText(this, R.string.editor_check_before_save_supplier, Toast.LENGTH_SHORT).show();
            return false;
        } else if (!mhasImage) {
            Toast.makeText(this, R.string.editor_check_before_save_picture, Toast.LENGTH_SHORT).show();
            return false;
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        mBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] imageByte = byteArrayOutputStream.toByteArray();

        // create new contentvalues object with values
        ContentValues values = new ContentValues();
        values.put(InvEntry.COLUMN_INV_NAME, nameString);
        values.put(InvEntry.COLUMN_INV_TYPE, typeString);
        values.put(InvEntry.COLUMN_INV_PRICE, priceString);
        values.put(InvEntry.COLUMN_INV_QUANTITY, quantityString);
        values.put(InvEntry.COLUMN_INV_SUPPLIER, supplierString);
        values.put(InvEntry.COLUMN_INV_PICTURE, imageByte);


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
        return true;
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
                boolean saved = saveItem();
                // exit activity
                if (saved){
                    finish();
                }
                return true;
            case R.id.action_delete:
                // ask if it's ok to delete and return back
                showDeleteConfirmationDialog();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            mBitmap = (Bitmap) extras.get("data");
            mPictureImageView.setImageBitmap(mBitmap);
            mhasImage = true;
        }
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
            String quantity = cursor.getString(quantityColumnIndex);
            String sold = cursor.getString(soldColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);

            byte[] picture = cursor.getBlob(pictureColumnIndex);
            if (picture != null) {
                mhasImage = true;
                mBitmap = BitmapFactory.decodeByteArray(picture, 0, picture.length);
            }

            mNameEditText.setText(name);
            mTypeEditText.setText(type);
            mPriceEditText.setText(price);
            mQuantityTextView.setText(quantity);
            mSoldTextView.setText(sold);
            mSupplierEditText.setText(supplier);
            mPictureImageView.setImageBitmap(mBitmap);
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
        builder.setNegativeButton(R.string.dialog_delete_no, null);
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
                Toast.makeText(this, "Item deleted.", Toast.LENGTH_SHORT).show();

            }
        }
        finish();
    }

    private void callSupplier(int number) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + number));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private int getSupplierNumber() {
        int number = parseInt(mSupplierEditText.getText().toString().trim());
        return number;
    }

    private void quantityMinusClicked() {
        int currentQuantity = parseInt(mQuantityTextView.getText().toString().trim());
        if (currentQuantity == 0) {
            Toast.makeText(this, "Can't have negative quantity", Toast.LENGTH_SHORT).show();
        } else {
            currentQuantity--;
        }
        mQuantityTextView.setText(Integer.toString(currentQuantity));
    }

    private void quantityPlusClicked() {
        int currentQuantity = parseInt(mQuantityTextView.getText().toString().trim());
        currentQuantity++;
        mQuantityTextView.setText(Integer.toString(currentQuantity));
    }
}
