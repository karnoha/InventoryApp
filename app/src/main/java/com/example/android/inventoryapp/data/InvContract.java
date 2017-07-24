package com.example.android.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Karnoha on 23.07.2017.
 */

public class InvContract {
    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_INV = "inventory";

    private InvContract() {
    }

    public static abstract class InvEntry implements BaseColumns{
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INV);

        public static final String TABLE_NAME = "inventory";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_INV_NAME = "name";
        public static final String COLUMN_INV_TYPE = "type";
        public static final String COLUMN_INV_PRICE = "price";
        public static final String COLUMN_INV_QUANTITY = "quantity";
        public static final String COLUMN_INV_SOLD = "sold";
        public static final String COLUMN_INV_SUPPLIER = "supplier";
        public static final String COLUMN_INV_PICTURE = "picture";

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INV;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INV;
    }
}