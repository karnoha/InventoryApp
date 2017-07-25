package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InvContract;

import static android.R.attr.name;

/**
 * Created by Karnoha on 23.07.2017.
 */

public class InvCursorAdapter extends CursorAdapter {

    public InvCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        TextView tvName = (TextView) view.findViewById(R.id.list_name);
        TextView tvType = (TextView) view.findViewById(R.id.list_type);
        TextView tvPrice = (TextView) view.findViewById(R.id.list_price);
        TextView tvQuantity = (TextView) view.findViewById(R.id.list_quantity);
        TextView tvSold = (TextView) view.findViewById(R.id.list_sold);
        Button buttonSell = (Button) view.findViewById(R.id.list_sell_button);


        String name = cursor.getString(cursor.getColumnIndexOrThrow(InvContract.InvEntry.COLUMN_INV_NAME));
        String type = cursor.getString(cursor.getColumnIndexOrThrow(InvContract.InvEntry.COLUMN_INV_TYPE));
        String price = "price: $" + cursor.getString(cursor.getColumnIndexOrThrow(InvContract.InvEntry.COLUMN_INV_PRICE));
        final String quantity = cursor.getString(cursor.getColumnIndexOrThrow(InvContract.InvEntry.COLUMN_INV_QUANTITY));
        final String sold = cursor.getString(cursor.getColumnIndexOrThrow(InvContract.InvEntry.COLUMN_INV_SOLD));
        final int invId = cursor.getInt(cursor.getColumnIndexOrThrow(InvContract.InvEntry._ID));


        tvName.setText(name);
        tvType.setText(type);
        tvPrice.setText(price);
        tvQuantity.setText(quantity);
        tvSold.setText(sold);

        buttonSell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri mCurrentInvUri = ContentUris.withAppendedId(InvContract.InvEntry.CONTENT_URI, invId);
                int currentQuantity = Integer.parseInt(quantity);
                int currentSold = Integer.parseInt(sold);
                if (currentQuantity == 0) {
                    return;
                } else {
                    currentQuantity--;
                    currentSold++;
                    ContentValues values = new ContentValues();
                    values.put(InvContract.InvEntry.COLUMN_INV_QUANTITY, currentQuantity);
                    values.put(InvContract.InvEntry.COLUMN_INV_SOLD, currentSold);
                    context.getContentResolver().update(mCurrentInvUri, values, null, null);
                }
            }
        });
    }
}
