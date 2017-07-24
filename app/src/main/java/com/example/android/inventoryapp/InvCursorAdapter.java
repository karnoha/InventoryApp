package com.example.android.inventoryapp;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.inventoryapp.data.InvContract;

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
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tvName = (TextView) view.findViewById(R.id.list_name);
        TextView tvType = (TextView) view.findViewById(R.id.list_type);
        TextView tvPrice = (TextView) view.findViewById(R.id.list_price);
        TextView tvQuantity = (TextView) view.findViewById(R.id.list_quantity);
        TextView tvSold = (TextView) view.findViewById(R.id.list_sold);

        String name = cursor.getString(cursor.getColumnIndexOrThrow(InvContract.InvEntry.COLUMN_INV_NAME));
        String type = cursor.getString(cursor.getColumnIndexOrThrow(InvContract.InvEntry.COLUMN_INV_TYPE));
        String price = "price: $" + cursor.getString(cursor.getColumnIndexOrThrow(InvContract.InvEntry.COLUMN_INV_PRICE));
        String quantity = cursor.getString(cursor.getColumnIndexOrThrow(InvContract.InvEntry.COLUMN_INV_QUANTITY));
        String sold = cursor.getString(cursor.getColumnIndexOrThrow(InvContract.InvEntry.COLUMN_INV_SOLD));

        if (TextUtils.isEmpty(sold)){
            sold = "0";
        }

        tvName.setText(name);
        tvType.setText(type);
        tvPrice.setText(price);
        tvQuantity.setText(quantity);
        tvSold.setText(sold);
    }
}
