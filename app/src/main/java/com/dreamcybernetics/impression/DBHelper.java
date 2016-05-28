package com.dreamcybernetics.impression;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class DBHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "dtbase.db";
    public static final int DB_VERSION = 1;

    public static final String SQL_CREATE_POLLS = "CREATE TABLE " + Polls.TABLE_NAME +
            " (" + Polls._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            Polls.COLUMN_NAME_PARENT_ID + " INTEGER NOT NULL, " + Polls.COLUMN_NAME_ITEM_NAME +
            " TEXT NOT NULL, " + Polls.COLUMN_NAME_ITEM_TYPE + " INTEGER NOT NULL)";

    private static DBHelper mInstance = null;
    private static SQLiteDatabase mDBase = null;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static void openDatabase(Context context) {
        if (mInstance == null) {
            mInstance = new DBHelper(context);
            mDBase = mInstance.getWritableDatabase();
        }
    }

    public static void closeDatabase() {
        if (mInstance != null) {
            mDBase.close();
            mInstance.close();

            mDBase = null;
            mInstance = null;
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_POLLS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public static Cursor select(String tableName, String[] columns, String criteria) {
        return mDBase.query(tableName, columns, criteria, null, null, null, null);
    }

    public static int insert(String tableName, String[] columns, String[] values) {
        ContentValues cv = new ContentValues();
        for (int i = 0; i < columns.length; i++) {
            cv.put(columns[i], values[i]);
        }

        return (int)mDBase.insert(tableName, null, cv);
    }

    public static int update(String tableName, String[] columns, String[] values, String criteria) {
        ContentValues cv = new ContentValues();
        for (int i = 0; i < columns.length; i++) {
            cv.put(columns[i], values[i]);
        }

        return mDBase.update(tableName, cv, criteria, null);
    }

    public static int delete(String tableName, String criteria) {
        return mDBase.delete(tableName, criteria, null);
    }

    public static abstract class Polls implements BaseColumns {
        public static final String TABLE_NAME = "polls";
        public static final String COLUMN_NAME_PARENT_ID= "parent_id";
        public static final String COLUMN_NAME_ITEM_NAME = "item_name";
        public static final String COLUMN_NAME_ITEM_TYPE = "item_type";
        //public static final String COLUMN_NAME_ORDER_NUM = "order_num";
    }
}
